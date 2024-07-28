package sumcoda.boardbuddy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sumcoda.boardbuddy.dto.ParticipationApplicationResponse;
import sumcoda.boardbuddy.dto.common.ApiResponse;
import sumcoda.boardbuddy.service.ParticipationApplicationService;

import java.util.List;
import java.util.Map;

import static sumcoda.boardbuddy.builder.ResponseBuilder.*;

@RestController
@RequiredArgsConstructor
public class ParticipationApplicationController {

    private final ParticipationApplicationService participationApplicationService;

    /**
     * 모집글 참가 신청 요청
     *
     * @param gatherArticleId 모집글 ID
     * @param username 참가신청 요청을 보내는 사용자 아이디
     **/
    @PostMapping("/api/gatherArticles/{gatherArticleId}/participation")
    public ResponseEntity<ApiResponse<Void>> applyParticipation(@PathVariable Long gatherArticleId, @RequestAttribute String username) {
        participationApplicationService.applyParticipation(gatherArticleId, username);

        return buildSuccessResponseWithoutData("해당 모집글에 참가 신청이 완료되었습니다.", HttpStatus.OK);
    }

    /**
     * 모집글 참가 신청 승인 요청
     *
     * @param gatherArticleId 모집글 Id
     * @param participationApplicationId 참가 신청 Id
     * @param username 승인 요청을 보내는 모집글 작성자 아이디
     * @param applicantNickname 참가 신청을 했던 사용자의 아이디
     **/
    @PutMapping("/api/gatherArticles/{gatherArticleId}/participation/{participationApplicationId}/approval")
    public ResponseEntity<ApiResponse<Void>> approveParticipationApplication(@PathVariable Long gatherArticleId, @PathVariable Long participationApplicationId, @RequestAttribute String username, @RequestParam String applicantNickname) {
        participationApplicationService.approveParticipationApplication(gatherArticleId, participationApplicationId, username);

        return buildSuccessResponseWithoutData(applicantNickname + "님의 참가 신청을 승인 했습니다.", HttpStatus.OK);
    }

    /**
     * 모집글 참가 신청 거절 요청
     *
     * @param gatherArticleId 모집글 Id
     * @param participationApplicationId 참가 신청 Id
     * @param username 거절 요청을 보내는 모집글 작성자 아이디
     * @param applicantNickname 참가 신청을 했던 사용자의 아이디
     **/
    @PutMapping("/api/gatherArticles/{gatherArticleId}/participation/{participationApplicationId}/rejection")
    public ResponseEntity<ApiResponse<Void>> rejectParticipationApplication(@PathVariable Long gatherArticleId, @PathVariable Long participationApplicationId, @RequestAttribute String username, @RequestParam String applicantNickname) {
        participationApplicationService.rejectParticipationApplication(gatherArticleId, participationApplicationId, username);

        return buildSuccessResponseWithoutData(applicantNickname + "님의 참가 신청을 거절 했습니다.", HttpStatus.OK);
    }

    /**
     * 모집글 참가 신청 취소 요청
     *
     * @param gatherArticleId 모집글 Id
     * @param username 참가신청을 취소하는 사용자 아이디
     **/
    @PutMapping("/api/gatherArticles/{gatherArticleId}/participation")
    public ResponseEntity<ApiResponse<Void>> cancelParticipationApplication(@PathVariable Long gatherArticleId, @RequestAttribute String username) {
        participationApplicationService.cancelParticipationApplication(gatherArticleId, username);

        return buildSuccessResponseWithoutData("해당 모집글의 참가 신청을 취소했습니다.", HttpStatus.OK);
    }

    /**
     * 모집글 참가 신청 목록 조회 요청
     *
     * @param gatherArticleId 모집글 Id
     * @param username 참가 신청 목록 조회 요청을 보내는 모집글 작성자 아이디
     * @return 모집글 참가 신청중인 사용자 목록
     **/
    @GetMapping("/api/gatherArticles/{gatherArticleId}/participation")
    public ResponseEntity<ApiResponse<Map<String, List<ParticipationApplicationResponse.InfoDTO>>>> getParticipationAppliedMemberList(@PathVariable Long gatherArticleId, @RequestAttribute String username) {
        List<ParticipationApplicationResponse.InfoDTO> participationAppliedMemberList = participationApplicationService.getParticipationAppliedMemberList(gatherArticleId, username);
        return buildSuccessResponseWithPairKeyData("participationAppliedMemberList", participationAppliedMemberList, "해당 모집글의 참가 신청 목록을 성공적으로 조회했습니다.", HttpStatus.OK);
    }
}
