package sumcoda.boardbuddy.handler.auth.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        log.info("OAuth2 authentication failure handler is working");

        RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

        String messageCode = null;

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        if (exception instanceof OAuth2AuthenticationException) {
            messageCode = getOAuth2MessageCode(exception.getMessage());
        }
        else if (exception instanceof AuthenticationCredentialsNotFoundException) {
            messageCode = "8";
        }
        else if (exception instanceof InternalAuthenticationServiceException) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            messageCode = "9";
        }

        redirectStrategy.sendRedirect(request, response, "https://m.boredbuddi.com/login/oauth/callback?isLoginSucceed=false&isVerifiedMember=false&messageCode=" + messageCode);
    }

    private String getOAuth2MessageCode(String errorMessage) {

        String oAuth2MessageCode;

        switch (errorMessage) {
            case "The authorization request or token request is missing a required parameter":
                oAuth2MessageCode = "1";
                break;
            case "Missing or invalid client identifier":
                oAuth2MessageCode = "2";
                break;
            case "Invalid or mismatching redirection URI":
                oAuth2MessageCode = "3";
                break;
            case "The requested scope is invalid, unknown, or malformed":
                oAuth2MessageCode = "4";
                break;
            case "The resource owner or authorization server denied the access request":
                oAuth2MessageCode = "5";
                break;
            case "Client authentication failed":
                oAuth2MessageCode = "6";
                break;
            default:
                oAuth2MessageCode = "7";
                break;
        }
        return oAuth2MessageCode;
    }
}


