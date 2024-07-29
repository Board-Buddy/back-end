package sumcoda.boardbuddy.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class KakaoApiRequest {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class LocationDTO {

        private Double x;
        private Double y;
        private Integer radius;

        @Builder
        public LocationDTO(Double x, Double y, Integer radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }
    }
}
