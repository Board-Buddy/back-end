package sumcoda.boardbuddy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sumcoda.boardbuddy.dto.MemberRequest;
import sumcoda.boardbuddy.dto.MemberResponse;
import sumcoda.boardbuddy.entity.Member;
import sumcoda.boardbuddy.enumerate.MemberRole;
import sumcoda.boardbuddy.exception.member.MemberSaveException;
import sumcoda.boardbuddy.exception.member.NicknameAlreadyExistsException;
import sumcoda.boardbuddy.exception.member.UsernameAlreadyExistsException;
import sumcoda.boardbuddy.repository.GatherArticleRepository;
import sumcoda.boardbuddy.repository.MemberRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    private final GatherArticleRepository gatherArticleRepository;

    // 비밀번호를 암호화 하기 위한 필드
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 아이디 중복검사
     *
     * @param verifyUsernameDuplicationDTO 사용자가 입력한 아이디
     * @return 아이디가 존재하지 않으면 true, 이미 존재하면 false 를 프론트로 반환
     **/
    public Boolean verifyUsernameDuplication(MemberRequest.VerifyUsernameDuplicationDTO verifyUsernameDuplicationDTO) {

        Boolean isAlreadyExistsUsername = memberRepository.existsByUsername(verifyUsernameDuplicationDTO.getUsername());

        if (Boolean.TRUE.equals(isAlreadyExistsUsername)) {
            throw new UsernameAlreadyExistsException("동일한 아이디가 이미 존재합니다.");
        }

        return true;
    }

    /**
     * 닉네임 중복검사
     *
     * @param verifyNicknameDuplicationDTO 사용자가 입력한 닉네임
     * @return 닉네임이 존재하지 않으면 true, 이미 존재하면 false 를 프론트로 반환
     **/
    public Boolean verifyNicknameDuplication(MemberRequest.VerifyNicknameDuplicationDTO verifyNicknameDuplicationDTO) {

        Boolean isAlreadyExistsNickname = memberRepository.existsByNickname(verifyNicknameDuplicationDTO.getNickname());

        if (Boolean.TRUE.equals(isAlreadyExistsNickname)) {
            throw new NicknameAlreadyExistsException("동일한 닉네임이 이미 존재합니다.");
        }

        return true;
    }

    /**
     * 회원가입 요청 캐치
     *
     * @param registerDTO 전달받은 회원가입 정보
     **/
    @Transactional
    public Long registerMember(MemberRequest.RegisterDTO registerDTO) {

        Long memberId = memberRepository.save(Member.createMember(
                registerDTO.getUsername(),
                bCryptPasswordEncoder.encode(registerDTO.getPassword()),
                registerDTO.getNickname(),
                registerDTO.getEmail(),
                registerDTO.getPhoneNumber(),
                registerDTO.getSido(),
                registerDTO.getSigu(),
                registerDTO.getDong(),
                5,
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

        return memberId;
    }

    /**
     * 내가 작성한 모집글 조회
     *
     * @param username 회원의 username
     * @return 내가 작성한 모집글 DTO 리스트
     **/
    @Transactional
    public List<MemberResponse.GatherArticleDTO> getMyGatherArticles (String username) {
        return gatherArticleRepository.findGatherArticleDTOByUsername(username);
    }
}
