package org.nemesiscodex.transfers.core.security;

import org.nemesiscodex.transfers.core.service.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnBean(org.nemesiscodex.transfers.core.service.JwtService.class)
public class JwtAuthenticationWebFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final ReactiveUserDetailsService userDetailsService;

    public JwtAuthenticationWebFilter(JwtService jwtService, ReactiveUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.justOrEmpty(extractToken(exchange))
            .flatMap(token -> authenticate(token)
                .flatMap(authentication -> chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))))
            .switchIfEmpty(chain.filter(exchange));
    }

    private Mono<Authentication> authenticate(String token) {
        return this.jwtService.extractUsername(token)
            .flatMap(username -> this.userDetailsService.findByUsername(username)
                .filter(userDetails -> this.jwtService.isTokenValid(token, userDetails.getUsername()))
                .map(userDetails -> buildAuthentication(userDetails, token))
            );
    }

    private Authentication buildAuthentication(UserDetails userDetails, String token) {
        return UsernamePasswordAuthenticationToken.authenticated(
            userDetails,
            token,
            userDetails.getAuthorities()
        );
    }

    private String extractToken(ServerWebExchange exchange) {
        return exchange.getRequest()
            .getHeaders()
            .getOrEmpty(HttpHeaders.AUTHORIZATION)
            .stream()
            .filter(value -> value.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length()))
            .findFirst()
            .map(value -> value.substring(BEARER_PREFIX.length()).trim())
            .filter(value -> !value.isBlank())
            .orElse(null);
    }
}
