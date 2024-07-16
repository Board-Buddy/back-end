package sumcoda.boardbuddy.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sumcoda.boardbuddy.dto.MemberRequest;
import sumcoda.boardbuddy.dto.MemberResponse;
import sumcoda.boardbuddy.service.MemberService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    /**
     * 아이디 중복 확인 요청
     *
     * @param verifyUsernameDuplicationDTO 사용자가 입력한 아이디
     **/
    @PostMapping(value = "/api/auth/check-identifier")
    public ResponseEntity<Map<String, Object>> verifyUsernameDuplication(
            @RequestBody MemberRequest.VerifyUsernameDuplicationDTO verifyUsernameDuplicationDTO) {
        log.info("verify username duplication is working");

        Map<String, Object> response = new HashMap<>();

        Boolean isNotDuplicate = memberService.verifyUsernameDuplication(verifyUsernameDuplicationDTO);

        response.put("data", isNotDuplicate);

        response.put("message", "사용가능한 아이디 입니다.");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 닉네임 중복 확인 요청
     *
     * @param verifyNicknameDuplicationDTO 사용자가 입력한 닉네임
     **/
    @PostMapping(value = "/api/auth/check-nickname")
    public ResponseEntity<Map<String, Object>> verifyNicknameDuplication(
            @RequestBody MemberRequest.VerifyNicknameDuplicationDTO verifyNicknameDuplicationDTO) {
        log.info("verify nickname duplication is working");

        Map<String, Object> response = new HashMap<>();

        Boolean isNotDuplicate = memberService.verifyNicknameDuplication(verifyNicknameDuplicationDTO);

        response.put("data", isNotDuplicate);

        response.put("message", "사용가능한 닉네임 입니다.");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 회원가입 요청 캐치
     *
     * @param registerDTO 프론트로부터 전달받은 회원가입 정보
     **/
    @PostMapping(value = "/api/auth/register")
    public ResponseEntity<?> register(@RequestBody MemberRequest.RegisterDTO registerDTO) {
        log.info("register is working");

        Map<String, Object> response = new HashMap<>();

        Long memberId = memberService.registerMember(registerDTO);

        response.put("data", memberId);

        response.put("message", "회원가입이 완료되었습니다.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 내가 작성한 모집글 조회 요청
     *
     * @param userDetails 현재 인증된 사용자 정보
     * @return 작성한 게시글 정보
     **/
    @GetMapping(value = "/api/my/gatherArticles")
    public ResponseEntity<?> getMyGatherArticles (@AuthenticationPrincipal UserDetails userDetails) {
        log.info("get gather articles is working");

        Map<String, Object> response = new HashMap<>();

        String username = userDetails.getUsername();

        List<MemberResponse.GatherArticleDTO> gatherArticles = memberService.getMyGatherArticles(username);

        response.put("data", Map.of("posts", gatherArticles));

        response.put("message", "내 모집글이 성공적으로 조회되었습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
