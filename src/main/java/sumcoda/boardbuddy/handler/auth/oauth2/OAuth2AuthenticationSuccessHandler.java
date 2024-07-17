package sumcoda.boardbuddy.handler.auth.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import sumcoda.boardbuddy.dto.auth.oauth2.CustomOAuth2User;
import sumcoda.boardbuddy.entity.Member;
import sumcoda.boardbuddy.repository.MemberRepository;

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
            redirectStrategy.sendRedirect(request, response, "https://boardbuddyapp.vercel.app/login/oauth/callback?isLoginSucceed=true&isVerifiedMember=true&message=로그인에 성공하였습니다.");
        // 신규 소셜 로그인 사용자인 경우
        } else {
            response.setStatus(HttpStatus.CREATED.value());
            redirectStrategy.sendRedirect(request, response , "https://boardbuddyapp.vercel.app/login/oauth/callback?isLoginSucceed=true&isVerifiedMember=false&message=로그인에 성공하였습니다.");
        }
    }

    private Boolean checkIsPhoneNumberVerifiedMember(CustomOAuth2User user) {
        String username = user.getUsername();

        log.info("login user name : " + username);

        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(""));

        return member.getPhoneNumber() != null;
    }

}