package sumcoda.boardbuddy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sumcoda.boardbuddy.dto.KakaoApiRequest;
import sumcoda.boardbuddy.dto.BoardCafeResponse;
import sumcoda.boardbuddy.exception.boardCafe.BoardCafeRadiusException;
import sumcoda.boardbuddy.exception.member.MemberRetrievalException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardCafeService {

    // 최소 반경
    private static final int MIN_RADIUS = 0;
    // 최대 반경
    private static final int MAX_RADIUS = 20000;

    // Kakao API 서비스 의존성 주입
    private final KakaoApiService kakaoApiService;

    /**
     * 보드게임 카페 찾기 메서드
     *
     * @param x 경도
     * @param y 위도
     * @param radius 반경 (단위: 미터)
     * @param username 사용자 이름
     * @return 보드게임 카페 리스트
     */
    public List<BoardCafeResponse.InfoDTO> getBoardCafes(Double x, Double y, Integer radius , String username) {

        // 유저 검증
        if (username == null) {
            throw new MemberRetrievalException("보드 게임 카페 찾기 요청을 처리할 수 없습니다. 관리자에게 문의하세요.");
        }

        // 반경 검증
        if (radius < MIN_RADIUS || radius > MAX_RADIUS) {
            throw new BoardCafeRadiusException("반경 값은 0에서 20000 사이여야 합니다.");
        }

        // Kakao API 서비스 호출
        return kakaoApiService.requestBoardCafeKeywordSearch(
                KakaoApiRequest.LocationDTO.builder()
                        .x(x)
                        .y(y)
                        .radius(radius)
                        .build());
    }
}

