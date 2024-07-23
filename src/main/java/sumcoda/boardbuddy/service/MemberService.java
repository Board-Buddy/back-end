package sumcoda.boardbuddy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sumcoda.boardbuddy.dto.MemberRequest;
import sumcoda.boardbuddy.dto.MemberResponse;
import sumcoda.boardbuddy.dto.NearPublicDistrictResponse;
import sumcoda.boardbuddy.dto.PublicDistrictResponse;
import sumcoda.boardbuddy.entity.Member;
import sumcoda.boardbuddy.enumerate.MemberRole;
import sumcoda.boardbuddy.enumerate.RankScorePoints;
import sumcoda.boardbuddy.enumerate.ReviewType;
import sumcoda.boardbuddy.exception.member.*;
import sumcoda.boardbuddy.exception.publicDistrict.PublicDistrictNotFoundException;
import sumcoda.boardbuddy.repository.MemberJdbcRepository;
import sumcoda.boardbuddy.repository.MemberRepository;
import sumcoda.boardbuddy.repository.memberGatherArticle.MemberGatherArticleRepository;
import sumcoda.boardbuddy.repository.publicDistrict.PublicDistrictRepository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    private final PublicDistrictRepository publicDistrictRepository;

    private final NearPublicDistrictService nearPublicDistrictService;

    private final MemberGatherArticleRepository memberGatherArticleRepository;

    private final MemberJdbcRepository memberJdbcRepository;

    // 비밀번호를 암호화 하기 위한 필드
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 아이디 중복검사
     *
     * @param verifyUsernameDuplicationDTO 사용자가 입력한 아이디
     **/
    public void verifyUsernameDuplication(MemberRequest.VerifyUsernameDuplicationDTO verifyUsernameDuplicationDTO) {

        Boolean isAlreadyExistsUsername = memberRepository.existsByUsername(verifyUsernameDuplicationDTO.getUsername());

        if (isAlreadyExistsUsername == null) {
            throw new MemberRetrievalException("유저를 조회하면서 서버 문제가 발생했습니다. 관리자에게 문의하세요.");
        }

        if (Boolean.TRUE.equals(isAlreadyExistsUsername)) {
            throw new UsernameAlreadyExistsException("동일한 아이디가 이미 존재합니다.");
        }
    }

    /**
     * 닉네임 중복검사
     *
     * @param verifyNicknameDuplicationDTO 사용자가 입력한 닉네임
     **/
    public void verifyNicknameDuplication(MemberRequest.VerifyNicknameDuplicationDTO verifyNicknameDuplicationDTO) {

        Boolean isAlreadyExistsNickname = memberRepository.existsByNickname(verifyNicknameDuplicationDTO.getNickname());

        if (isAlreadyExistsNickname == null) {
            throw new MemberRetrievalException("유저를 조회하면서 서버 문제가 발생했습니다. 관리자에게 문의하세요.");
        }

        if (Boolean.TRUE.equals(isAlreadyExistsNickname)) {
            throw new NicknameAlreadyExistsException("동일한 닉네임이 이미 존재합니다.");
        }
    }

    /**
     * 회원가입 요청 캐치
     *
     * @param registerDTO 전달받은 회원가입 정보
     **/
    @Transactional
    public void registerMember(MemberRequest.RegisterDTO registerDTO) {

        // 데이터베이스에 사용자가 입력한 행정 구역이 있는지 검증
        PublicDistrictResponse.LocationDTO baseLocation = publicDistrictRepository.findOneBySidoAndSiguAndDong(
                registerDTO.getSido(), registerDTO.getSigu(), registerDTO.getDong())
                .orElseThrow(() -> new PublicDistrictNotFoundException("입력한 위치 정보를 찾을 수 없습니다. 관리자에게 문의하세요."));

        Long memberId = memberRepository.save(Member.buildMember(
                registerDTO.getUsername(),
                bCryptPasswordEncoder.encode(registerDTO.getPassword()),
                registerDTO.getNickname(),
                registerDTO.getEmail(),
                registerDTO.getPhoneNumber(),
                registerDTO.getSido(),
                registerDTO.getSigu(),
                registerDTO.getDong(),
                2,
                50,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                null,
                null,
                0.0,
                MemberRole.USER,
                null)).getId();

        if (memberId == null) {
            throw new MemberSaveException("서버 문제로 회원가입에 실패하였습니다. 관리자에게 문의하세요.");
        }

        // 회원가입 시 주변 행정 구역 저장
        nearPublicDistrictService.saveNearDistrictByRegisterLocation(baseLocation);
    }

    /**
     * 애플리케이션 시작시 관리자 계정 생성
     *
     **/
    public void createAdminAccount() {
        Boolean existsByUsername = memberRepository.existsByUsername("admin");
        if (existsByUsername) {
            return;
        }
        memberRepository.save(Member.buildMember(
                "admin",
                bCryptPasswordEncoder.encode("a12345#"),
                "admin",
                "admin@naver.com",
                "01012345678",
                "서울 특별시",
                "강남구",
                "삼성동",
                2,
                50,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                null,
                null,
                0.0,
                MemberRole.USER,
                null)
        );
    }

    /**
     * 소셜 로그인 사용자에 대한 추가적인 회원가입
     *
     * @param oAuth2RegisterDTO 소셜로그인 사용자에 대한 추가적인 회원가입 정보
     * @param username 로그인 사용자 아이디
     **/
    @Transactional
    public void registerOAuth2Member(MemberRequest.OAuth2RegisterDTO oAuth2RegisterDTO, String username) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("해당 유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        // 데이터베이스에 사용자가 입력한 행정 구역이 있는지 검증
        PublicDistrictResponse.LocationDTO baseLocation = publicDistrictRepository.findOneBySidoAndSiguAndDong(
                oAuth2RegisterDTO.getSido(), oAuth2RegisterDTO.getSigu(), oAuth2RegisterDTO.getDong())
                .orElseThrow(() -> new PublicDistrictNotFoundException("입력한 위치 정보를 찾을 수 없습니다. 관리자에게 문의하세요."));

        member.assignPhoneNumber(oAuth2RegisterDTO.getPhoneNumber());
        member.assignSido(oAuth2RegisterDTO.getSido());
        member.assignSigu(oAuth2RegisterDTO.getSigu());
        member.assignDong(oAuth2RegisterDTO.getDong());

        // 회원가입 시 주변 행정 구역 저장
        nearPublicDistrictService.saveNearDistrictByRegisterLocation(baseLocation);
    }

    /**
     * 소셜 로그인 사용자에 대한 추가적인 회원가입
     *
     * @param username 로그인 사용자 아이디
     **/
    @Transactional
    public void withdrawalMember(String username) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("해당 유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        memberRepository.delete(member);

        // 삭제 확인
        boolean isExists = memberRepository.existsById(member.getId());
        if (isExists) {
            throw new MemberDeletionFailureException("회원 탈퇴에 실패했습니다. 관리자에게 문의하세요.");
        }
    }

    /**
     * 멤버 위치 설정 요청 캐치
     *
     * @param locationDTO 사용자가 입력한 위치 정보
     * @param username 로그인 사용자 아이디
     **/
    @Transactional
    public Map<Integer, List<NearPublicDistrictResponse.LocationDTO>> updateMemberLocation(MemberRequest.LocationDTO locationDTO, String username) {

        // 사용자가 입력한 시도, 시구, 동
        String sido = locationDTO.getSido();
        String sigu = locationDTO.getSigu();
        String dong = locationDTO.getDong();

        // 사용자 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("해당 유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        // 데이터베이스에 사용자가 입력한 행정 구역이 있는지 검증
        PublicDistrictResponse.LocationDTO baseLocation = publicDistrictRepository.findOneBySidoAndSiguAndDong(sido, sigu, dong)
                .orElseThrow(() -> new PublicDistrictNotFoundException("입력한 위치 정보를 찾을 수 없습니다. 관리자에게 문의하세요."));

        // 멤버의 위치 업데이트
        member.assignLocation(sido, sigu, dong);

        // 위치 설정 시 주변 행정 구역 저장 후 DTO 로 응답
        return nearPublicDistrictService.saveNearDistrictByUpdateLocation(baseLocation);
    }

    /**
     * 멤버 반경 설정
     *
     * @param radiusDTO 사용자가 입력한 반경 정보
     * @param username 로그인 사용자 아이디
     **/
    @Transactional
    public void updateMemberRadius(MemberRequest.RadiusDTO radiusDTO, String username) {
        // 사용자 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("해당 유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        // 멤버의 반경 업데이트
        member.assignRadius(radiusDTO.getRadius());
    }

    /**
     * 랭킹 조회
     *
     * @return TOP3 RankingsDTO list
     */
    @Transactional
    public List<MemberResponse.RankingsDTO> getTop3Rankings(){

        return memberRepository.findTop3RankingMembers();
    }

    /**
     * 리뷰 보내기 요청 캐치
     *
     * @param gatherArticleId 모집글 Id
     * @param reviewDTO 리뷰를 받는 유저 닉네임과 리뷰 타입을 담은 dto
     * @param username 로그인 사용자 아이디
     **/
    @Transactional
    public void sendReview(Long gatherArticleId, MemberRequest.ReviewDTO reviewDTO, String username) {
        //리뷰 보내는 유저 조회
        Member reviewer = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("리뷰를 보내는 유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        // 리뷰를 보내는 유저가 해당 모집글에 참가했는지 확인
        boolean isRelated = memberGatherArticleRepository.existsByGatherArticleIdAndMemberUsername(gatherArticleId, username);

        if (!isRelated) {
            throw new MemberNotJoinedGatherArticleException("잘못된 접근입니다.");
        }

        // 리뷰 받는 유저 조회
        Member reviewee = memberRepository.findByNickname(reviewDTO.getNickname())
                .orElseThrow(() -> new MemberRetrievalException("리뷰를 받는 유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        ReviewType reviewType = ReviewType.valueOf(String.valueOf(reviewDTO.getReview()));

        reviewee.assignReviewCount(reviewType);
        reviewer.assignSendReviewCount();
    }

    /**
     * 랭킹 집계 - 매월 1일 00시 스케줄링
     *
     */
    @Transactional
    @Scheduled(cron = "0 21 23 23 * ?") // 매월 1일 00시
    public void calculateMonthlyRankings() {

        log.info("Ranking calculation started.");

        LocalDateTime startOfLastMonth = getStartOfLastMonth();
        LocalDateTime endOfLastMonth = getEndOfLastMonth();

        List<Member> members = memberRepository.findAll();
        Map<Long, Double> memberScores = new HashMap<>();

        // 점수 계산
        for (Member member : members) {
          // 지난 달 모집글 갯수
          long gatherArticleCount = memberRepository.countGatherArticlesByMember(member, startOfLastMonth, endOfLastMonth);
          // 지난 달 댓글 갯수
          long commentCount = memberRepository.countCommentsByMember(member, startOfLastMonth, endOfLastMonth);
          // 후기 카운트, 리뷰 보낸 횟수 합하여 점수 계산
          double rankScore = calculateRankScore(member, gatherArticleCount, commentCount);
          memberScores.put(member.getId(), rankScore);
        }

        // 점수 업데이트
        memberJdbcRepository.updateMemberRankScores(memberScores);

        // 점수 별로 정렬
        List<Member> orderedByScoreMembers = memberRepository.findAllOrderedByRankScore();

        // 랭킹 업데이트
        Map<Long, Integer> rankUpdateMap = new HashMap<>();

        for (int i = 0; i < orderedByScoreMembers.size(); i++) {
          // TODO : 1 ~ 3등 뱃지 부여
          Member member = orderedByScoreMembers.get(i);
          if (i == 0) {
            rankUpdateMap.put(member.getId(), 1);
          } else if (i == 1) {
            rankUpdateMap.put(member.getId(), 2);
          } else if (i == 2) {
            rankUpdateMap.put(member.getId(), 3);
          } else {
            rankUpdateMap.put(member.getId(), null);
          }
        }

        // 랭킹 업데이트
        memberJdbcRepository.updateMemberRanks(rankUpdateMap);

        // 후기 카운트, 보낸 리뷰 카운트 초기화
        memberJdbcRepository.resetMonthlyCounts();

    }

    // 지난 달 시작일
    private LocalDateTime getStartOfLastMonth() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        return lastMonth.atDay(1).atStartOfDay();
    }

    // 지난 달 종료일
    private LocalDateTime getEndOfLastMonth() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        return lastMonth.atEndOfMonth().atTime(23, 59, 59);
    }

    // 점수 계산
    private double calculateRankScore(Member member, long gatherArticleCount, long commentCount) {
        double score = 0.0;
        score += member.getMonthlyExcellentCount() * RankScorePoints.EXCELLENT_REVIEW_SCORE.getScore();
        score += member.getMonthlyGoodCount() * RankScorePoints.GOOD_REVIEW_SCORE.getScore();
        score += member.getMonthlyBadCount() * RankScorePoints.BAD_REVIEW_SCORE.getScore();
        score += member.getMonthlyNoShowCount() * RankScorePoints.NOSHOW_REVIEW_SCORE.getScore();
        score += member.getMonthlySendReviewCount() * RankScorePoints.SEND_REVIEW_SCORE.getScore();
        score += gatherArticleCount * RankScorePoints.GATHER_ARTICLE_SCORE.getScore();
        score += commentCount * RankScorePoints.COMMENT_SCORE.getScore();
        return score;
    }

}
