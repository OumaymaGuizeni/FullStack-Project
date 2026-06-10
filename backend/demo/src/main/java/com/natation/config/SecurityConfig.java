package com.natation.config;

import com.natation.service.AuthService;
import com.natation.dto.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private ObjectProvider<AuthService> authServiceProvider;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Value("${app.oauth2.redirect-uri:http://localhost:4200/oauth-callback}")
    private String oauth2RedirectUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                  ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**").disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/clubs/**").permitAll()
                        .requestMatchers("/api/users/**").permitAll()
                        .requestMatchers("/api/chatbot/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .sessionManagement(sess -> sess.sessionCreationPolicy(IF_REQUIRED))
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestResolver(
                                        authorizationRequestResolver(clientRegistrationRepository)))
                        .successHandler((request, response, authentication) -> {
                            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
                            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                            String registrationId = oauthToken.getAuthorizedClientRegistrationId().toUpperCase();
                            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                                    oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());
                            AuthResponse authResponse = handleOAuth2User(registrationId, oauthUser, authorizedClient);
                            redirectWithAuthResponse(response, authResponse);
                        })
                        .failureHandler((request, response, exception) -> response.sendRedirect(
                                UriComponentsBuilder.fromUriString(oauth2RedirectUri)
                                        .queryParam("error", exception.getMessage())
                                        .build()
                                        .encode()
                                        .toUriString())))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        DefaultOAuth2AuthorizationRequestResolver defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");

        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(jakarta.servlet.http.HttpServletRequest request) {
                return customizeAuthorizationRequest(defaultResolver.resolve(request));
            }

            @Override
            public OAuth2AuthorizationRequest resolve(jakarta.servlet.http.HttpServletRequest request,
                                                      String clientRegistrationId) {
                return customizeAuthorizationRequest(defaultResolver.resolve(request, clientRegistrationId));
            }
        };
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null) {
            return null;
        }

        Map<String, Object> additionalParameters = new HashMap<>(authorizationRequest.getAdditionalParameters());
        if (authorizationRequest.getAuthorizationUri().contains("accounts.google.com")) {
            additionalParameters.put("prompt", "select_account");
        }

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .build();
    }

    private AuthResponse handleOAuth2User(String provider, OAuth2User oauthUser,
                                          OAuth2AuthorizedClient authorizedClient) {
        String email = oauthUser.getAttribute("email");
        String username = oauthUser.getAttribute("name");
        String providerId = oauthUser.getAttribute("sub");

        if ("GITHUB".equals(provider)) {
            providerId = String.valueOf(oauthUser.getAttribute("id"));
            if (username == null || username.isBlank()) {
                username = oauthUser.getAttribute("login");
            }
            if (email == null || email.isBlank()) {
                email = fetchPrimaryGitHubEmail(authorizedClient);
            }
        }

        return authServiceProvider.getObject().socialLogin(email, username, provider, providerId);
    }

    private String fetchPrimaryGitHubEmail(OAuth2AuthorizedClient authorizedClient) {
        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            return null;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(authorizedClient.getAccessToken().getTokenValue());
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            List<Map<String, Object>> emails = response.getBody();
            if (emails == null) {
                return null;
            }

            return emails.stream()
                    .filter(email -> Boolean.TRUE.equals(email.get("primary")))
                    .filter(email -> Boolean.TRUE.equals(email.get("verified")))
                    .map(email -> String.valueOf(email.get("email")))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.err.println("Could not fetch GitHub primary email: " + e.getMessage());
            return null;
        }
    }

    private void redirectWithAuthResponse(jakarta.servlet.http.HttpServletResponse response,
                                          AuthResponse authResponse) throws IOException {
        String redirectUrl = UriComponentsBuilder.fromUriString(oauth2RedirectUri)
                .queryParam("token", authResponse.getToken())
                .queryParam("id", authResponse.getId())
                .queryParam("email", authResponse.getEmail())
                .queryParam("username", authResponse.getUsername())
                .queryParam("role", authResponse.getRole())
                .build()
                .encode()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setExposedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
