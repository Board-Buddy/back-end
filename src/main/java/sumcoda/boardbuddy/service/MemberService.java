package sumcoda.boardbuddy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sumcoda.boardbuddy.dto.MemberRequest;
import sumcoda.boardbuddy.dto.NearPublicDistrictResponse;
import sumcoda.boardbuddy.dto.PublicDistrictResponse;
import sumcoda.boardbuddy.entity.Member;
import sumcoda.boardbuddy.enumerate.MemberRole;
import sumcoda.boardbuddy.exception.member.*;
import sumcoda.boardbuddy.exception.publicDistrict.PublicDistrictNotFoundException;
import sumcoda.boardbuddy.repository.MemberRepository;
import sumcoda.boardbuddy.repository.publicDistrict.PublicDistrictRepository;
import sumcoda.boardbuddy.util.AuthUtil;

import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    private final PublicDistrictRepository publicDistrictRepository;

    private final NearPublicDistrictService nearPublicDistrictService;

    // 비밀번호를 암호화 하기 위한 필드
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final AuthUtil authUtil;

    /**
     * 아이디 중복검사
     *
     * @param verifyUsernameDuplicationDTO 사용자가 입력한 아이디
     **/
    public void verifyUsernameDuplication(MemberRequest.VerifyUsernameDuplicationDTO verifyUsernameDuplicationDTO) {

        Boolean isAlreadyExistsUsername = memberRepository.existsByUsername(verifyUsernameDuplicationDTO.getUsername());

        if (isAlreadyExistsUsername == null) {
            throw new MemberRetrievalException("유저를 조회하면서 서버 문제가 발생했습니다. 관리자에게 문의하세요.");
        }

        if (Boolean.TRUE.equals(isAlreadyExistsUsername)) {
            throw new UsernameAlreadyExistsException("동일한 아이디가 이미 존재합니다.");
        }
    }

    /**
     * 닉네임 중복검사
     *
     * @param verifyNicknameDuplicationDTO 사용자가 입력한 닉네임
     **/
    public void verifyNicknameDuplication(MemberRequest.VerifyNicknameDuplicationDTO verifyNicknameDuplicationDTO) {

        Boolean isAlreadyExistsNickname = memberRepository.existsByNickname(verifyNicknameDuplicationDTO.getNickname());

        if (isAlreadyExistsNickname == null) {
            throw new MemberRetrievalException("유저를 조회하면서 서버 문제가 발생했습니다. 관리자에게 문의하세요.");
        }

        if (Boolean.TRUE.equals(isAlreadyExistsNickname)) {
            throw new NicknameAlreadyExistsException("동일한 닉네임이 이미 존재합니다.");
        }
    }

    /**
     * 회원가입 요청 캐치
     *
     * @param registerDTO 전달받은 회원가입 정보
     **/
    @Transactional
    public void registerMember(MemberRequest.RegisterDTO registerDTO) {

        Long memberId = memberRepository.save(Member.createMember(
                registerDTO.getUsername(),
                bCryptPasswordEncoder.encode(registerDTO.getPassword()),
                registerDTO.getNickname(),
                registerDTO.getEmail(),
                registerDTO.getPhoneNumber(),
                registerDTO.getSido(),
                registerDTO.getSigu(),
                registerDTO.getDong(),
                2,
                50,
                0,
                0,
                0,
                0,
                0,
                0,
                null,
                null,
                MemberRole.USER,
                null)).getId();

        if (memberId == null) {
            throw new MemberSaveException("서버 문제로 회원가입에 실패하였습니다. 관리자에게 문의하세요.");
        }
    }

    /**
     * 소셜 로그인 사용자에 대한 추가적인 회원가입
     *
     * @param oAuth2RegisterDTO 소셜로그인 사용자에 대한 추가적인 회원가입 정보
     * @param authentication 로그인 정보를 포함하는 사용자 객체
     **/
    @Transactional
    public void registerOAuth2Member(MemberRequest.OAuth2RegisterDTO oAuth2RegisterDTO, Authentication authentication) {

        String username = authUtil.getUserNameByLoginType(authentication);

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("해당 유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        member.assignPhoneNumber(oAuth2RegisterDTO.getPhoneNumber());
        member.assignSido(oAuth2RegisterDTO.getSido());
        member.assignSigu(oAuth2RegisterDTO.getSigu());
        member.assignDong(oAuth2RegisterDTO.getDong());
    }

    /**
     * 소셜 로그인 사용자에 대한 추가적인 회원가입
     *
     * @param authentication 로그인 정보를 포함하는 사용자 객체
     **/
    @Transactional
    public void withdrawalMember(Authentication authentication) {

        String username = authUtil.getUserNameByLoginType(authentication);

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("해당 유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        memberRepository.delete(member);

        // 삭제 확인
        boolean isExists = memberRepository.existsById(member.getId());
        if (isExists) {
            throw new MemberDeletionFailureException("회원 탈퇴에 실패했습니다. 관리자에게 문의하세요.");
        }
    }

    /**
     * 멤버 위치 설정 요청 캐치
     *
     * @param locationDTO 사용자가 입력한 위치 정보
     **/
    @Transactional
    public Map<Integer, List<NearPublicDistrictResponse.LocationDTO>> updateMemberLocation(MemberRequest.LocationDTO locationDTO, String username) {

        // 사용자가 입력한 시도, 시구, 동
        String sido = locationDTO.getSido();
        String sigu = locationDTO.getSigu();
        String dong = locationDTO.getDong();

        // 사용자 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("유효하지 않은 사용자입니다."));

        // 데이터베이스에 사용자가 입력한 행정 구역이 있는지 검증
        PublicDistrictResponse.LocationDTO baseLocationRequestDTO = publicDistrictRepository.findOneBySidoAndSiguAndDong(sido, sigu, dong)
                .orElseThrow(() -> new PublicDistrictNotFoundException("입력한 위치 정보가 올바르지 않습니다."));

        // 멤버의 위치 업데이트
        member.assignLocation(sido, sigu, dong);

        // 사용자가 입력한 주변 시도, 시구, 동에 대한 주변 시도, 시구, 동을 반환하기 위해 findNearbyLocations() 메서드 실행 후 응답 반환
        return nearPublicDistrictService.findNearbyLocations(baseLocationRequestDTO);
    }
}
