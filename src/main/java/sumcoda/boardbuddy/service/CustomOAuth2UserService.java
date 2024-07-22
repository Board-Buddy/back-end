package sumcoda.boardbuddy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import sumcoda.boardbuddy.dto.auth.oauth2.*;
import sumcoda.boardbuddy.entity.Member;
import sumcoda.boardbuddy.enumerate.MemberRole;
import sumcoda.boardbuddy.exception.auth.ClientRegistrationRetrievalException;
import sumcoda.boardbuddy.exception.auth.SocialUserInfoRetrievalException;
import sumcoda.boardbuddy.repository.MemberRepository;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 리소스 서버에서 유저 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        if (oAuth2User == null) {
            throw new SocialUserInfoRetrievalException("소셜 리소스 서버에서 유저 정보를 가져오지 못했습니다. 잠시후 다시 시도해주세요.");
        }

        log.info("oAuth2User = " + oAuth2User.getAttributes());

        // naver, google, kakao 등 값이 저장
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if (registrationId == null) {
            throw new ClientRegistrationRetrievalException("소셜 리소스 서버에서 클라이언트의 정보를 가져오지 못했습니다. 잠시후 다시 시도해주세요.");
        }

        // 응답 받은 데이터 저장하기 위한 변수 생성
        OAuth2UserInfo oAuth2UserInfo;

        // 네이버에서 제공된 데이터라면
        switch (registrationId) {
            // 네이버에서 제공된 데이터라면
            case "naver":
                oAuth2UserInfo = new NaverUserInfo(oAuth2User.getAttributes());
                break;
            // 구글에서 제공된 데이터라면
            case "google":
                oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
                break;
            // 카카오에서 제공된 데이터라면
            case "kakao":
                oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
                break;
            default:
                return null;
        }

        String username = oAuth2UserInfo.getUsername();

        // 유저 이름을 바탕으로 DB 조회
        Member findMember = memberRepository.findByUsername(username).orElse(null);

        MemberRole role = MemberRole.USER;

        SecureRandom secureRandom = new SecureRandom();
        int randomNumber = 10000000 + secureRandom.nextInt(90000000);

        // 만약 신규 로그인 회원이라면
        if (findMember == null) {
            Member member = Member.builder()
                    .username(username)
                    .password(bCryptPasswordEncoder.encode(oAuth2UserInfo.getEmail()))
                    .nickname(oAuth2UserInfo.getName() + randomNumber)
                    .email(oAuth2UserInfo.getEmail())
                    .phoneNumber(null)
                    .sido(null)
                    .sigu(null)
                    .dong(null)
                    .radius(2)
                    .buddyScore(50)
                    .joinCount(0)
                    .monthlyExcellentCount(0)
                    .totalExcellentCount(0)
                    .monthlyGoodCount(0)
                    .totalGoodCount(0)
                    .monthlyBadCount(0)
                    .totalBadCount(0)
                    .monthlyNoShowCount(0)
                    .monthlySendReviewCount(0)
                    .description(null)
                    .rank(null)
                    .memberRole(MemberRole.USER)
                    .profileImage(null)
                    .build();

            memberRepository.save(member);

            // 신규 로그인 회원이 아니라면 현재 로그인한 유저 정보를 바탕으로 DB에 업데이트
        } else {

            // 해당 유저가 특정 경로에 접근할때 인가 작업에 필요한 role 값 업데이트
            role = findMember.getMemberRole();

            // Dirty Checking 으로 업데이트 값 자동으로 DB 반영
            findMember.assignEmail(oAuth2UserInfo.getEmail());
        }

        // OAuth2LoginAuthenticationProvider에 OAuth2User 를 전달하고 로그인 완료
        return new CustomOAuth2User(oAuth2UserInfo, role);
    }
}
