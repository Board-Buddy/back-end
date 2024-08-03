package sumcoda.boardbuddy.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import sumcoda.boardbuddy.enumerate.EventName;
import sumcoda.boardbuddy.exception.sseEmitter.SseEmitterSendErrorException;
import sumcoda.boardbuddy.exception.sseEmitter.SseEmitterSubscribeErrorException;
import sumcoda.boardbuddy.repository.sseEmitter.SseEmitterRepository;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterManager {

    // SSE Emitter와 이벤트 캐시를 위한 저장소
    private static final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 기본 타임아웃 시간 설정
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    @Autowired
    private SseEmitterRepository sseEmitterRepository;

    public SseEmitter createEmitter(String username) {
        // 매 연결마다 고유 Id 부여
        String emitterId = username + "_" + System.currentTimeMillis();

        // SseEmitter 인스턴스 생성
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        // 503 Service Unavailable 방지용 dummy 이벤트 전송
        try {
            emitter.send(SseEmitter.event().name("connect"));
        } catch (IOException e) {
            throw new SseEmitterSubscribeErrorException("서버 문제로 알림 구독을 실패했습니다. 관리자에게 문의하세요.");
        }

        emitters.put(username, emitter);

        // 알림 전송 완료, 타임 아웃 시 emitter 삭제 처리
        emitter.onCompletion(() -> sseEmitterRepository.deleteEmitterById(emitterId));
        emitter.onTimeout(() -> sseEmitterRepository.deleteEmitterById(emitterId));

        return emitter;
    }

    /**
     * 유저가 SSE Emitter에 등록되어 있는지 확인하고 DB에 저장 후 알림을 보내는 메서드
     *
     * @param username 알림을 받는 유저의 아이디
     * @param message 알림 메세지
     * @param eventName 알림 이벤트 이름
     **/
    public void sendNotification(String username, String message, EventName eventName) {
        // 작성자가 SSE 이벤트 수신을 위해 등록되어 있는지 확인
        if (emitters.containsKey(username)) {

            // 작성자의 SSE Emitter 객체를 가져옴
            SseEmitter sseEmitterReceiver = emitters.get(username);

            try {
                // 알림 메세지를 SSE Emitter를 통해 전송
                sseEmitterReceiver.send(SseEmitter.event().name(eventName.toString()).data(message));

                // 이벤트 캐시에 알림 메세지를 저장
                String eventCacheId = username + "_" + System.currentTimeMillis();
                sseEmitterRepository.saveEventCache(eventCacheId, message);

            } catch (SseEmitterSendErrorException | IOException e) {
                // 전송 중 오류 발생 시, 작성자의 SSE Emitter를 제거
                emitters.remove(username);
            }
        }
    }
}
