package sumcoda.boardbuddy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sumcoda.boardbuddy.dto.GatherArticleRequest;
import sumcoda.boardbuddy.entity.GatherArticle;
import sumcoda.boardbuddy.entity.Member;
import sumcoda.boardbuddy.entity.MemberGatherArticle;
import sumcoda.boardbuddy.enumerate.GatherArticleRole;
import sumcoda.boardbuddy.exception.gatherArticle.GatherArticleNotFoundException;
import sumcoda.boardbuddy.exception.gatherArticle.GatherArticleSaveException;
import sumcoda.boardbuddy.exception.gatherArticle.GatherArticleUpdateException;
import sumcoda.boardbuddy.exception.gatherArticle.UnauthorizedActionException;
import sumcoda.boardbuddy.exception.member.InvalidUserException;
import sumcoda.boardbuddy.repository.gatherArticle.GatherArticleRepository;
import sumcoda.boardbuddy.repository.MemberRepository;
import sumcoda.boardbuddy.repository.memberGatherArticle.MemberGatherArticleRepository;

import java.time.LocalDateTime;
import java.util.List;

import static sumcoda.boardbuddy.dto.GatherArticleResponse.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GatherArticleService {

    private final MemberRepository memberRepository;

    private final GatherArticleRepository gatherArticleRepository;

    private final MemberGatherArticleRepository memberGatherArticleRepository;

    /**
     * 모집글 작성
     * @param createRequest
     * @param username
     * @return
     */
    @Transactional
    public CreateDTO createGatherArticle(GatherArticleRequest.CreateDTO createRequest, String username){

        // 사용자 검증
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidUserException("유효하지 않은 사용자입니다."));

        // 작성 요청 예외 검증, 처리
        validateCreateRequest(createRequest);

        // 엔티티로 변환
        GatherArticle gatherArticle = createRequest.toEntity();

        // 저장
        gatherArticleRepository.save(gatherArticle);

        // memberGatherArticle 생성
        MemberGatherArticle memberGatherArticle = MemberGatherArticle.createMemberGatherArticle(
                LocalDateTime.now(),
                true,
                GatherArticleRole.AUTHOR,
                member,
                gatherArticle
        );

        // 저장
        memberGatherArticleRepository.save(memberGatherArticle);

        return CreateDTO.from(gatherArticle);
    }

    /**
     * 모집글 조회
     * @param id
     * @return
     */
    public ReadDTO getGatherArticle(Long id, String username) {

        // // 존재하는 모집글인지 확인
        GatherArticle gatherArticle = gatherArticleRepository.findById(id)
                .orElseThrow(() -> new GatherArticleNotFoundException("존재하지 않는 모집글입니다."));

        // 사용자 검증
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidUserException("유효하지 않은 사용자입니다."));

        // 사용자와 모집글의 관계 찾기
        String participationStatus = getParticipationStatus(gatherArticle, member);

        // 작성자 가져오기
        Member author = memberGatherArticleRepository.findAuthorByGatherArticleId(id);

        return ReadDTO.from(gatherArticle, author, participationStatus);
    }

    /**
     * 모집글 수정
     * @param id
     * @param updateRequest
     * @param username
     * @return
     */
    @Transactional
    public UpdateDTO updateGatherArticle(Long id, GatherArticleRequest.UpdateDTO updateRequest, String username) {

        // 존재하는 모집글인지 확인
        GatherArticle gatherArticle = gatherArticleRepository.findById(id)
                .orElseThrow(() -> new GatherArticleNotFoundException("존재하지 않는 모집글입니다."));

        // 사용자 검증
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidUserException("유효하지 않은 사용자입니다."));

        // 작성자 검증
        if (!memberGatherArticleRepository.isAuthor(id, member.getId())) {
            throw new UnauthorizedActionException("작성자만 수정할 수 있습니다.");
        }

        // 예외 검증, 처리
        validateUpdateRequest(updateRequest, gatherArticle);

        // 수정
        updateRequest.updateEntity(gatherArticle);

        // 저장
        gatherArticleRepository.save(gatherArticle);

        return UpdateDTO.from(gatherArticle);
    }

    /**
     * 모집글 삭제
     * @param id
     * @param username
     * @return
     */
    @Transactional
    public DeleteDTO deleteGatherArticle(Long id, String username) {

        // 존재하는 모집글인지 확인
        GatherArticle gatherArticle = gatherArticleRepository.findById(id)
                .orElseThrow(() -> new GatherArticleNotFoundException("존재하지 않는 모집글입니다."));

        // 사용자 검증
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidUserException("유효하지 않은 사용자입니다."));

        // 작성자인지 검증
        if (!memberGatherArticleRepository.isAuthor(id, member.getId())) {
            throw new UnauthorizedActionException("작성자만 삭제할 수 있습니다.");
        }

        // 삭제
        gatherArticleRepository.delete(gatherArticle);

        return DeleteDTO.from(gatherArticle);
    }

    /**
     * 작성 요청 검증
     * @param createRequest
     */
    private void validateCreateRequest(GatherArticleRequest.CreateDTO createRequest) {
        if (createRequest.getTitle() == null || createRequest.getTitle().isEmpty()) {
            throw new GatherArticleSaveException("제목이 입력되지 않았습니다.");
        }
        if (createRequest.getDescription() == null || createRequest.getDescription().isEmpty()) {
            throw new GatherArticleSaveException("설명이 입력되지 않았습니다.");
        }
        if (createRequest.getMeetingLocation() == null || createRequest.getMeetingLocation().isEmpty()) {
            throw new GatherArticleSaveException("장소가 입력되지 않았습니다.");
        }
        if (createRequest.getSido() == null || createRequest.getSido().isEmpty()) {
            throw new GatherArticleSaveException("시, 도가 입력되지 않았습니다.");
        }
        if (createRequest.getSigu() == null || createRequest.getSigu().isEmpty()) {
            throw new GatherArticleSaveException("시, 구가 입력되지 않았습니다.");
        }
        if (createRequest.getDong() == null || createRequest.getDong().isEmpty()) {
            throw new GatherArticleSaveException("동이 입력되지 않았습니다.");
        }
        if (createRequest.getStartDateTime() == null) {
            throw new GatherArticleSaveException("시작 시간이 입력되지 않았습니다.");
        }
        if (createRequest.getEndDateTime() == null) {
            throw new GatherArticleSaveException("종료 시간이 입력되지 않았습니다.");
        }
        if (createRequest.getMaxParticipants() == null || createRequest.getMaxParticipants() <= 0) {
            throw new GatherArticleSaveException("최대 참가 인원이 유효하지 않습니다.");
        }
    }

    /** 수정 요청 검증
     * @param updateRequest
     * @param gatherArticle
     */
    private void validateUpdateRequest(GatherArticleRequest.UpdateDTO updateRequest, GatherArticle gatherArticle) {
        if (updateRequest.getTitle() == null || updateRequest.getTitle().isEmpty()) {
            throw new GatherArticleUpdateException("제목이 입력되지 않았습니다.");
        }
        if (updateRequest.getDescription() == null || updateRequest.getDescription().isEmpty()) {
            throw new GatherArticleUpdateException("설명이 입력되지 않았습니다.");
        }
        if (updateRequest.getMeetingLocation() == null || updateRequest.getMeetingLocation().isEmpty()) {
            throw new GatherArticleUpdateException("장소가 입력되지 않았습니다.");
        }
        if (updateRequest.getSido() == null || updateRequest.getSido().isEmpty()) {
            throw new GatherArticleUpdateException("시도가 입력되지 않았습니다.");
        }
        if (updateRequest.getSigu() == null || updateRequest.getSigu().isEmpty()) {
            throw new GatherArticleUpdateException("시구가 입력되지 않았습니다.");
        }
        if (updateRequest.getDong() == null || updateRequest.getDong().isEmpty()) {
            throw new GatherArticleUpdateException("동이 입력되지 않았습니다.");
        }
        if (updateRequest.getStartDateTime() == null) {
            throw new GatherArticleUpdateException("시작 시간이 입력되지 않았습니다.");
        }
        if (updateRequest.getEndDateTime() == null) {
            throw new GatherArticleUpdateException("종료 시간이 입력되지 않았습니다.");
        }
        if (updateRequest.getMaxParticipants() == null || updateRequest.getMaxParticipants() <= 0) {
            throw new GatherArticleUpdateException("최대 참가 인원이 유효하지 않습니다.");
        }
        if (updateRequest.getMaxParticipants() < gatherArticle.getCurrentParticipants()) {
            throw new GatherArticleUpdateException("최대 참가 인원은 현재 참가 인원보다 적을 수 없습니다.\n 현재 참가 인원 : "
                    + gatherArticle.getCurrentParticipants() + "명");
        }
    }

    /**
     * 사용자와 모집글의 관계 찾기
     * @param gatherArticle
     * @param member
     * @return
     */
    private String getParticipationStatus(GatherArticle gatherArticle, Member member) {
        MemberGatherArticle memberGatherArticle = memberGatherArticleRepository.findByGatherArticleAndMember(gatherArticle, member);

        // 아무런 연관이 없는 사용자
        if (memberGatherArticle == null) {
            return "none";
        }
        // 작성자
        if (memberGatherArticle.getGatherArticleRole() == GatherArticleRole.AUTHOR) {
            return "author";
        }
        // 참가자
        if (memberGatherArticle.getIsPermit()) {
            return "permitted";
        }
        // 대기자
        return "waiting";
    }

    /**
     * 내가 작성한 모집글 조회
     *
     * @param username 회원의 username
     * @return 내가 작성한 모집글 DTO 리스트
     **/
    @Transactional
    public List<GatherArticleInfosDTO> getMyGatherArticles(String username) {
        return gatherArticleRepository.findGatherArticleInfosByUsername(username);
    }

    /**
     * 참가한 모집글 조회
     *
     * @param username 회원의 username
     * @return 참가한 모집글 DTO 리스트
     **/
    @Transactional
    public List<GatherArticleInfosDTO> getMyParticipations(String username) {
        return gatherArticleRepository.findParticipationsByUsername(username);
    }
}
