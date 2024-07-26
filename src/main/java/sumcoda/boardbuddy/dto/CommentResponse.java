package sumcoda.boardbuddy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sumcoda.boardbuddy.entity.Comment;
import sumcoda.boardbuddy.entity.Member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CommentResponse {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CommentDTO {
        private Long id;
        private AuthorDTO author;
        private String content;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime createdAt;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<CommentDTO> children;

        @Builder
        public CommentDTO(Long id, AuthorDTO author, String content, LocalDateTime createdAt, List<CommentDTO> children) {
            this.id = id;
            this.author = author;
            this.content = content;
            this.createdAt = createdAt;
            this.children = children;
        }

        public static CommentDTO from(Comment comment) {
            return CommentDTO.builder()
                    .id(comment.getId())
                    .author(AuthorDTO.from(comment.getMember()))
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .children(comment.getChildren().stream()
                            .map(CommentDTO::from)
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class AuthorDTO {
        private String nickname;
        private Integer rank;
        private String profileImageS3SavedURL;

        @Builder
        public AuthorDTO(String nickname, Integer rank, String profileImageS3SavedURL) {
            this.nickname = nickname;
            this.rank = rank;
            this.profileImageS3SavedURL = profileImageS3SavedURL;
        }

        public static AuthorDTO from(Member member) {
            return AuthorDTO.builder()
                    .nickname(member.getNickname())
                    .rank(member.getRank())
                    .profileImageS3SavedURL(member.getProfileImage() != null ? member.getProfileImage().getProfileImageS3SavedURL() : null)
                    .build();
        }
    }
}