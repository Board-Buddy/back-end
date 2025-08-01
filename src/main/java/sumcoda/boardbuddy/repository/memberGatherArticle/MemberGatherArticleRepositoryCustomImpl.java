package sumcoda.boardbuddy.repository.memberGatherArticle;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import sumcoda.boardbuddy.dto.MemberResponse;
import sumcoda.boardbuddy.dto.fetch.ReviewAuthorProjection;
import sumcoda.boardbuddy.enumerate.MemberGatherArticleRole;

import java.util.List;
import java.util.Optional;

import static sumcoda.boardbuddy.entity.QGatherArticle.gatherArticle;
import static sumcoda.boardbuddy.entity.QMember.member;
import static sumcoda.boardbuddy.entity.QMemberGatherArticle.memberGatherArticle;
import static sumcoda.boardbuddy.entity.QProfileImage.profileImage;
import static sumcoda.boardbuddy.entity.QReview.review;

@RequiredArgsConstructor
public class MemberGatherArticleRepositoryCustomImpl implements MemberGatherArticleRepositoryCustom {
  private final JPAQueryFactory jpaQueryFactory;

  // 모집글의 작성자인지 확인
  @Override
  public boolean isAuthor(Long gatherArticleId, Long memberId) {
    Long count = jpaQueryFactory
            .select(memberGatherArticle.count())
            .from(memberGatherArticle)
            .where(memberGatherArticle.gatherArticle.id.eq(gatherArticleId)
                    .and(memberGatherArticle.member.id.eq(memberId))
                    .and(memberGatherArticle.memberGatherArticleRole.eq(MemberGatherArticleRole.AUTHOR)))
            .fetchOne();

    return count != null && count > 0;
  }

  // 유저와 모집글 사이에 Role이 있는지 확인
  @Override
  public boolean isHasRole(Long gatherArticleId, String username) {
    Long count = jpaQueryFactory
            .select(memberGatherArticle.count())
            .from(memberGatherArticle)
            .join(memberGatherArticle.gatherArticle, gatherArticle)
            .join(memberGatherArticle.member, member)
            .where(gatherArticle.id.eq(gatherArticleId)
                    .and(member.username.eq(username))
                    .and(memberGatherArticle.memberGatherArticleRole.in(MemberGatherArticleRole.AUTHOR, MemberGatherArticleRole.PARTICIPANT)))
            .fetchOne();

    return count != null && count > 0;
  }

  // 모집글의 작성자를 찾는 메서드
  @Override
  public Optional<MemberResponse.UsernameDTO> findAuthorUsernameByGatherArticleId(Long gatherArticleId) {
    return Optional.ofNullable(jpaQueryFactory
            .select(Projections.fields(MemberResponse.UsernameDTO.class,
                    member.username))
            .from(memberGatherArticle)
            .join(memberGatherArticle.member, member)
            .where(memberGatherArticle.gatherArticle.id.eq(gatherArticleId)
                    .and(memberGatherArticle.memberGatherArticleRole.eq(MemberGatherArticleRole.AUTHOR)))
            .fetchOne());
  }

  //모집글의 모든 참가자를 찾는 메서드
  @Override
  public List<MemberResponse.UsernameDTO> findParticipantsByGatherArticleId(Long gatherArticleId) {
    return jpaQueryFactory
            .select(Projections.fields(MemberResponse.UsernameDTO.class,
                    member.username))
            .from(memberGatherArticle)
            .join(memberGatherArticle.member, member)
            .where(memberGatherArticle.gatherArticle.id.eq(gatherArticleId)
                    .and(memberGatherArticle.memberGatherArticleRole.in(
                            MemberGatherArticleRole.PARTICIPANT,
                            MemberGatherArticleRole.AUTHOR)))
            .fetch();
  }

  // 자신을 제외한 모임 참가 유저 리스트를 반환하는 메서드
  @Override
  public List<ReviewAuthorProjection> findReviewerByGatherArticleIdAndUsernameNot(Long gatherArticleId, String excludedUsername) {
      return jpaQueryFactory
              .select(Projections.constructor(ReviewAuthorProjection.class,
                      profileImage.s3SavedObjectName,
                      member.rank,
                      member.nickname,
                      review.hasReviewed
              ))
              .from(memberGatherArticle)
              .join(memberGatherArticle.member, member)
              .leftJoin(member.profileImage, profileImage)
              .leftJoin(review).on(
                      review.reviewer.username.eq(excludedUsername)
                              .and(review.reviewee.eq(member))
                              .and(review.gatherArticle.id.eq(gatherArticleId))
              )
              .where(memberGatherArticle.gatherArticle.id.eq(gatherArticleId)
                      .and(member.username.ne(excludedUsername)))
              .fetch();
  }
}
