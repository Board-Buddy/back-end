package sumcoda.boardbuddy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import sumcoda.boardbuddy.dto.KakaoApiRequest;
import sumcoda.boardbuddy.dto.BoardCafeResponse;
import sumcoda.boardbuddy.dto.KakaoApiResponse;
import sumcoda.boardbuddy.exception.kakaoApi.KakaoApiServerException;
import sumcoda.boardbuddy.util.KakaoApiUtil;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoApiService {

    // 첫 페이지 번호
    private static final int FIRST_PAGE_NUMBER = 1;
    // 법정동 인덱스
    private static final int LEGAL_DONG_INDEX = 0;

    // Kakao API 키 접두사
    private static final String KAKAO_API_KEY_PREFIX = "KakaoAK ";
    // 중요 카테고리만 그룹핑한 카테고리 그룹명
    private static final String BOARD_CAFE_CATEGORY = "가정,생활 > 여가시설 > 보드카페";
    // 세종특별자치시
    private static final String SEJONG_SPECIAL_SELF_GOVERNING_CITY = "세종특별자치시";

    // 환경 변수로 등록한 API 키
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoRestApiKey;

    // kakao API 를 호출하기 위한 클라이언트 모듈
    private final RestTemplate restTemplate;

    /**
     * 키워드로 장소 검색 API 요청 처리 메서드
     *
     * @param locationDTO 검색 요청에 대한 위치 정보가 담긴 DTO
     * @return 보드게임 카페 리스트
     */
    public List<BoardCafeResponse.InfoDTO> requestBoardCafeKeywordSearch(KakaoApiRequest.LocationDTO locationDTO) {

        // 첫 페이지 번호 초기화
        int page = FIRST_PAGE_NUMBER;
        // API 응답의 마지막 페이지 여부
        boolean isEnd = false;

        // 결과를 담을 리스트 초기화
        List<BoardCafeResponse.InfoDTO> infoDTOS = new ArrayList<>();

            // 마지막 페이지가 아닐 때까지 반복
            while (!isEnd) {
                // URI 빌드
                URI uri = KakaoApiUtil.buildUriByKeywordSearch(locationDTO.getX(), locationDTO.getY(), locationDTO.getRadius(), page);

                // HTTP 헤더 설정
                HttpHeaders headers = new HttpHeaders();
                headers.set(HttpHeaders.AUTHORIZATION, KAKAO_API_KEY_PREFIX + kakaoRestApiKey);

                // 요청 엔터티 생성
                HttpEntity<?> httpEntity = new HttpEntity<>(headers);

                // Kakao API 호출 및 응답 받기
                KakaoApiResponse.KeywordSearchDTO response = restTemplate.exchange(
                        uri, HttpMethod.GET, httpEntity, KakaoApiResponse.KeywordSearchDTO.class).getBody();

                // 응답이 null 인 경우 예외 처리
                if (response == null) {
                    throw new KakaoApiServerException("카카오 API 서버에 문제가 있습니다.");
                }

                // "가정,생활 > 여가시설 > 보드카페"을 포함하는 것만 필터링하고 DTO 로 응답
                List<BoardCafeResponse.InfoDTO> filteredInfoDTOS = response.getDocumentDTOList().stream()
                                .filter(documentDTO -> documentDTO.getCategoryName().contains(BOARD_CAFE_CATEGORY))
                                .map(documentDTO -> {
                                    // 좌표로 행정구역정보를 받아서 응답 DTO 에 추가
                                    KakaoApiResponse.RegionDTO regionDTO = requestCoordinateToRegion(documentDTO.getX(), documentDTO.getY());
                                    return BoardCafeResponse.InfoDTO.builder()
                                            .addressName(documentDTO.getAddressName())
                                            .distance(documentDTO.getDistance())
                                            .id(documentDTO.getId())
                                            .phone(documentDTO.getPhone())
                                            .placeName(documentDTO.getPlaceName())
                                            .placeUrl(documentDTO.getPlaceUrl())
                                            .roadAddressName(documentDTO.getRoadAddressName())
                                            .x(documentDTO.getX())
                                            .y(documentDTO.getY())
                                            .sido(regionDTO.getRegion1depthName())
                                            .sgg(regionDTO.getRegion2depthName())
                                            .emd(regionDTO.getRegion3depthName())
                                            .build();
                                })
                        .toList();
                // 필터링된 결과를 리스트에 추가
                infoDTOS.addAll(filteredInfoDTOS);
                // 마지막 페이지 여부 갱신
                isEnd = response.getMetaDTO().getIsEnd();
                // 페이지 번호 증가
                page++;
        }

        // 검색된 보드카페 정보 DTO 리스트 반환
        return infoDTOS;
    }

    /**
     * 좌표로 행정구역정보 받기 API 요청 처리 메서드
     *
     * @param x 경도
     * @param y 위도
     * @return 행정구역정보 DTO 리스트
     */
    private KakaoApiResponse.RegionDTO requestCoordinateToRegion(Double x, Double y) {

        // URI 빌드
        URI uri = KakaoApiUtil.buildUriByCoordinateToRegion(x, y);

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, KAKAO_API_KEY_PREFIX + kakaoRestApiKey);

        // 요청 엔터티 생성
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        // Kakao API 호출 및 응답 받기
        KakaoApiResponse.CoordinateToRegionDTO response = restTemplate.exchange(
                uri, HttpMethod.GET, httpEntity, KakaoApiResponse.CoordinateToRegionDTO.class).getBody();

        // 응답이 null 인 경우 예외 처리
        if (response == null) {
            throw new KakaoApiServerException("카카오 API 서버에 문제가 있습니다.");
        }

        // 법정동 정보를 가져옴
        KakaoApiResponse.RegionDTO regionDTO = response.getRegionDTOs().get(LEGAL_DONG_INDEX);

        // sido 가 세종특별자치시인 경우 sido 와 sgg 를 동일하게 설정
        if (SEJONG_SPECIAL_SELF_GOVERNING_CITY.equals(regionDTO.getRegion1depthName())) {
            return KakaoApiResponse.RegionDTO.builder()
                    .region1depthName(SEJONG_SPECIAL_SELF_GOVERNING_CITY)
                    .region2depthName(SEJONG_SPECIAL_SELF_GOVERNING_CITY)
                    .region3depthName(regionDTO.getRegion3depthName())
                    .build();
        }

        // 행정구역 정보 반환
        return regionDTO;
    }
}
