package sumcoda.boardbuddy.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sumcoda.boardbuddy.enumerate.MemberType;
import sumcoda.boardbuddy.enumerate.Role;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 일반 로그인 = 아이디
    // 소셜 로그인 = provider + " " + providerId
    // ex) naver asdf1234as93hf8932hf9ah2393hf9ah39fha9shd9fht
    @Column(nullable = false)
    private String username;

    // 비밀번호
    // 일반 로그인 = 회원가입시 직접 설정해야함
    // 소셜 로그인 = 설정하지 않아도됨
    @Column(nullable = false)
    private String password;

    // 애플리케이션에서 활동할 이름
    // 일반 로그인 = 직접 설정 가능
    // 소셜 로그인 = 해당 계정에 이미 등록된 이름으로 설정됨
    @Column(nullable = false)
    private String nickname;

    // 이메일
    // 일반 로그인 = 회원가입시 설정 가능
    // 소셜 로그인 = 해당 계정에 이미 등록된 이메일로 설정됨
    @Column(nullable = false)
    private String email;

    // 핸드폰 번호
    // 일반 로그인 = 회원가입시 설정 가능
    // 소셜 로그인 = 로그인후 마이페이지에서 별도로 설정 필요
    private String phoneNumber;

    // 사용자가 거주하는 oo시, oo도
    // 일반 로그인 = 회원가입시 설정 가능
    // 소셜 로그인 = 로그인후 마이페이지에서 별도로 설정 필요
    private String sido;

    // 사용자가 거주하는 oo시, oo군, oo구
    // 일반 로그인 = 회원가입시 설정 가능
    // 소셜 로그인 = 로그인후 마이페이지에서 별도로 설정 필요
    private String sgg;

    // 사용자가 거주하는 oo읍, oo면, oo동
    // 일반 로그인 = 회원가입시 설정 가능
    // 소셜 로그인 = 로그인후 마이페이지에서 별도로 설정 필요
    private String emd;

    // 사용자 위치를 기준으로 주변 범위를 조절하기 위한 필드
    // 일반 로그인 = 회원가입시 설정 가능
    // 소셜 로그인 = 로그인후 마이페이지에서 별도로 설정 필요
    @Column(nullable = false)
    private Integer radius;

    // 사용자의 버디지수
    // 일반 로그인, 소셜 로그인 별도 설정 필요 없음
    @Column(nullable = false)
    private Double buddyScore;

    // 프로필에서 보여주기 위한 참여 횟수
    // 일반 로그인, 소셜 로그인 별도 설정 필요 없음
    @Column(nullable = false)
    private Integer joinCount;

    // 랭킹 점수 환산을 위한 최고예요 횟수 (매월 1일마다 초기화될 예정)
    // 일반 로그인, 소셜 로그인 별도 설정 필요 없음
    @Column(nullable = false)
    private Integer monthlyExcellentCount;

    // 프로필에서 보여주기 위한 최고예요 횟수
    // 일반 로그인, 소셜 로그인 별도 설정 필요 없음
    @Column(nullable = false)
    private Integer totalExcellentCount;

    // 랭킹 점수 환산을 위한 좋아요 횟수 (매월 1일마다 초기화될 예정)
    // 일반 로그인, 소셜 로그인 별도 설정 필요 없음
    @Column(nullable = false)
    private Integer monthlyGoodCount;

    // 프로필에서 보여주기 위한 좋아요 횟수
    // 일반 로그인, 소셜 로그인 별도 설정 필요 없음
    @Column(nullable = false)
    private Integer totalGoodCount;

    // 랭킹 점수 환산을 위한 별로예요 횟수 (매월 1일마다 초기화될 예정)
    // 일반 로그인, 소셜 로그인 별도 설정 필요 없음
    @Column(nullable = false)
    private Integer monthlyBadCount;

    // 프로필에서 보여주기 위한 별로예요 횟수
    // 일반 로그인, 소셜 로그인 별도 설정 필요 없음
    @Column(nullable = false)
    private Integer totalBadCount;

    // 랭킹 점수 환산을 위한 노쇼 횟수 (매월 1일마다 초기화될 예정)
    // 일반 로그인, 소셜 로그인 별도 설정 필요 없음
    @Column(nullable = false)
    private Integer monthlyNoShowCount;

    // 랭킹 점수 환산을 위한 리뷰 보내기 횟수 (매월 1일마다 초기화될 예정)
    // 일반 로그인, 소셜 로그인 별도 설정 필요 없음
    @Column(nullable = false)
    private Integer monthlySendReviewCount;

    // 자기소개
    // 일반 로그인, 소셜 로그인 마이페이지 설정 필요
    private String description;

    // 저번 달 활동에 대한 랭크 (매월 1일마다 초기화될 예정)
    // 일반 로그인, 소셜 로그인 별도 설정 필요 없음
    private Integer rank;

    // 랭킹 산정을 위한 점수
    private Double rankScore;

    // ex) REGULAR, SOCIAL
    @Column(nullable = false)
    private MemberType memberType;

    // 사용자의 권한을 나타내기위한 role
    // ex) ADMIN, USER
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    // 연관관게 주인
    // 단방향 연관관계
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_image_id")
    private ProfileImage profileImage;

    // 단방향 연관관계
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.REMOVE)
    private List<Notification> notifications = new ArrayList<>();

    // 양방향 연관관계
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.REMOVE)
    private List<MemberGatherArticle> memberGatherArticles = new ArrayList<>();

    // 양방향 연관관계
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.REMOVE)
    private List<Comment> comments = new ArrayList<>();

    // 양방향 연관관계
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberChatRoom> memberChatRooms = new ArrayList<>();

    // 양방향 연관관계
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.REMOVE)
    private List<BadgeImage> badgeImages = new ArrayList<>();

    @Builder
    public Member(String username, String password, String nickname, String email, String phoneNumber, String sido, String sgg, String emd, Integer radius, Double buddyScore, Integer joinCount, Integer monthlyExcellentCount, Integer totalExcellentCount, Integer monthlyGoodCount, Integer totalGoodCount, Integer monthlyBadCount, Integer totalBadCount, Integer monthlyNoShowCount, Integer monthlySendReviewCount, String description, Integer rank, Double rankScore, MemberType memberType, Role role, ProfileImage profileImage) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.sido = sido;
        this.sgg = sgg;
        this.emd = emd;
        this.radius = radius;
        this.buddyScore = buddyScore;
        this.joinCount = joinCount;
        this.monthlyExcellentCount = monthlyExcellentCount;
        this.totalExcellentCount = totalExcellentCount;
        this.monthlyGoodCount = monthlyGoodCount;
        this.totalGoodCount = totalGoodCount;
        this.monthlyBadCount = monthlyBadCount;
        this.totalBadCount = totalBadCount;
        this.monthlyNoShowCount = monthlyNoShowCount;
        this.monthlySendReviewCount = monthlySendReviewCount;
        this.description = description;
        this.rank = rank;
        this.rankScore = rankScore;
        this.memberType = memberType;
        this.role = role;
        this.assignProfileImage(profileImage);
    }

    // 직접 빌더 패턴의 생성자를 활용하지 말고 해당 메서드를 활용하여 엔티티 생성
    public static Member buildMember(String username, String password, String nickname, String email, String phoneNumber, String sido, String sgg, String emd, Integer radius, Double buddyScore, Integer joinCount, Integer monthlyExcellentCount, Integer totalExcellentCount, Integer monthlyGoodCount, Integer totalGoodCount, Integer monthlyBadCount, Integer totalBadCount, Integer monthlyNoShowCount, Integer monthlySendReviewCount, String description, Integer rank, Double rankScore, MemberType memberType, Role role, ProfileImage profileImage) {
        return Member.builder()
                .username(username)
                .password(password)
                .nickname(nickname)
                .email(email)
                .phoneNumber(phoneNumber)
                .sido(sido)
                .sgg(sgg)
                .emd(emd)
                .radius(radius)
                .buddyScore(buddyScore)
                .joinCount(joinCount)
                .monthlyExcellentCount(monthlyExcellentCount)
                .totalExcellentCount(totalExcellentCount)
                .monthlyGoodCount(monthlyGoodCount)
                .totalGoodCount(totalGoodCount)
                .monthlyBadCount(monthlyBadCount)
                .totalBadCount(totalBadCount)
                .monthlyNoShowCount(monthlyNoShowCount)
                .monthlySendReviewCount(monthlySendReviewCount)
                .description(description)
                .rank(rank)
                .rankScore(rankScore)
                .memberType(memberType)
                .role(role)
                .profileImage(profileImage)
                .build();
    }

    // Member 1 -> 1 ProfileImage
    // 단방향 연관관계 편의 메서드
    public void assignProfileImage(ProfileImage profileImage) {
        this.profileImage = profileImage;
    }

    // Member 1 <-> N MemberGatherArticle
    // 양방향 연관관계 편의 메서드
    public void addMemberGatherArticle(MemberGatherArticle memberGatherArticle) {
        this.memberGatherArticles.add(memberGatherArticle);

        if (memberGatherArticle.getMember() != this) {
            memberGatherArticle.assignMember(this);
        }
    }

    // Member 1 <-> N Comment
    // 양방향 연관관계 편의 메서드
    public void addComment(Comment comment) {
        this.comments.add(comment);

        if (comment.getMember() != this) {
            comment.assignMember(this);
        }
    }

    // Member 1 <-> N MemberChatRoom
    // 양방향 연관관계 편의 메서드
    public void addMemberChatRoom(MemberChatRoom memberChatRoom) {
        this.memberChatRooms.add(memberChatRoom);

        if (memberChatRoom.getMember() != this) {
            memberChatRoom.assignMember(this);
        }
    }

    // Member 1 <-> N BadgeImage
    // 양방향 연관관계 편의 메서드
    public void addBadgeImage(BadgeImage badgeImage) {
        this.badgeImages.add(badgeImage);

        if (badgeImage.getMember() != this) {
            badgeImage.assignMember(this);
        }
    }

    // 이메일 수정 메서드
    public void assignEmail(String email) {
        this.email = email;
    }

    public void assignPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void assignNickname(String nickname) {
        this.nickname = nickname;
    }

    public void assignPassword(String password) {
        this.password = password;
    }

    public void assignDescription(String description) {
        this.description = description;
    }

    public void assignSido(String sido) {
        this.sido = sido;
    }

    public void assignSgg(String sgg) {
        this.sgg = sgg;
    }

    public void assignEmd(String emd) {
        this.emd = emd;
    }

    // Member 위치 수정 메서드
    public void assignLocation(String sido, String sgg, String emd) {
        this.sido = sido;
        this.sgg = sgg;
        this.emd = emd;
    }

    // Member 반경 수정 메서드
    public void assignRadius(Integer radius) {
        this.radius = radius;
    }

    // 각 리뷰 카운트 수정 메서드
    public void assignReviewCount(Integer monthlyExcellentCount, Integer totalExcellentCount, Integer monthlyGoodCount, Integer totalGoodCount, Integer monthlyBadCount, Integer totalBadCount, Integer monthlyNoShowCount) {
        this.monthlyExcellentCount = monthlyExcellentCount;
        this.totalExcellentCount = totalExcellentCount;
        this.monthlyGoodCount = monthlyGoodCount;
        this.totalGoodCount = totalGoodCount;
        this.monthlyBadCount = monthlyBadCount;
        this.totalBadCount = totalBadCount;
        this.monthlyNoShowCount = monthlyNoShowCount;
    }

    // 리뷰 보낸 횟수 수정 메서드
    public void assignSendReviewCount(Integer monthlySendReviewCount) {
        this.monthlySendReviewCount = monthlySendReviewCount;
    }

    // 버디 지수 수정 메서드
    public void assignBuddyScore(Double buddyScore) {
        this.buddyScore = buddyScore;
    }

    // 참가 횟수 수정 메서드
    public void assignJoinCount(Integer joinCount) {
        this.joinCount = joinCount;
    }
}
