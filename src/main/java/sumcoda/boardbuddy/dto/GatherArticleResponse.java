package sumcoda.boardbuddy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sumcoda.boardbuddy.entity.GatherArticle;
import sumcoda.boardbuddy.entity.Member;
import sumcoda.boardbuddy.enumerate.GatherArticleStatus;

import java.time.LocalDateTime;

public class GatherArticleResponse {

    @Getter
    @NoArgsConstructor
    public static class GatherArticleInfosDTO {

        private Long id;

        private String title;

        private String description;

        private String meetingLocation;

        private Integer maxParticipants;

        private Integer currentParticipants;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime startDateTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime endDateTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime createdAt;

        private GatherArticleStatus status;

        @Builder
        public GatherArticleInfosDTO(Long id, String title, String description, String meetingLocation, Integer maxParticipants, Integer currentParticipants, LocalDateTime startDateTime, LocalDateTime endDateTime, LocalDateTime createdAt, GatherArticleStatus status) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.meetingLocation = meetingLocation;
            this.maxParticipants = maxParticipants;
            this.currentParticipants = currentParticipants;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
            this.createdAt = createdAt;
            this.status = status;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class CreateDTO {
        private Long id;

        @Builder
        public CreateDTO(Long id) {
            this.id = id;
        }

        public static GatherArticleResponse.CreateDTO from(GatherArticle gatherArticle) {
            return GatherArticleResponse.CreateDTO.builder()
                    .id(gatherArticle.getId())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class ReadDTO {
        private String title;
        private String description;
        private GatherArticleResponse.AuthorDTO author;
        private String meetingLocation;
        private Integer maxParticipants;
        private Integer currentParticipants;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime startDateTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime endDateTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime createdAt;
        private GatherArticleStatus status;
        private String participationStatus;

        @Builder
        public ReadDTO(String title, String description, AuthorDTO author, String meetingLocation,
                       Integer maxParticipants, Integer currentParticipants, LocalDateTime startDateTime,
                       LocalDateTime endDateTime, LocalDateTime createdAt, GatherArticleStatus status, String participationStatus) {
            this.title = title;
            this.description = description;
            this.author = author;
            this.meetingLocation = meetingLocation;
            this.maxParticipants = maxParticipants;
            this.currentParticipants = currentParticipants;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
            this.createdAt = createdAt;
            this.status = status;
            this.participationStatus = participationStatus;
        }

        // 엔티티를 response dto로 변환
        public static GatherArticleResponse.ReadDTO from(GatherArticle gatherArticle, Member member, String participationStatus) {
            return ReadDTO.builder()
                    .title(gatherArticle.getTitle())
                    .description(gatherArticle.getDescription())
                    .author(AuthorDTO.from(member))
                    .meetingLocation(gatherArticle.getMeetingLocation())
                    .maxParticipants(gatherArticle.getMaxParticipants())
                    .currentParticipants(gatherArticle.getCurrentParticipants())
                    .startDateTime(gatherArticle.getStartDateTime())
                    .endDateTime(gatherArticle.getEndDateTime())
                    .createdAt(gatherArticle.getCreatedAt())
                    .status(gatherArticle.getStatus())
                    .participationStatus(participationStatus)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class AuthorDTO {
        private String nickname;
        private int rank;
        private String profileURL;
        private String description;

        @Builder
        public AuthorDTO(String nickname, int rank, String profileURL, String description) {
            this.nickname = nickname;
            this.rank = rank;
            this.profileURL = profileURL;
            this.description = description;
        }

        // 엔티티를 response dto로 변환
        public static GatherArticleResponse.AuthorDTO from(Member member) {
            return GatherArticleResponse.AuthorDTO.builder()
                    .nickname(member.getNickname())
                    .rank(member.getRank())
                    .profileURL(member.getProfileImage() != null ? member.getProfileImage().getAwsS3SavedFileURL() : null)
                    .description(member.getDescription())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateDTO {
        private Long id;

        @Builder
        public UpdateDTO(Long id) {
            this.id = id;
        }

        // 엔티티를 response dto로 변환
        public static GatherArticleResponse.UpdateDTO from(GatherArticle gatherArticle) {
            return GatherArticleResponse.UpdateDTO.builder()
                    .id(gatherArticle.getId())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class DeleteDTO {
        private Long id;

        @Builder
        public DeleteDTO(Long id) {
            this.id = id;
        }

        // 엔티티를 response dto로 변환
        public static GatherArticleResponse.DeleteDTO from(GatherArticle gatherArticle) {
            return GatherArticleResponse.DeleteDTO.builder()
                    .id(gatherArticle.getId())
                    .build();
        }
    }
}
