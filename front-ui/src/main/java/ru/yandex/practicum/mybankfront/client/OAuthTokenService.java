package ru.yandex.practicum.mybankfront.client;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class OAuthTokenService {

    private final ObjectProvider<OAuth2AuthorizedClientService> authorizedClientServiceProvider;

    public OAuthTokenService(ObjectProvider<OAuth2AuthorizedClientService> authorizedClientServiceProvider) {
        this.authorizedClientServiceProvider = authorizedClientServiceProvider;
    }

    public String getAccessToken(OAuth2AuthenticationToken authentication) {
        if (authentication == null) {
            throw new IllegalStateException("User is not authenticated");
        }

        OAuth2AuthorizedClientService service = authorizedClientServiceProvider.getIfAvailable();
        if (service == null) {
            throw new IllegalStateException("OAuth2AuthorizedClientService is not available (check oauth2 client config)");
        }

        OAuth2AuthorizedClient client = service.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        if (client == null || client.getAccessToken() == null) {
            throw new IllegalStateException("No OAuth2AuthorizedClient or access token found for current user");
        }

        return client.getAccessToken().getTokenValue();
    }
}
