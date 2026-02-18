package com.shopping.b2c_ecommerce.service;

import com.shopping.b2c_ecommerce.dto.GoogleTokenResponse;
import com.shopping.b2c_ecommerce.dto.GoogleUserInfo;
import com.shopping.b2c_ecommerce.exception.GoogleOAuthException;
import com.shopping.b2c_ecommerce.exception.GoogleUserInfoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleOAuthService {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuthService.class);

    @Value("${google.oauth.client-id}")
    private String clientId;

    @Value("${google.oauth.client-secret}")
    private String clientSecret;

    @Value("${google.oauth.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    public GoogleTokenResponse getToken(String code) {

        log.info("Requesting Google OAuth token");

        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("code", code);
            params.add("redirect_uri", redirectUri);
            params.add("grant_type", "authorization_code");

            GoogleTokenResponse response = restTemplate.postForObject(
                    "https://oauth2.googleapis.com/token",
                    params,
                    GoogleTokenResponse.class
            );

            if (response == null || response.getAccessToken() == null) {
                throw new GoogleOAuthException("Invalid Google OAuth response");
            }

            log.info("Google OAuth token received");
            return response;

        } catch (Exception ex) {
            log.error("Google OAuth token request failed", ex);
            throw new GoogleOAuthException("Google authentication failed");
        }
    }


    public GoogleUserInfo getUserInfo(String accessToken) {

        log.info("Requesting Google user info");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<GoogleUserInfo> response =
                    restTemplate.exchange(
                            "https://www.googleapis.com/oauth2/v2/userinfo",
                            HttpMethod.GET,
                            entity,
                            GoogleUserInfo.class
                    );

            if (response.getBody() == null) {
                throw new GoogleUserInfoException();
            }

            log.info("Google user info retrieved successfully");
            return response.getBody();

        } catch (Exception ex) {
            log.error("Failed to fetch Google user info", ex);
            throw new GoogleUserInfoException();
        }
    }

}
