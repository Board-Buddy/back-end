package sumcoda.boardbuddy.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import sumcoda.boardbuddy.dto.AuthResponse;
import sumcoda.boardbuddy.dto.MemberResponse;
import sumcoda.boardbuddy.entity.Member;
import sumcoda.boardbuddy.enumerate.GatherArticleRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static sumcoda.boardbuddy.entity.QComment.*;
import static sumcoda.boardbuddy.entity.QMember.*;
import static sumcoda.boardbuddy.entity.QMemberGatherArticle.*;
import static sumcoda.boardbuddy.entity.QProfileImage.*;

@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<AuthResponse.ProfileDTO> findAuthDTOByUsername(String username) {
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.fields(AuthResponse.ProfileDTO.class,
                        member.username,
                        member.password,
                        member.memberRole
                ))
                .from(member)
                .where(member.username.eq(username))
                .fetchOne());
    }

    @Override
    public Optional<MemberResponse.ProfileDTO> findMemberDTOByUsername(String username) {
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.fields(MemberResponse.ProfileDTO.class,
                        member.nickname,
                        member.sido,
                        member.sigu,
                        member.dong,
                        member.phoneNumber,
                        profileImage.profileImageS3SavedURL
                ))
                .from(member)
                .leftJoin(member.profileImage, profileImage)
                .where(member.username.eq(username))
                .fetchOne());
    }

    @Override
    public List<MemberResponse.RankingsDTO> findTop3RankingMembers() {
        return jpaQueryFactory
                .select(Projections.fields(MemberResponse.RankingsDTO.class,
                        member.nickname,
                        profileImage.profileImageS3SavedURL
                ))
                .from(member)
                .leftJoin(member.profileImage, profileImage)
                .where(member.rank.isNotNull())
                .orderBy(member.rank.asc())
                .limit(3)
                .fetch();
    }

    // 지난 달에 쓴 모집글 갯수 세기
    @Override
    public long countGatherArticlesByMember(Member member, LocalDateTime startOfLastMonth, LocalDateTime endOfLastMonth) {
        return jpaQueryFactory.select(memberGatherArticle.count())
                .from(memberGatherArticle)
                .where(memberGatherArticle.member.eq(member)
                        .and(memberGatherArticle.gatherArticleRole.eq(GatherArticleRole.AUTHOR))
                        .and(memberGatherArticle.gatherArticle.createdAt.between(startOfLastMonth, endOfLastMonth)))
                .fetchOne();
    }

    // 지난 달에 쓴 댓글 갯수 세기
    @Override
    public long countCommentsByMember(Member member, LocalDateTime startOfLastMonth, LocalDateTime endOfLastMonth) {
        return jpaQueryFactory.select(comment.count())
                .from(comment)
                .where(comment.member.eq(member)
                        .and(comment.createdAt.between(startOfLastMonth, endOfLastMonth)))
                .fetchOne();
    }

    // 점수로 정렬
    @Override
    public List<Member> findAllOrderedByRankScore() {
        return jpaQueryFactory.selectFrom(member)
                .orderBy(member.rankScore.desc())
                .fetch();
    }

}
