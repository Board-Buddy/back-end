package sumcoda.boardbuddy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sumcoda.boardbuddy.dto.*;
import sumcoda.boardbuddy.entity.Member;
import sumcoda.boardbuddy.entity.ProfileImage;
import sumcoda.boardbuddy.enumerate.MemberRole;
import sumcoda.boardbuddy.enumerate.ReviewType;
import sumcoda.boardbuddy.exception.member.*;
import sumcoda.boardbuddy.exception.publicDistrict.PublicDistrictRetrievalException;
import sumcoda.boardbuddy.repository.MemberRepository;
import sumcoda.boardbuddy.repository.ProfileImageRepository;
import sumcoda.boardbuddy.repository.memberGatherArticle.MemberGatherArticleRepository;
import sumcoda.boardbuddy.repository.publicDistrict.PublicDistrictRepository;
import sumcoda.boardbuddy.util.FileStorageUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    private final PublicDistrictRepository publicDistrictRepository;

    private final NearPublicDistrictService nearPublicDistrictService;

    private final MemberGatherArticleRepository memberGatherArticleRepository;

    private final ProfileImageRepository profileImageRepository;

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
        PublicDistrictResponse.LocationDTO baseLocation = publicDistrictRepository.findLocationDTOBySidoAndSiguAndDong(
                registerDTO.getSido(), registerDTO.getSigu(), registerDTO.getDong())
                .orElseThrow(() -> new PublicDistrictRetrievalException("입력한 위치 정보를 찾을 수 없습니다. 관리자에게 문의하세요."));

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
                50.0,
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
                "서울특별시",
                "강남구",
                "삼성동",
                2,
                50.0,
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
        PublicDistrictResponse.LocationDTO baseLocation = publicDistrictRepository.findLocationDTOBySidoAndSiguAndDong(
                oAuth2RegisterDTO.getSido(), oAuth2RegisterDTO.getSigu(), oAuth2RegisterDTO.getDong())
                .orElseThrow(() -> new PublicDistrictRetrievalException("입력한 위치 정보를 찾을 수 없습니다. 관리자에게 문의하세요."));

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
        PublicDistrictResponse.LocationDTO baseLocation = publicDistrictRepository.findLocationDTOBySidoAndSiguAndDong(sido, sigu, dong)
                .orElseThrow(() -> new PublicDistrictRetrievalException("입력한 위치 정보를 찾을 수 없습니다. 관리자에게 문의하세요."));

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
        if (!memberGatherArticleRepository.isPermit(gatherArticleId, username)) {
            throw new MemberNotJoinedGatherArticleException("리뷰를 보낼 권한이 없습니다.");
        }

        // 리뷰 받는 유저 조회
        Member reviewee = memberRepository.findByNickname(reviewDTO.getNickname())
                .orElseThrow(() -> new MemberRetrievalException("리뷰를 받는 유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        ReviewType reviewType = ReviewType.valueOf(String.valueOf(reviewDTO.getReview()));

        incrementReviewCounts(reviewee, reviewType);
        incrementSendReviewCount(reviewer);
        updateBuddyScore(reviewee, reviewType);
    }

    /**
     * 각 리뷰 카운트 증가 메서드
     *
     * @param reviewee 리뷰를 받는 유저
     * @param reviewType 리뷰 타입
     **/
    private void incrementReviewCounts(Member reviewee, ReviewType reviewType) {
        Integer newMonthlyExcellentCount = reviewee.getMonthlyExcellentCount();
        Integer newTotalExcellentCount = reviewee.getTotalExcellentCount();
        Integer newMonthlyGoodCount = reviewee.getMonthlyGoodCount();
        Integer newTotalGoodCount = reviewee.getTotalGoodCount();
        Integer newMonthlyBadCount = reviewee.getMonthlyBadCount();
        Integer newTotalBadCount = reviewee.getTotalBadCount();
        Integer newMonthlyNoShowCount = reviewee.getMonthlyNoShowCount();

        switch (reviewType) {
            case EXCELLENT:
                newMonthlyExcellentCount++;
                newTotalExcellentCount++;
                break;
            case GOOD:
                newMonthlyGoodCount++;
                newTotalGoodCount++;
                break;
            case BAD:
                newMonthlyBadCount++;
                newTotalBadCount++;
                break;
            case NOSHOW:
                newMonthlyNoShowCount++;
                break;
        }

        reviewee.assignReviewCount(newMonthlyExcellentCount, newTotalExcellentCount, newMonthlyGoodCount, newTotalGoodCount, newMonthlyBadCount, newTotalBadCount, newMonthlyNoShowCount);
    }

    /**
     * 리뷰 보낸 횟수 증가 메서드
     *
     * @param reviewer 리뷰를 보낸 유저
     **/
    private void incrementSendReviewCount(Member reviewer) {
        Integer newSendReviewCount = reviewer.getMonthlySendReviewCount() + 1;

        reviewer.assignSendReviewCount(newSendReviewCount);
    }

    /**
     * 버디지수 업데이트 메서드
     *
     * @param reviewee 리뷰를 받는 유저
     * @param reviewType 리뷰 타입
     **/
    private void updateBuddyScore(Member reviewee, ReviewType reviewType) {
        // 리뷰 타입에 따라 얻는 버디 지수
        double gettingBuddyScore = reviewType.getScore();

        // 새로 계산된 버디 지수
        double newBuddyScore = reviewee.getBuddyScore() + gettingBuddyScore;

        // 버디 지수 업데이트
        reviewee.assignBuddyScore(newBuddyScore);
    }

    /**
     * 프로필 조회 요청 캐치
     *
     * @param nickname 유저 닉네임
     * @return 해당 닉네임의 유저 프로필
     **/
    @Transactional
    public MemberResponse.ProfileInfosDTO getMemberProfileByNickname(String nickname) {
        return memberRepository.findMemberProfileByNickname(nickname)
                .orElseThrow(() -> new MemberRetrievalException("유저를 찾을 수 없습니다. 관리자에게 문의하세요."));
    }

    /**
     * 프로필 수정 요청 캐치
     *
     * @param username 유저 아이디
     * @param updateProfileDTO 수정할 정보가 담겨있는 DTO
     * @param profileImageFile 수정할 프로필 이미지 파일
     **/
    @Transactional
    public void updateProfile(String username, MemberRequest.UpdateProfileDTO updateProfileDTO, MultipartFile profileImageFile) {
        // 유저 아이디로 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        // 닉네임이 null이 아니면 업데이트
        if (updateProfileDTO.getNickname() != null) {
            member.assignNickname(updateProfileDTO.getNickname());
        }

        // 비밀번호가 null이 아니면 암호화 후 업데이트
        if (updateProfileDTO.getPassword() != null && !updateProfileDTO.getPassword().isEmpty()) {
            member.assignPassword(bCryptPasswordEncoder.encode(updateProfileDTO.getPassword()));
        }

        // 핸드폰 번호가 null이 아니면 업데이트
        if (updateProfileDTO.getPhoneNumber() != null) {
            member.assignPhoneNumber(updateProfileDTO.getPhoneNumber());
        }

        // 자기소개가 null이 아니면 업데이트
        if (updateProfileDTO.getDescription() != null) {
            member.assignDescription(updateProfileDTO.getDescription());
        }

        if (profileImageFile == null || profileImageFile.isEmpty()) {
            member.assignProfileImage(null);
        } else {
            try {
                if (!profileImageFile.isEmpty()) {
                    // 이미지 파일 형식 검증
                    String contentType = profileImageFile.getContentType();
                    if (contentType != null && !contentType.startsWith("multipart/form-data")) {
                        throw new InvalidFileFormatException("지원되지 않는 파일 형식입니다.");
                    }

                    FileDTO fileDTO = FileStorageUtil.saveFile(profileImageFile);
                    String profileImageUrl = FileStorageUtil.getLocalStoreDir(fileDTO.getSavedFilename());

                    ProfileImage newProfileImage = ProfileImage.buildProfileImage(
                            fileDTO.getOriginalFilename(),
                            fileDTO.getSavedFilename(),
                            profileImageUrl
                    );

                    profileImageRepository.save(newProfileImage);
                    member.assignProfileImage(newProfileImage);
                }
            } catch (IOException e) {
                throw new ProfileImageSaveException("프로필 이미지를 저장하는 동안 오류가 발생했습니다.");
            }
        }
    }
}
