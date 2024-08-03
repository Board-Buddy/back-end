package sumcoda.boardbuddy.repository.comment;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import sumcoda.boardbuddy.dto.CommentResponse;
import sumcoda.boardbuddy.entity.Comment;
import sumcoda.boardbuddy.entity.Member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static sumcoda.boardbuddy.entity.QComment.*;
import static sumcoda.boardbuddy.entity.QMember.member;

@RequiredArgsConstructor
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    // 지난 달에 쓴 댓글 갯수 세기
    @Override
    public long countCommentsByMember(Member member, LocalDateTime startOfLastMonth, LocalDateTime endOfLastMonth) {
        return jpaQueryFactory.select(comment.count())
                .from(comment)
                .where(comment.member.eq(member)
                        .and(comment.createdAt.between(startOfLastMonth, endOfLastMonth)))
                .fetchOne();
    }

    @Override
    public List<CommentResponse.InfoDTO> findCommentDTOsByGatherArticleId(Long gatherArticleId) {

        List<Comment> comments = jpaQueryFactory.selectFrom(comment)
                .join(comment.member, member)
                .fetchJoin()
                .leftJoin(comment.children)
                .fetchJoin()
                .where(comment.gatherArticle.id.eq(gatherArticleId))
                .fetch();

        return comments.stream()
                .filter(c -> c.getParent() == null)
                .map(CommentResponse.InfoDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Comment> findCommentByCommentId(Long commentId) {
        return Optional.ofNullable(jpaQueryFactory.selectFrom(comment)
                .join(comment.member, member)
                .fetchJoin()
                .leftJoin(comment.children)
                .fetchJoin()
                .where(comment.id.eq(commentId))
                .fetchOne());
    }

  @Override
  public Optional<CommentResponse.AuthorUsernameDTO> findCommentAuthorByCommentId(Long commentId) {
    return Optional.ofNullable(jpaQueryFactory.
            select(Projections.fields(CommentResponse.AuthorUsernameDTO.class,
                    member.username))
            .from(comment)
            .join(comment.member, member)
            .where(comment.id.eq(commentId))
            .fetchOne());
  }
}
