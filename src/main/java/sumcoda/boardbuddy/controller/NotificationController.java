package sumcoda.boardbuddy.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import sumcoda.boardbuddy.dto.NotificationResponse;
import sumcoda.boardbuddy.dto.common.ApiResponse;
import sumcoda.boardbuddy.service.NotificationService;

import java.util.List;
import java.util.Map;

import static sumcoda.boardbuddy.builder.ResponseBuilder.buildSuccessResponseWithPairKeyData;

@RestController
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * SSE Emitter 구독 요청
     *
//     * @param username 유저 아이디
     **/
    @GetMapping(value = "/api/notifications/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(
//            @RequestAttribute String username
    ) {
        log.info("User {} subscribed for notifications", "test");
        SseEmitter sseEmitter = notificationService.subscribe("test");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache");
        headers.add("X-Accel-Buffering", "no");
        return new ResponseEntity<>(sseEmitter, headers, HttpStatus.OK);
    }

    /**
     * 알림 목록 조회 요청
     *
//     * @param username 유저 아이디
     * @return 알림 목록 조회 성공 시 약속된 SuccessResponse 반환
     **/
    @GetMapping(value = "/api/notifications")
    public ResponseEntity<ApiResponse<Map<String, List<NotificationResponse.NotificationDTO>>>> getNotifications(
//            @RequestAttribute String username
    ) {

        List<NotificationResponse.NotificationDTO> notificationDTOs = notificationService.getNotifications("test");

        return buildSuccessResponseWithPairKeyData("notifications", notificationDTOs, "알림이 조회되었습니다.", HttpStatus.OK);
    }
}