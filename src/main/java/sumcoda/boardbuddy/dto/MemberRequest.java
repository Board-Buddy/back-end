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

        private String sigu;

        private String dong;

        @Builder
        public RegisterDTO(String username, String password, String nickname, String email, String phoneNumber, String sido, String sigu, String dong) {
            this.username = username;
            this.password = password;
            this.nickname = nickname;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.sido = sido;
            this.sigu = sigu;
            this.dong = dong;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class OAuth2RegisterDTO {

        private String phoneNumber;

        private String sido;

        private String sigu;

        private String dong;

        @Builder
        public OAuth2RegisterDTO(String phoneNumber, String sido, String sigu, String dong) {
            this.phoneNumber = phoneNumber;
            this.sido = sido;
            this.sigu = sigu;
            this.dong = dong;
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
        private String sigu;
        private String dong;

        @Builder
        public LocationDTO(String sido, String sigu, String dong) {
            this.sido = sido;
            this.sigu = sigu;
            this.dong = dong;
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
}

