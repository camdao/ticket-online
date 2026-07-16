package com.ticket_online.global.config.security;

import static com.ticket_online.global.common.constants.SwaggerUrlConstants.getSwaggerUrls;
import static com.ticket_online.global.common.constants.EnvironmentConstants.*;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.security.config.Customizer.withDefaults;

import com.ticket_online.domain.auth.application.JwtTokenService;
import com.ticket_online.global.common.constants.UrlConstants;
import com.ticket_online.global.config.annotion.ConditionalOnProfile;
import com.ticket_online.global.security.*;
import com.ticket_online.global.util.CookieUtil;
import com.ticket_online.global.util.SpringEnvironmentUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final SpringEnvironmentUtil springEnvironmentUtil;
    private final JwtTokenService jwtTokenService;
    private final CookieUtil cookieUtil;

    @Value("${swagger.user}")
    private String swaggerUser;

    @Value("${swagger.password}")
    private String swaggerPassword;

    private void defaultFilterChain(HttpSecurity http) throws Exception {
        http.httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        UserDetails user =
                User.withUsername(swaggerUser)
                        .password(passwordEncoder().encode(swaggerPassword))
                        .roles("SWAGGER")
                        .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    @Order(1)
    @ConditionalOnProfile({PROD, DEV, LOCAL})
    public SecurityFilterChain swaggerFilterChain(HttpSecurity http) throws Exception {
        defaultFilterChain(http);

        http.securityMatcher(getSwaggerUrls()).httpBasic(withDefaults());

        http.authorizeHttpRequests(
                (springEnvironmentUtil.isDevProfile() || springEnvironmentUtil.isProdProfile())
                        ? authorize -> authorize.anyRequest().authenticated()
                        : authorize -> authorize.anyRequest().permitAll());

        return http.build();
    }




    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        defaultFilterChain(http);

        http.authorizeHttpRequests(
                authorize ->
                        authorize
                                .requestMatchers(
                                        "/api/v1/auth/register",
                                        "/api/v1/auth/login",
                                        "/api/v1/auth/refresh")
                                .permitAll()
                                .requestMatchers("/api/v1/auth/logout")
                                .authenticated()
                                .anyRequest()
                                .permitAll());
        http.exceptionHandling(
                exception ->
                        exception.authenticationEntryPoint(
                                (request, response, authException) -> response.setStatus(401)));

        http.addFilterBefore(
                jwtAuthenticationFilter(jwtTokenService, cookieUtil),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        if (springEnvironmentUtil.isProdProfile()) {
            configuration.addAllowedOriginPattern(UrlConstants.PROD_DOMAIN_URL.getValue());
        }

        if (springEnvironmentUtil.isDevProfile()) {
            configuration.addAllowedOriginPattern(UrlConstants.DEV_DOMAIN_URL.getValue());
            configuration.addAllowedOriginPattern(UrlConstants.LOCAL_DOMAIN_URL.getValue());
            configuration.addAllowedOriginPattern(UrlConstants.LOCAL_SECURE_DOMAIN_URL.getValue());
        }

        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader(SET_COOKIE);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenService jwtTokenService, CookieUtil cookieUtil) {
        return new JwtAuthenticationFilter(jwtTokenService, cookieUtil);
    }
}
