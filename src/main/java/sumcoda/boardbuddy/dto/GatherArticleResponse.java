package sumcoda.boardbuddy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

        private LocalDateTime startDateTime;

        private LocalDateTime endDateTime;

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
    @Setter
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
    @Setter
    public static class ReadDTO {
        private String title;
        private String description;
        private GatherArticleResponse.AuthorDTO author;
        private String location;
        private Integer maxParticipants;
        private Integer currentParticipants;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime startTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime endTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime createdAt;
        private GatherArticleStatus status;
        private String participationStatus;

        @Builder
        public ReadDTO(String title, String description, AuthorDTO author, String location,
                       Integer maxParticipants, Integer currentParticipants, LocalDateTime startTime,
                       LocalDateTime endTime, LocalDateTime createdAt, GatherArticleStatus status, String participationStatus) {
            this.title = title;
            this.description = description;
            this.author = author;
            this.location = location;
            this.maxParticipants = maxParticipants;
            this.currentParticipants = currentParticipants;
            this.startTime = startTime;
            this.endTime = endTime;
            this.createdAt = createdAt;
            this.status = status;
            this.participationStatus = participationStatus;
        }

        // 엔티티를 response dto로 변환
        public static GatherArticleResponse.ReadDTO from(GatherArticle gatherArticle, Member member, String participationStatus) {
            return GatherArticleResponse.ReadDTO.builder()
                    .title(gatherArticle.getTitle())
                    .description(gatherArticle.getDescription())
                    .author(GatherArticleResponse.AuthorDTO.from(member))
                    .location(gatherArticle.getMeetingLocation())
                    .maxParticipants(gatherArticle.getMaxParticipants())
                    .currentParticipants(gatherArticle.getCurrentParticipants())
                    .startTime(gatherArticle.getStartDateTime())
                    .endTime(gatherArticle.getEndDateTime())
                    .createdAt(gatherArticle.getCreatedAt())
                    .status(gatherArticle.getStatus())
                    .participationStatus(participationStatus)
                    .build();
        }
    }

    @Getter
    @Setter
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
    @Setter
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
    @Setter
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