package sumcoda.boardbuddy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import sumcoda.boardbuddy.component.NotificationMessageFormatter;
import sumcoda.boardbuddy.component.SseEmitterManager;
import sumcoda.boardbuddy.dto.CommentResponse;
import sumcoda.boardbuddy.dto.GatherArticleResponse;
import sumcoda.boardbuddy.dto.MemberResponse;
import sumcoda.boardbuddy.dto.NotificationResponse;
import sumcoda.boardbuddy.entity.Member;
import sumcoda.boardbuddy.entity.Notification;
import sumcoda.boardbuddy.enumerate.EventName;
import sumcoda.boardbuddy.exception.gatherArticle.GatherArticleNotFoundException;
import sumcoda.boardbuddy.exception.member.MemberNotFoundException;
import sumcoda.boardbuddy.exception.member.MemberRetrievalException;
import sumcoda.boardbuddy.repository.member.MemberRepository;
import sumcoda.boardbuddy.repository.comment.CommentRepository;
import sumcoda.boardbuddy.repository.notification.NotificationRepository;
import sumcoda.boardbuddy.repository.gatherArticle.GatherArticleRepository;
import sumcoda.boardbuddy.repository.memberGatherArticle.MemberGatherArticleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificationService {

    private final MemberRepository memberRepository;

    private final GatherArticleRepository gatherArticleRepository;

    private final MemberGatherArticleRepository memberGatherArticleRepository;

    private final NotificationRepository notificationRepository;

    private final CommentRepository commentRepository;

    private final SseEmitterManager sseEmitterManager;

    private final NotificationMessageFormatter notificationMessageFormatter;

    /**
     * 유저 로그인 시 SSE Emitter 구독 요청 캐치
     *
     * @param username 로그인 사용자 아이디
     **/
    @Transactional
    public SseEmitter subscribe(String username) {
        return sseEmitterManager.createEmitter(username);
    }

    /**
     * 참가 신청 시 모집글 작성자에게 알림 보내기
     *
     * @param gatherArticleId 해당 모집글 Id
     * @param appliedUsername 참가 신청한 유저 아이디
     **/
    @Transactional
    public void notifyApplyParticipation(Long gatherArticleId, String appliedUsername) {
        // 모집글 작성자의 유저 아이디를 조회
        MemberResponse.UserNameDTO authorUsernameDTO = memberGatherArticleRepository.findAuthorUsernameByGatherArticleId(gatherArticleId)
                .orElseThrow(() -> new MemberRetrievalException("서버 문제로 해당 모집글의 작성자를 찾을 수 없습니다. 관리자에게 문의하세요."));

        String authorUsername = authorUsernameDTO.getUsername();

        // 참가 신청 메시지를 포맷하여 생성
        String message = notificationMessageFormatter.formatApplyParticipationMessage(
                getNickname(appliedUsername),
                getTitle(gatherArticleId));

        saveNotification(authorUsername, message, EventName.APPLY_PARTICIPATION);
    }

    /**
     * 참가 신청 승인 시 신청한 유저에게 알림 보내기
     *
     * @param gatherArticleId 해당 모집글 Id
     * @param appliedNickname 참가 신청한 유저 닉네임
     **/
    @Transactional
    public void notifyApproveParticipation(String appliedNickname, Long gatherArticleId) {
        // 참가 신청한 유저의 닉네임으로 유저 아이디 조회
        String receiverUsername = getUsername(appliedNickname);

        // 참가 신청 승인 메시지를 포맷하여 생성
        String message = notificationMessageFormatter.formatApproveParticipationMessage(
                getTitle(gatherArticleId)
        );

        saveNotification(receiverUsername, message, EventName.APPROVE_PARTICIPATION);
    }

    /**
     * 참가 신청 거절 시 신청한 유저에게 알림 보내기
     *
     * @param gatherArticleId 해당 모집글 Id
     * @param appliedNickname 참가 신청한 유저 닉네임
     **/
    @Transactional
    public void notifyRejectParticipation(String appliedNickname, Long gatherArticleId) {
        // 참가 신청한 유저의 닉네임으로 유저 아이디 조회
        String receiverUsername = getUsername(appliedNickname);

        // 참가 신청 거절 메시지를 포맷하여 생성
        String message = notificationMessageFormatter.formatRejectParticipationMessage(
                getTitle(gatherArticleId)
        );

        saveNotification(receiverUsername, message, EventName.REJECT_PARTICIPATION);
    }

    /**
     * 참가 신청 취소 시 모집글 작성자에게 알림 보내기
     *
     * @param gatherArticleId 해당 모집글 Id
     * @param canceledUsername 참가 신청 취소한 유저 아이디
     **/
    @Transactional
    public void notifyCancelParticipation(Long gatherArticleId, String canceledUsername) {
        // 모집글 작성자의 유저 아이디를 조회
        MemberResponse.UserNameDTO authorUsernameDTO = memberGatherArticleRepository.findAuthorUsernameByGatherArticleId(gatherArticleId)
                .orElseThrow(() -> new MemberRetrievalException("서버 문제로 해당 모집글의 작성자를 찾을 수 없습니다. 관리자에게 문의하세요."));

        String authorUsername = authorUsernameDTO.getUsername();

        // 참가 신청 취소 메시지를 포맷하여 생성
        String message = notificationMessageFormatter.formatCancelParticipationMessage(
                getNickname(canceledUsername),
                getTitle(gatherArticleId)
        );

        saveNotification(authorUsername, message, EventName.CANCEL_PARTICIPATION);
    }

    /**
     * 모집글 상태가 completed로 변경되면 모든 참가자에게 리뷰 요청 알림 보내기
     *
     * @param gatherArticleId 해당 모집글 Id
     **/
    @Transactional
    public void notifyReviewRequest(Long gatherArticleId) {
        // 리뷰 요청 메시지를 포맷하여 생성
        String message = notificationMessageFormatter.formatReviewRequestMessage(
                getTitle(gatherArticleId)
        );

        // 모든 참가자들의 아이디 조회
        List<MemberResponse.UserNameDTO> participants = memberGatherArticleRepository.findParticipantsByGatherArticleId(gatherArticleId);

        // 모든 참가자에게 알림 전송
        participants.forEach(userNameDTO -> saveNotification(userNameDTO.getUsername(), message, EventName.REVIEW_REQUEST));
    }

    /**
     * 모집글에 댓글이 달리면 모집글 작성자에게, 대댓글이 달리면 원댓글 작성자에게 알림 보내기
     *
     * @param gatherArticleId 해당 모집글 Id
     **/
    @Transactional
    public void notifyWriteComment(Long gatherArticleId, Long parentId, String writtenUsername) {

        if (parentId == null) {
            // 모집글 작성자의 유저 아이디를 조회
            MemberResponse.UserNameDTO authorUsernameDTO = memberGatherArticleRepository.findAuthorUsernameByGatherArticleId(gatherArticleId)
                    .orElseThrow(() -> new MemberRetrievalException("서버 문제로 해당 모집글의 작성자를 찾을 수 없습니다. 관리자에게 문의하세요."));

            String authorUsername = authorUsernameDTO.getUsername();

            // 리뷰 요청 메시지를 포맷하여 생성
            String message = notificationMessageFormatter.formatWriteCommentMessage(
                    getNickname(writtenUsername),
                    getTitle(gatherArticleId)
            );

            saveNotification(authorUsername, message, EventName.WRITE_COMMENT);
        } else {
            // 원댓글 작성자의 유저 아이디를 조회
            Optional<CommentResponse.AuthorUsernameDTO> authorUsernameDTO = commentRepository.findCommentAuthorByCommentId(parentId);

            String authorUsername = authorUsernameDTO.get().getUsername();

            // 리뷰 요청 메시지를 포맷하여 생성
            String message = notificationMessageFormatter.formatReplyCommentMessage(
                    getNickname(writtenUsername),
                    getTitle(gatherArticleId)
            );

            saveNotification(authorUsername, message, EventName.WRITE_COMMENT);
        }
    }

    /**
     * 알림 생성 시 DB에 저장하는 메서드
     *
     * @param username 알림을 받는 유저의 아이디
     * @param message 알림 메세지
     * @param eventName 알림 이벤트 이름
     **/
    public void saveNotification(String username, String message, EventName eventName) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        Notification notification = Notification.buildNotification(message, LocalDateTime.now(), member);
        notificationRepository.save(notification);

        sseEmitterManager.sendNotification(username, message, eventName);
    }

    /**
     * 유저의 알림을 조회하여 최신순으로 반환하는 메서드
     *
     * @param username 알림을 조회할 유저의 아이디
     * @return NotificationResponse 알림 응답 DTO
     **/
    public List<NotificationResponse.NotificationDTO> getNotifications(String username) {
        // 유저 검증
        if (Boolean.FALSE.equals(memberRepository.existsByUsername(username))) {
            throw new MemberNotFoundException("해당 유저를 찾을 수 없습니다.");
        }

        //DB에서 해당 유저의 알림을 최신순으로 조회
        return notificationRepository.findNotificationByMemberUsername(username).stream()
                .map(notification -> NotificationResponse.NotificationDTO.builder()
                        .message(notification.getMessage())
                        .createdAt(notification.getCreatedAt())
                        .build())
                .toList();
    }

    /**
     * 모집글 Id로 모집글 제목 조회
     *
     * @param gatherArticleId 해당 모집글 Id
     * @return 모집글 제목
     **/
    private String getTitle(Long gatherArticleId) {
        // 모집글 조회
        GatherArticleResponse.TitleDTO gatherArticle = gatherArticleRepository.findTitleDTOById(gatherArticleId)
                .orElseThrow(() -> new GatherArticleNotFoundException("존재하지 않는 모집글입니다."));

        return gatherArticle.getTitle();
    }

    /**
     * 유저 아이디로 조회하여 유저 닉네임을 반환하는 메서드
     *
     * @param username 유저 아이디
     * @return nickname 유저 닉네임
     **/
    private String getNickname(String username) {

        // 유저 아이디로 유저 닉네임 조회
        MemberResponse.NicknameDTO nicknameDTO = memberRepository.findNicknameDTOByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("서버 문제로 해당 유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        return nicknameDTO.getNickname();
    }

    /**
     * 유저 닉네임으로 조회하여 유저 아이디를 반환하는 메서드
     *
     * @param nickname 유저 닉네임
     * @return username 유저 아이디
     **/
    private String getUsername(String nickname) {

        // 유저 닉네임으로 유저 아이디 조회
        MemberResponse.UserNameDTO userNameDTO = memberRepository.findUsernameDTOByNickname(nickname)
                .orElseThrow(() -> new MemberRetrievalException("서버 문제로 해당 유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        return userNameDTO.getUsername();
    }
}
