package sumcoda.boardbuddy.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sumcoda.boardbuddy.dto.MemberResponse;
import sumcoda.boardbuddy.enumerate.GatherArticleRole;

import java.util.List;

import static sumcoda.boardbuddy.entity.QGatherArticle.gatherArticle;
import static sumcoda.boardbuddy.entity.QMember.member;
import static sumcoda.boardbuddy.entity.QMemberGatherArticle.memberGatherArticle;

@Slf4j
@RequiredArgsConstructor
public class GatherArticleRepositoryCustomImpl implements sumcoda.boardbuddy.repository.GatherArticleRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MemberResponse.GatherArticleDTO> findGatherArticleDTOByUsername(String username) {
        return jpaQueryFactory.select(Projections.fields(MemberResponse.GatherArticleDTO.class,
                        gatherArticle.id,
                        gatherArticle.title,
                        gatherArticle.description,
                        gatherArticle.meetingLocation,
                        gatherArticle.maxParticipants,
                        gatherArticle.currentParticipants,
                        gatherArticle.meetingDate,
                        gatherArticle.meetingEndDate,
                        gatherArticle.createdAt,
                        gatherArticle.status))
                .from(member)
                .join(member.memberGatherArticles, memberGatherArticle)
                .join(memberGatherArticle.gatherArticle, gatherArticle)
                .where(member.username.eq(username)
                        .and(memberGatherArticle.gatherArticleRole.eq(GatherArticleRole.AUTHOR)))
                .fetch();
    }
}

