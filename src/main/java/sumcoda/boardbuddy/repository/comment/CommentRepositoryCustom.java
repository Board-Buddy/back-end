package sumcoda.boardbuddy.repository.comment;

import sumcoda.boardbuddy.dto.CommentResponse;
import sumcoda.boardbuddy.entity.Comment;
import sumcoda.boardbuddy.entity.Member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommentRepositoryCustom {

  long countCommentsByMember(Member member, LocalDateTime startOfLastMonth, LocalDateTime endOfLastMonth);

  List<CommentResponse.CommentDTO> findCommentDTOsByGatherArticleId(Long gatherArticleId);

  Optional<Comment> findCommentByCommentId(Long commentId);
}
