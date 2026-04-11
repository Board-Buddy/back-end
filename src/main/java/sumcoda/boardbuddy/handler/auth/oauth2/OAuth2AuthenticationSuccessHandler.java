package sumcoda.boardbuddy.handler.auth.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import sumcoda.boardbuddy.dto.auth.oauth2.CustomOAuth2User;
import sumcoda.boardbuddy.entity.Member;
import sumcoda.boardbuddy.exception.member.MemberRetrievalException;
import sumcoda.boardbuddy.repository.member.MemberRepository;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException{
        log.info("OAuth2 success handler is working");

        RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

        CustomOAuth2User user =  (CustomOAuth2User) authentication.getPrincipal();
        Boolean isPhoneNumberVerifiedMember = checkIsPhoneNumberVerifiedMember(user);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 기존 소셜 로그인 사용자인 경우
        if (Boolean.TRUE.equals(isPhoneNumberVerifiedMember)) {
            response.setStatus(HttpStatus.OK.value());
            redirectStrategy.sendRedirect(request, response, "https://m.boredbuddi.com/login/oauth/callback?isLoginSucceed=true&isVerifiedMember=true&messageCode=0");
            // 신규 소셜 로그인 사용자인 경우
        } else {
            response.setStatus(HttpStatus.CREATED.value());
            redirectStrategy.sendRedirect(request, response, "https://m.boredbuddi.com/login/oauth/callback?isLoginSucceed=true&isVerifiedMember=false&messageCode=0");
        }
    }

    private Boolean checkIsPhoneNumberVerifiedMember(CustomOAuth2User user) {
        String username = user.getUsername();

        log.info("login user name : " + username);

        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new MemberRetrievalException("해당 유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        return member.getPhoneNumber() != null;
    }

}

