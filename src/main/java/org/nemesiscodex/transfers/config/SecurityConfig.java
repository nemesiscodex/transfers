package org.nemesiscodex.transfers.config;

import java.time.Instant;
import org.nemesiscodex.transfers.core.repository.UserRepository;
import org.nemesiscodex.transfers.core.security.JwtAuthenticationWebFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveUserDetailsPasswordService reactiveUserDetailsPasswordService(
        ObjectProvider<UserRepository> userRepositoryProvider,
        PasswordEncoder passwordEncoder
    ) {
        UserRepository userRepository = userRepositoryProvider.getIfAvailable();
        if (userRepository != null) {
            return (userDetails, newPassword) -> userRepository.findByUsername(userDetails.getUsername())
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + userDetails.getUsername())))
                .flatMap(existing -> {
                    var updated = existing.toBuilder()
                        .passwordHash(passwordEncoder.encode(newPassword))
                        .build();
                    return userRepository.save(updated);
                })
                .map(saved -> User.withUsername(saved.username())
                    .password(saved.passwordHash())
                    .roles("USER")
                    .build());
        }
        return (userDetails, newPassword) -> Mono.just(
            User.withUsername(userDetails.getUsername())
                .password(passwordEncoder.encode(newPassword))
                .authorities(userDetails.getAuthorities())
                .build()
        );
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
        ServerHttpSecurity http,
        ObjectProvider<JwtAuthenticationWebFilter> jwtAuthenticationWebFilterProvider) {
        var httpSpec = http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .logout(ServerHttpSecurity.LogoutSpec::disable)
            // Security: store no session state because JWTs provide stateless authentication
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/auth/signup", "/auth/login").permitAll()
                .pathMatchers("/actuator/**").permitAll()
                .anyExchange().authenticated()
            )
            .exceptionHandling(spec -> spec
                .authenticationEntryPoint((exchange, ex) -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                })
                .accessDeniedHandler((exchange, denied) -> {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                })
            );

        JwtAuthenticationWebFilter jwtFilter = jwtAuthenticationWebFilterProvider.getIfAvailable();
        if (jwtFilter != null) {
            httpSpec.addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION);
        }

        return httpSpec.build();
    }
}
