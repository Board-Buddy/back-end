package sumcoda.boardbuddy.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sumcoda.boardbuddy.dto.PublicDistrictResponse;
import sumcoda.boardbuddy.dto.common.ApiResponse;
import sumcoda.boardbuddy.service.PublicDistrictService;

import java.util.List;
import java.util.Map;

import static sumcoda.boardbuddy.builder.ResponseBuilder.buildSuccessResponseWithPairKeyData;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PublicDistrictController {

    private final PublicDistrictService publicDistrictService;

    /**
     * 위치 검색 요청 처리 (회원가입)
     *
     * @param emd 읍면동 검색어
     * @return 검색된 위치 정보 리스트
     */
    @GetMapping("/api/locations/search")
    public ResponseEntity<ApiResponse<Map<String, List<PublicDistrictResponse.InfoDTO>>>> searchLocations(@RequestParam String emd) {
        log.info("searchLocations is working: " + emd + "으로 검색하였습니다.");

        List<PublicDistrictResponse.InfoDTO> locations = publicDistrictService.searchLocations(emd);

        return buildSuccessResponseWithPairKeyData("locations", locations, "위치 검색을 성공하였습니다.", HttpStatus.OK);
    }

    /**
     * 위치 검색 요청 처리 (로그인 이후)
     *
     * @param emd 읍면동 검색어
     * @return 검색된 위치 정보 리스트
     */
    @GetMapping("/api/auth/locations/search")
    public ResponseEntity<ApiResponse<Map<String, List<PublicDistrictResponse.InfoDTO>>>> searchAuthLocations(
            @RequestParam String emd,
            @RequestAttribute String username) {
        log.info("searchAuthLocations is working: " + emd + "으로 검색하였습니다.");

        List<PublicDistrictResponse.InfoDTO> locations = publicDistrictService.searchAuthLocations(emd, username);

        return buildSuccessResponseWithPairKeyData("locations", locations, "위치 검색을 성공하였습니다.", HttpStatus.OK);
    }
}
