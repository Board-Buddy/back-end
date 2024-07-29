package sumcoda.boardbuddy.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sumcoda.boardbuddy.dto.BoardCafeResponse;
import sumcoda.boardbuddy.dto.common.ApiResponse;
import sumcoda.boardbuddy.service.BoardCafeService;

import java.util.List;
import java.util.Map;

import static sumcoda.boardbuddy.builder.ResponseBuilder.buildSuccessResponseWithPairKeyData;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BoardCafeController {

    private final BoardCafeService boardCafeService;

    /**
     * 보드게임 카페 찾기 요청 처리
     *
     * @param x 경도
     * @param y 위도
     * @param radius 반경 (단위: 미터)
     * @param username 사용자 이름
     * @return 보드게임 카페 리스트
     */
    @GetMapping("/api/board-cafes")
    public ResponseEntity<ApiResponse<Map<String, List<BoardCafeResponse.InfoDTO>>>> getBoardCafes(
            @RequestParam Double x,
            @RequestParam Double y,
            @RequestParam Integer radius,
            @RequestAttribute String username) {
        log.info("getBoardCafes is working");

        List<BoardCafeResponse.InfoDTO> cafes = boardCafeService.getBoardCafes(x, y, radius, username);

        return buildSuccessResponseWithPairKeyData("cafes", cafes, "보드 게임 카페 조회를 성공하였습니다.", HttpStatus.OK);
    }
}
