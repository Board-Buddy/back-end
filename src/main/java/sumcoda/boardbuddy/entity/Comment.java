package sumcoda.boardbuddy.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    // 연관관계 주인
    // 양방향 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 연관관계 주인
    // 양방향 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gather_article_id")
    private GatherArticle gatherArticle;

    // 자기 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent")
    private List<Comment> children = new ArrayList<>();

    @Builder
    public Comment(String content, Member member, GatherArticle gatherArticle, Comment parent) {
        this.content = content;
        this.assignMember(member);
        this.assignGatherArticle(gatherArticle);
        this.assignParent(parent);
    }

    // 직접 빌더 패턴의 생성자를 활용하지 말고 해당 메서드를 활용하여 엔티티 생성
    public static Comment buildComment(String content, Member member, GatherArticle gatherArticle, Comment parent) {
        return Comment.builder()
                .content(content)
                .member(member)
                .gatherArticle(gatherArticle)
                .parent(parent)
                .build();
    }

    // Comment N <-> 1 Member
    // 양방향 연관관계 편의 메서드
    public void assignMember(Member member) {
        if (this.member != null) {
            this.member.getComments().remove(this);
        }
        this.member = member;

        if (!member.getComments().contains(this)) {
            member.addComment(this);
        }
    }

    // Comment N <-> 1 GatherArticle
    // 양방향 연관관계 편의 메서드
    public void assignGatherArticle(GatherArticle gatherArticle) {
        if (this.gatherArticle != null) {
            this.gatherArticle.getComments().remove(this);
        }
        this.gatherArticle = gatherArticle;

        if (!gatherArticle.getComments().contains(this)) {
            gatherArticle.addComment(this);
        }
    }

    // Comment(parent) 1 <-> N Comment(child)
    // 양방향 연관관계 편의 메서드
    public void assignParent(Comment parent) {
        if (this.parent != null) {
            this.parent.getChildren().remove(this);
        }
        this.parent = parent;

        if (parent != null && !parent.getChildren().contains(this)) {
            parent.addChild(this);
        }
    }

    // Comment(child) N <-> 1 Comment(parent)
    // 양방향 연관관계 편의 메서드
    public void addChild(Comment child) {
        this.children.add(child);

        if (child.getParent() != this) {
            child.assignParent(this);
        }
    }

    // 댓글 내용 수정 메서드
    public void assignContent(String content) {
        this.content = content;
    }
}
