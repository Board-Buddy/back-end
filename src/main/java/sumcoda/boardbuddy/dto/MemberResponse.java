package sumcoda.boardbuddy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sumcoda.boardbuddy.enumerate.GatherArticleStatus;

import java.time.LocalDateTime;

public class MemberResponse {

    @Getter
    @NoArgsConstructor
    public static class ProfileDTO {

        private String nickname;

        private String sido;

        private String sigu;

        private String dong;

        // phoneNumber 필드가 null 일 때 JSON 반환하지 않도록하는 어노테이션
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String phoneNumber;

        private Boolean isPhoneNumberVerified;

        private String awsS3SavedFileURL;

        @Builder
        public ProfileDTO(String nickname, String sido, String sigu, String dong, String phoneNumber, Boolean isPhoneNumberVerified, String awsS3SavedFileURL) {
            this.nickname = nickname;
            this.sido = sido;
            this.sigu = sigu;
            this.dong = dong;
            this.phoneNumber = phoneNumber;
            this.isPhoneNumberVerified = isPhoneNumberVerified;
            this.awsS3SavedFileURL = awsS3SavedFileURL;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class GatherArticleDTO {

        private Long id;

        private String title;

        private String description;

        private String meetingLocation;

        private Integer maxParticipants;

        private Integer currentParticipants;

        private LocalDateTime meetingDate;

        private LocalDateTime meetingEndDate;

        private LocalDateTime createdAt;

        private GatherArticleStatus status;

        @Builder
        public GatherArticleDTO (Long id, String title, String description, String meetingLocation, Integer maxParticipants, Integer currentParticipants, LocalDateTime meetingDate, LocalDateTime meetingEndDate, LocalDateTime createdAt, GatherArticleStatus status) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.meetingLocation = meetingLocation;
            this.maxParticipants = maxParticipants;
            this.currentParticipants = currentParticipants;
            this.meetingDate = meetingDate;
            this.meetingEndDate = meetingEndDate;
            this.createdAt = createdAt;
            this.status = status;
        }
    }
}
