package ru.yandex.practicum.mybankfront.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GatewayApiClient {

    private final RestClient restClient;
    private final OAuthTokenService tokenService;

    public GatewayApiClient(RestClient gatewayRestClient, OAuthTokenService tokenService) {
        this.restClient = gatewayRestClient;
        this.tokenService = tokenService;
    }

    public <T> T get(String path, OAuth2AuthenticationToken auth, Class<T> responseType) {
        String token = tokenService.getAccessToken(auth);
        return restClient.get()
                .uri(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(responseType);
    }

    public <T> T postJson(String path, Object body, OAuth2AuthenticationToken auth, Class<T> responseType) {
        String token = tokenService.getAccessToken(auth);
        return restClient.post()
                .uri(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(responseType);
    }

    public void patchJson(String path, Object body, OAuth2AuthenticationToken auth) {
        String token = tokenService.getAccessToken(auth);
        restClient.patch()
                .uri(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
