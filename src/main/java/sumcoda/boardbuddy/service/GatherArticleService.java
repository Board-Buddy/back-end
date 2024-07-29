package sumcoda.boardbuddy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sumcoda.boardbuddy.dto.*;
import sumcoda.boardbuddy.entity.GatherArticle;
import sumcoda.boardbuddy.entity.Member;
import sumcoda.boardbuddy.entity.MemberGatherArticle;
import sumcoda.boardbuddy.enumerate.MemberGatherArticleRole;
import sumcoda.boardbuddy.enumerate.ParticipationApplicationStatus;
import sumcoda.boardbuddy.exception.gatherArticle.GatherArticleNotFoundException;
import sumcoda.boardbuddy.exception.gatherArticle.GatherArticleSaveException;
import sumcoda.boardbuddy.exception.gatherArticle.GatherArticleUpdateException;
import sumcoda.boardbuddy.exception.gatherArticle.GatherArticleAccessDeniedException;

import sumcoda.boardbuddy.enumerate.GatherArticleStatus;
import sumcoda.boardbuddy.exception.gatherArticle.*;
import sumcoda.boardbuddy.exception.member.MemberRetrievalException;
import sumcoda.boardbuddy.exception.nearPublicDistrict.NearPublicDistrictRetrievalException;
import sumcoda.boardbuddy.exception.publicDistrict.PublicDistrictRetrievalException;
import sumcoda.boardbuddy.repository.gatherArticle.GatherArticleRepository;
import sumcoda.boardbuddy.repository.MemberRepository;
import sumcoda.boardbuddy.repository.memberGatherArticle.MemberGatherArticleRepository;
import sumcoda.boardbuddy.repository.nearPublicDistric.NearPublicDistrictRepository;
import sumcoda.boardbuddy.repository.publicDistrict.PublicDistrictRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GatherArticleService {

    private final MemberRepository memberRepository;

    private final GatherArticleRepository gatherArticleRepository;

    private final MemberGatherArticleRepository memberGatherArticleRepository;

    private final NearPublicDistrictRepository nearPublicDistrictRepository;

    private final PublicDistrictRepository publicDistrictRepository;

    private static final int PAGE_SIZE = 15;

    /**
     * 모집글 작성
     * @param createRequest
     * @param username
     * @return
     */
    @Transactional
    public GatherArticleResponse.CreateDTO createGatherArticle(GatherArticleRequest.CreateDTO createRequest, String username){

        // 사용자 검증
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("유효하지 않은 사용자입니다."));

        // 작성 요청 예외 검증, 처리
        validateCreateRequest(createRequest);

        // 엔티티로 변환
        GatherArticle gatherArticle = createRequest.toEntity();

        // 저장
        gatherArticleRepository.save(gatherArticle);

        // memberGatherArticle 생성
        MemberGatherArticle memberGatherArticle = MemberGatherArticle.buildMemberGatherArticle(
                LocalDateTime.now(),
                MemberGatherArticleRole.AUTHOR,
                member,
                gatherArticle
        );

        // 저장
        memberGatherArticleRepository.save(memberGatherArticle);

        return GatherArticleResponse.CreateDTO.from(gatherArticle);
    }

    /**
     * 모집글 조회
     * @param id
     * @return
     */
    public GatherArticleResponse.ReadDTO getGatherArticle(Long id, String username) {

        // 존재하는 모집글인지 확인
        GatherArticle gatherArticle = gatherArticleRepository.findById(id)
                .orElseThrow(() -> new GatherArticleNotFoundException("존재하지 않는 모집글입니다."));

        // 사용자 검증
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("유효하지 않은 사용자입니다."));

        // 사용자와 모집글의 관계 찾기
        String participationStatus = getParticipationStatus(gatherArticle, member);

        // 작성자 가져오기
        Member author = memberGatherArticleRepository.findAuthorByGatherArticleId(id);

        return GatherArticleResponse.ReadDTO.from(gatherArticle, author, participationStatus);
    }

    /**
     * 모집글 수정
     * @param id
     * @param updateRequest
     * @param username
     * @return
     */
    @Transactional
    public GatherArticleResponse.UpdateDTO updateGatherArticle(Long id, GatherArticleRequest.UpdateDTO updateRequest, String username) {

        // 존재하는 모집글인지 확인
        GatherArticle gatherArticle = gatherArticleRepository.findById(id)
                .orElseThrow(() -> new GatherArticleNotFoundException("존재하지 않는 모집글입니다."));

        // 사용자 검증
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("유효하지 않은 사용자입니다."));

        // 작성자 검증
        if (!memberGatherArticleRepository.isAuthor(id, member.getId())) {
            throw new GatherArticleAccessDeniedException("작성자만 수정할 수 있습니다.");
        }

        // 예외 검증, 처리
        validateUpdateRequest(updateRequest, gatherArticle);

        // 수정
        updateRequest.updateEntity(gatherArticle);

        return GatherArticleResponse.UpdateDTO.from(gatherArticle);
    }

    /**
     * 모집글 삭제
     * @param id
     * @param username
     * @return
     */
    @Transactional
    public GatherArticleResponse.DeleteDTO deleteGatherArticle(Long id, String username) {

        // 존재하는 모집글인지 확인
        GatherArticle gatherArticle = gatherArticleRepository.findById(id)
                .orElseThrow(() -> new GatherArticleNotFoundException("존재하지 않는 모집글입니다."));

        // 사용자 검증
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("유효하지 않은 사용자입니다."));

        // 작성자인지 검증
        if (!memberGatherArticleRepository.isAuthor(id, member.getId())) {
            throw new GatherArticleAccessDeniedException("작성자만 삭제할 수 있습니다.");
        }

        // 삭제
        gatherArticleRepository.delete(gatherArticle);

        return GatherArticleResponse.DeleteDTO.from(gatherArticle);
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
        if (createRequest.getSgg() == null || createRequest.getSgg().isEmpty()) {
            throw new GatherArticleSaveException("시, 구가 입력되지 않았습니다.");
        }
        if (createRequest.getEmd() == null || createRequest.getEmd().isEmpty()) {
            throw new GatherArticleSaveException("동이 입력되지 않았습니다.");
        }
        if (createRequest.getX() == null) {
            throw new GatherArticleSaveException("경도가 입력되지 않았습니다.");
        }
        if (createRequest.getY() == null) {
            throw new GatherArticleSaveException("위도가 입력되지 않았습니다.");
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
        if (updateRequest.getSgg() == null || updateRequest.getSgg().isEmpty()) {
            throw new GatherArticleUpdateException("시구가 입력되지 않았습니다.");
        }
        if (updateRequest.getEmd() == null || updateRequest.getEmd().isEmpty()) {
            throw new GatherArticleUpdateException("동이 입력되지 않았습니다.");
        }
        if (updateRequest.getX() == null) {
            throw new GatherArticleUpdateException("경도가 입력되지 않았습니다.");
        }
        if (updateRequest.getY() == null) {
            throw new GatherArticleUpdateException("위도가 입력되지 않았습니다.");
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
        if (memberGatherArticle.getMemberGatherArticleRole() == MemberGatherArticleRole.AUTHOR) {
            return "author";
        }
        // 참가자
        if (memberGatherArticle.getParticipationApplication().getParticipationApplicationStatus() == ParticipationApplicationStatus.APPROVED) {
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
    public List<GatherArticleResponse.GatherArticleInfosDTO> getMyGatherArticles(String username) {

        if (username == null) {
            throw new MemberRetrievalException("작성한 모집글 조회 요청을 처리할 수 없습니다. 관리자에게 문의하세요.");
        }

        return gatherArticleRepository.findGatherArticleInfosByUsername(username);
    }

    /**
     * 참가한 모집글 조회
     *
     * @param username 회원의 username
     * @return 참가한 모집글 DTO 리스트
     **/
    public List<GatherArticleResponse.GatherArticleInfosDTO> getMyParticipations(String username) {

        if (username == null) {
            throw new MemberRetrievalException("참가한 모집글 조회 요청을 처리할 수 없습니다. 관리자에게 문의하세요.");
        }

        return gatherArticleRepository.findParticipationsByUsername(username);
    }

    /**
     * 모집글 리스트 조회
     *
     * @param page     페이지 번호
     * @param status   모집 상태 (옵션)
     * @param sort     정렬 기준 (옵션)
     * @param username 사용자 이름
     * @return 모집글 리스트 DTO
     */
    public GatherArticleResponse.ReadListDTO getGatherArticles(Integer page, String status, String sort, String username) {

        // 정렬 기준 검증
        if (sort != null && !sort.equals(GatherArticleStatus.SOON.getValue())) {
            throw new GatherArticleSortException("유효하지 않은 정렬 기준입니다.");
        }

        // 모집글 상태 검증
        if (status != null && !status.equals(GatherArticleStatus.OPEN.getValue())) {
            throw new GatherArticleStatusException("유효하지 않은 모집글 상태입니다.");
        }

        // 사용자 위치 및 반경 정보 조회
        MemberResponse.LocationWithRadiusDTO locationWithRadiusDTO = memberRepository.findLocationWithRadiusDTOByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("해당 유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        // 기준 위치에 해당하는 행정 구역을 조회
        PublicDistrictResponse.LocationWithIdDTO locationWithIdDTO = publicDistrictRepository.findLocationWithIdDTOBySidoAndSggAndEmd(
                        locationWithRadiusDTO.getSido(), locationWithRadiusDTO.getSgg(), locationWithRadiusDTO.getEmd())
                .orElseThrow(() -> new PublicDistrictRetrievalException("유저의 위치 정보를 찾을 수 없습니다. 관리자에게 문의하세요."));

        // 사용자의 위치와 반경 정보로 주변 행정 구역 조회
        List<NearPublicDistrictResponse.LocationDTO> locationDTOs = nearPublicDistrictRepository.findLocationDTOsByPublicDistrictIdAndRadius(
                locationWithIdDTO.getId(), locationWithRadiusDTO.getRadius());

        // 주변 행정 구역이 없는 경우 예외 처리
        if (locationDTOs.isEmpty()) {
            throw new NearPublicDistrictRetrievalException("유저의 주변 행정 구역을 찾을 수 없습니다. 관리자에게 문의하세요.");
        }

        // 주변 행정 구역 리스트 생성
        List<String> sidoList = new ArrayList<>();
        List<String> sggList = new ArrayList<>();
        List<String> emdList = new ArrayList<>();

        // 주변 행정 구역 리스트에 데이터 추가
        locationDTOs.forEach(district -> {
            sidoList.add(district.getSido());
            sggList.add(district.getSgg());
            emdList.add(district.getEmd());
        });

        // 페이징 정보 생성
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        // 모집글 리스트 조회
        Slice<GatherArticleResponse.ReadSliceDTO> readSliceDTO = gatherArticleRepository.findReadSliceDTOByLocationAndStatusAndSort(
                sidoList, sggList, emdList, status, sort, pageable);

        // 모집글 리스트 DTO 생성 및 반환
        return GatherArticleResponse.ReadListDTO.builder()
                .posts(readSliceDTO.getContent())
                .last(readSliceDTO.isLast())
                .build();
    }
}
