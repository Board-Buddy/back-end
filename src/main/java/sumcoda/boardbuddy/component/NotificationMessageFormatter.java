package sumcoda.boardbuddy.component;

import org.springframework.stereotype.Component;

@Component
public class NotificationMessageFormatter {

    public String formatApplyParticipationMessage(String appliedNickname, String gatherArticleTitle) {
        return String.format("%s 님이 '%s'에 참가 신청을 했습니다.", appliedNickname, formatTitle(gatherArticleTitle));
    }

    public String formatApproveParticipationMessage(String gatherArticleTitle) {
        return String.format("'%s'의 참가 신청이 승인되었습니다.", formatTitle(gatherArticleTitle));
    }

    public String formatRejectParticipationMessage(String gatherArticleTitle) {
        return String.format("'%s'의 참가 신청이 거절되었습니다.", formatTitle(gatherArticleTitle));
    }

    public String formatCancelParticipationMessage(String canceledNickname, String gatherArticleTitle) {
        return String.format("%s 님이 '%s'의 참가 신청을 취소했습니다.", canceledNickname, formatTitle(gatherArticleTitle));
    }

    public String formatReviewRequestMessage(String gatherArticleTitle) {
        return String.format("모집글 '%s'에 대한 리뷰를 남겨주세요!", formatTitle(gatherArticleTitle));
    }

    public String formatWriteCommentMessage(String writtenNickname,  String gatherArticleTitle) {
        return String.format("%s 님이 '%s'에 댓글을 달았습니다.", writtenNickname, formatTitle(gatherArticleTitle));
    }

    public String formatReplyCommentMessage(String writtenNickname,  String gatherArticleTitle) {
        return String.format("%s 님이 '%s'에 대댓글을 달았습니다.", writtenNickname, formatTitle(gatherArticleTitle));
    }

    private String formatTitle(String title) {
        return title.length() > 9 ? title.substring(0, 9) + "..." : title;
    }
}
