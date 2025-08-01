package sumcoda.boardbuddy.repository.participationApplication;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import sumcoda.boardbuddy.dto.fetch.ParticipationApplicationInfoProjection;
import sumcoda.boardbuddy.entity.ParticipationApplication;
import sumcoda.boardbuddy.enumerate.ParticipationApplicationStatus;

import java.util.List;
import java.util.Optional;

import static sumcoda.boardbuddy.entity.QGatherArticle.gatherArticle;
import static sumcoda.boardbuddy.entity.QMember.member;
import static sumcoda.boardbuddy.entity.QMemberGatherArticle.memberGatherArticle;
import static sumcoda.boardbuddy.entity.QParticipationApplication.*;
import static sumcoda.boardbuddy.entity.QProfileImage.profileImage;

@RequiredArgsConstructor
public class ParticipationApplicationRepositoryCustomImpl implements ParticipationApplicationRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Boolean existsByGatherArticleIdAndUsername(Long gatherArticleId, String username) {
        return jpaQueryFactory
                .selectOne()
                .from(participationApplication)
                .leftJoin(participationApplication.memberGatherArticle, memberGatherArticle)
                .leftJoin(memberGatherArticle.gatherArticle, gatherArticle)
                .leftJoin(memberGatherArticle.member, member)
                .where(gatherArticle.id.eq(gatherArticleId)
                        .and(member.username.eq(username)))
                .fetchOne() != null;
    }

    @Override
    public Optional<ParticipationApplication> findByGatherArticleIdAndUsername(Long gatherArticleId, String username) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(participationApplication)
                .leftJoin(participationApplication.memberGatherArticle, memberGatherArticle)
                .leftJoin(memberGatherArticle.gatherArticle, gatherArticle)
                .leftJoin(memberGatherArticle.member, member)
                .where(gatherArticle.id.eq(gatherArticleId).and(member.username.eq(username)))
                .fetchOne());
    }

    @Override
    public List<ParticipationApplicationInfoProjection> findParticipationAppliedMemberByGatherArticleId(Long gatherArticleId) {
        return jpaQueryFactory
                .select(Projections.constructor(ParticipationApplicationInfoProjection.class,
                        participationApplication.id,
                        member.nickname,
                        profileImage.s3SavedObjectName
                ))
                .from(participationApplication)
                .leftJoin(participationApplication.memberGatherArticle, memberGatherArticle)
                .leftJoin(memberGatherArticle.gatherArticle, gatherArticle)
                .leftJoin(memberGatherArticle.member, member)
                .leftJoin(member.profileImage, profileImage)
                .where(gatherArticle.id.eq(gatherArticleId)
                        .and(participationApplication.participationApplicationStatus.eq(ParticipationApplicationStatus.PENDING))
                )
                .fetch();
    }
}
