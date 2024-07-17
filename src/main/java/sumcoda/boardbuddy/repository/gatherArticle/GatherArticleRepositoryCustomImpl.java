package sumcoda.boardbuddy.repository.gatherArticle;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sumcoda.boardbuddy.enumerate.GatherArticleRole;

import java.util.List;

import static sumcoda.boardbuddy.dto.GatherArticleResponse.*;
import static sumcoda.boardbuddy.entity.QGatherArticle.gatherArticle;
import static sumcoda.boardbuddy.entity.QMember.member;
import static sumcoda.boardbuddy.entity.QMemberGatherArticle.memberGatherArticle;

@Slf4j
@RequiredArgsConstructor
public class GatherArticleRepositoryCustomImpl implements GatherArticleRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<GatherArticleInfosDTO> findGatherArticleInfosByUsername(String username) {
        return jpaQueryFactory.select(Projections.fields(GatherArticleInfosDTO.class,
                        gatherArticle.id,
                        gatherArticle.title,
                        gatherArticle.description,
                        gatherArticle.meetingLocation,
                        gatherArticle.maxParticipants,
                        gatherArticle.currentParticipants,
                        gatherArticle.startDateTime,
                        gatherArticle.endDateTime,
                        gatherArticle.createdAt,
                        gatherArticle.status))
                .from(member)
                .join(member.memberGatherArticles, memberGatherArticle)
                .join(memberGatherArticle.gatherArticle, gatherArticle)
                .where(member.username.eq(username)
                        .and(memberGatherArticle.gatherArticleRole.eq(GatherArticleRole.AUTHOR)))
                .fetch();
    }

    @Override
    public List<GatherArticleInfosDTO> findParticipationsByUsername(String username) {
        return jpaQueryFactory.select(Projections.fields(GatherArticleInfosDTO.class,
                        gatherArticle.id,
                        gatherArticle.title,
                        gatherArticle.description,
                        gatherArticle.meetingLocation,
                        gatherArticle.maxParticipants,
                        gatherArticle.currentParticipants,
                        gatherArticle.startDateTime,
                        gatherArticle.endDateTime,
                        gatherArticle.createdAt,
                        gatherArticle.status))
                .from(member)
                .join(member.memberGatherArticles, memberGatherArticle)
                .join(memberGatherArticle.gatherArticle, gatherArticle)
                .where(member.username.eq(username)
                        .and(memberGatherArticle.gatherArticleRole.eq(GatherArticleRole.PARTICIPANT))
                        .and(memberGatherArticle.isPermit.eq(true)))
                .fetch();
    }
}
