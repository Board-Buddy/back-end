package sumcoda.boardbuddy.dto;

import lombok.*;
import sumcoda.boardbuddy.enumerate.ReviewType;

public class MemberRequest {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RegisterDTO {

        private String username;

        private String password;

        private String nickname;

        private String email;

        private String phoneNumber;

        private String sido;

        private String sgg;

        private String emd;

        @Builder
        public RegisterDTO(String username, String password, String nickname, String email, String phoneNumber, String sido, String sgg, String emd) {
            this.username = username;
            this.password = password;
            this.nickname = nickname;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.sido = sido;
            this.sgg = sgg;
            this.emd = emd;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class OAuth2RegisterDTO {

        private String phoneNumber;

        private String sido;

        private String sgg;

        private String emd;

        @Builder
        public OAuth2RegisterDTO(String phoneNumber, String sido, String sgg, String emd) {
            this.phoneNumber = phoneNumber;
            this.sido = sido;
            this.sgg = sgg;
            this.emd = emd;
        }
    }


    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class VerifyUsernameDuplicationDTO {

        private String username;

        @Builder
        public VerifyUsernameDuplicationDTO(String username) {
            this.username = username;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class VerifyNicknameDuplicationDTO {

        private String nickname;

        @Builder
        public VerifyNicknameDuplicationDTO(String nickname) {
            this.nickname = nickname;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class LocationDTO {

        private String sido;
        private String sgg;
        private String emd;

        @Builder
        public LocationDTO(String sido, String sgg, String emd) {
            this.sido = sido;
            this.sgg = sgg;
            this.emd = emd;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RadiusDTO {

        private Integer radius;

        @Builder
        public RadiusDTO(Integer radius) {
            this.radius = radius;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ReviewDTO {

        private String nickname;

        private ReviewType review;

        @Builder
        public ReviewDTO(String nickname, ReviewType review) {
            this.nickname = nickname;
            this.review = review;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class UpdateProfileDTO {

        private String nickname;

        private String password;

        private String phoneNumber;

        private String description;

        @Builder
        public UpdateProfileDTO(String nickname, String password, String phoneNumber, String description) {
            this.nickname = nickname;
            this.password = password;
            this.phoneNumber = phoneNumber;
            this.description = description;
        }
    }
}

