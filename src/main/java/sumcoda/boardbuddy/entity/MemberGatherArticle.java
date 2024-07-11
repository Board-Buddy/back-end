package sumcoda.boardbuddy.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sumcoda.boardbuddy.enumerate.GatherArticleRole;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MemberGatherArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 해당 유저가 방에 참석한 시점
    private LocalDateTime joinedAt;

    // 모집글 참여자의 role
    @Column(nullable = false)
    private Boolean isPermit;

    // 모집글 참여자의 권한을 나타내기위한 role
    // ex) AUTHOR, PARTICIPANT
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GatherArticleRole gatherArticleRole;

    // 양방향 연관관계
    // 연관관계 주인
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 양방향 연관관계
    // 연관관계 주인
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gather_article_id")
    private GatherArticle gatherArticle;

    @Builder
    public MemberGatherArticle(LocalDateTime joinedAt, Boolean isPermit, GatherArticleRole gatherArticleRole, Member member, GatherArticle gatherArticle) {
        this.joinedAt = joinedAt;
        this.isPermit = isPermit;
        this.gatherArticleRole = gatherArticleRole;
        this.assignMember(member);
        this.assignGatherArticle(gatherArticle);
    }

    // 직접 빌더 패턴의 생성자를 활용하지 말고 해당 메서드를 활용하여 엔티티 생성
    public static MemberGatherArticle createMemberGatherArticle(LocalDateTime joinedAt, Boolean isPermit, GatherArticleRole gatherArticleRole, Member member, GatherArticle gatherArticle) {
        return MemberGatherArticle.builder()
                .joinedAt(joinedAt)
                .isPermit(isPermit)
                .gatherArticleRole(gatherArticleRole)
                .member(member)
                .gatherArticle(gatherArticle)
                .build();
    }

    // MemberGatherArticle N <-> 1 Member
    // 양방향 연관관계 편의 메서드
    public void assignMember(Member member) {
        if (this.member != null) {
            this.member.getMemberGatherArticles().remove(this);
        }
        this.member = member;

        if (!member.getMemberGatherArticles().contains(this)) {
            member.addMemberGatherArticle(this);
        }
    }

    // MemberGatherArticle N <-> 1 GatherArticle
    // 양방향 연관관계 편의 메서드
    public void assignGatherArticle(GatherArticle gatherArticle) {
        if (this.gatherArticle != null) {
            this.gatherArticle.getMemberGatherArticles().remove(this);
        }
        this.gatherArticle = gatherArticle;

        if (!gatherArticle.getMemberGatherArticles().contains(this)) {
            gatherArticle.addMemberGatherArticle(this);
        }
    }


}