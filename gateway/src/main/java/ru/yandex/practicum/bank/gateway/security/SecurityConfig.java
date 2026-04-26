package ru.yandex.practicum.bank.gateway.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.bank.common.security.KeycloakRealmRoleConverter;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static org.springframework.core.convert.converter.Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthConverter() {
        JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
        delegate.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(delegate);
    }

    @Bean
    @Order(1)
    @ConditionalOnProperty(name = "bank.security.enabled", havingValue = "true", matchIfMissing = true)
    SecurityWebFilterChain oauthChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers("/api/accounts/**").hasRole("ACCOUNTS_ACCESS")
                        .pathMatchers("/api/cash/**").hasRole("CASH_ACCESS")
                        .pathMatchers("/api/transfers/**").hasRole("TRANSFER_ACCESS")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
                )
                .build();
    }

    @Bean
    @Order(2)
    @ConditionalOnProperty(name = "bank.security.enabled", havingValue = "false")
    SecurityWebFilterChain openChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex.anyExchange().permitAll())
                .build();
    }
}
