package sumcoda.boardbuddy.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import sumcoda.boardbuddy.filter.CustomAuthenticationFilter;
import sumcoda.boardbuddy.handler.auth.*;
import sumcoda.boardbuddy.handler.auth.oauth2.OAuth2AuthenticationFailureHandler;
import sumcoda.boardbuddy.handler.auth.oauth2.OAuth2AuthenticationSuccessHandler;
import sumcoda.boardbuddy.service.CustomOAuth2UserService;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    private final CustomOAuth2UserService customOAuth2UserService;

    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 출처 설정
        configuration.setAllowedOriginPatterns(List.of("https://board-buddy-front-end.vercel.app", "https://m.boredbuddi.com", "http://localhost:3000"));

        // 허용할 HTTP 메서드 설정
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 허용할 헤더 설정
        configuration.addAllowedHeader("*");

        // 노출할 헤더 설정
        configuration.addExposedHeader("Content-Type");
        configuration.addExposedHeader("X-AUTH-TOKEN");
        configuration.addExposedHeader("Authorization");
        configuration.addExposedHeader("Authorization_Refresh");
        configuration.addExposedHeader("Access-Control-Allow-Origin");
        configuration.addExposedHeader("Access-Control-Allow-Methods");
        configuration.addExposedHeader("Access-Control-Allow-Headers");
        configuration.addExposedHeader("Access-Control-Allow-Credentials");

        // 자격 증명 허용 설정
        configuration.setAllowCredentials(true);

        // pre-flight 요청 캐싱 시간 설정
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 새로추가
        http
                .cors((corsCustomizer -> corsCustomizer.configurationSource(corsConfigurationSource())))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // 보안 컨텍스트의 저장 방식을 제어하는 설정
                // 보안 컨텍스트가 명시적으로 저장될 때만 저장되도록 한다.
                // 보안 컨텍스트가 실수로 변경되거나 저장되는 것을 방지하여 보안성을 높인다.
                .securityContext(securityContext -> securityContext
                        .requireExplicitSave(true))
                .authorizeHttpRequests(request -> request
                        .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                        .requestMatchers(
                                // 로그인 하지 않은 사용자라도 요청을 보낼 수 있는 API Path
                                "/api/auth/register",
                                "/api/auth/username/check",
                                "/api/auth/nickname/check",
                                "/api/auth/sms-certifications/send",
                                "/api/auth/sms-certifications/verify",
                                "/api/auth/login",
                                "/api/oauth2/**",
                                "/api/login/oauth2/code/**",
                                "/api/board-cafes",
                                "/api/regions/**",
                                "/api/rankings",
                                "/api/auth/status"
                        ).permitAll()
                        .requestMatchers(
                                // 동일한 URL 요청에 대해서 GET 요청만 permitAll()
                                HttpMethod.GET,
                                "/api/gather-articles",
                                "/api/gather-articles/{gatherArticleId}",
                                "/api/gather-articles/{gatherArticleId}/comments"
                        ).permitAll()
                        // 단순히 로그인한 인증 여부만 확인
//                        .anyRequest().authenticated()
                        // 단순히 로그인하여 인증된것 뿐만 아니라 USER 라는 권한이 있어야만 서버로 요청을 보낼 수 있음
                        // 데이터베이스에 사용자의 권한을 저장할때 'ROLE_USER'가 아닌 'USER' 로 저장되기 때문에 hasRole() 메서드가 아닌 문자열 그대로 권한을 비교하는 hasAuthority() 메서드를 활용
                        .anyRequest().hasAuthority("USER")
                )

                // 추가 코드
                .addFilterAt(customAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(config -> config
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))
                // HTTP 응답 헤더를 설정한다.
                // frameOptions 설정을 sameOrigin 으로 설정하여,
                // 현재 페이지가 동일한 출처의 페이지에서만 포함될 수 있도록 한다.
                .headers(
                        headersConfigurer -> headersConfigurer
                                .frameOptions(
                                        HeadersConfigurer.FrameOptionsConfig::sameOrigin
                                )
                );

        // oauth2 소셜 로그인 구현 코드
        http
                .oauth2Login(oauth2 -> oauth2
//                        .loginPage("/auth/login")
                        .authorizationEndpoint(oAuth2 -> oAuth2
                                .baseUri("/api/oauth2/authorization"))
                        .redirectionEndpoint(oAuth2 -> oAuth2
                                .baseUri("/api/login/oauth2/code/**"))
                        .userInfoEndpoint(userInfoEndpointConfig ->
                                userInfoEndpointConfig
                                        .userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler));

        http.logout(auth -> auth
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(customLogoutSuccessHandler)
                .deleteCookies("JSESSIONID"));

        http    // 하나의 아이디에 대해서 다중 로그인에 대한 처리
                .sessionManagement(auth -> auth
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true));

        http
                .sessionManagement(auth -> auth
                        .sessionFixation()
                        .changeSessionId());

        http
                .sessionManagement(auth -> auth
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .rememberMe(auth -> auth
                        .useSecureCookie(true) // HTTPS 환경에서만 쿠키전송
                        .tokenValiditySeconds(86400));

        return http.build();
    }

    // 새로추가
    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter() throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter();
        customAuthenticationFilter.setAuthenticationManager(authenticationManager());
        customAuthenticationFilter.setAuthenticationSuccessHandler(customAuthenticationSuccessHandler);
        customAuthenticationFilter.setAuthenticationFailureHandler(customAuthenticationFailureHandler);

        customAuthenticationFilter.setSecurityContextRepository(
                new DelegatingSecurityContextRepository(
                        new HttpSessionSecurityContextRepository(),
                        new RequestAttributeSecurityContextRepository()
                ));

        return customAuthenticationFilter;
    }

    // AuthenticationManager 를 반환하는 메서드 등록
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
