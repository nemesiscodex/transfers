package org.nemesiscodex.transfers.core.service;

import java.time.Instant;
import java.util.UUID;
import org.nemesiscodex.transfers.core.dto.LoginRequest;
import org.nemesiscodex.transfers.core.dto.SignupRequest;
import org.nemesiscodex.transfers.core.entity.User;
import org.nemesiscodex.transfers.core.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Mono<User> signup(SignupRequest request) {
        return validateUniqueness(request)
            .then(Mono.defer(() -> {
                User newUser = User.builder()
                    .username(request.username().trim())
                    .passwordHash(passwordEncoder.encode(request.password()))
                    .email(request.email().trim().toLowerCase())
                    .build();
                return userRepository.save(newUser)
                    .flatMap(user -> userRepository.findById(user.id()));
            }));
    }

    public Mono<AuthenticationResult> login(LoginRequest request) {
        return userRepository.findByUsername(request.username())
            .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid username or password")))
            .filter(user -> passwordEncoder.matches(request.password(), user.passwordHash()))
            .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid username or password")))
            .map(user -> new AuthenticationResult(user, this.jwtService.generateToken(user.id(), user.username())));
    }

    private Mono<Void> validateUniqueness(SignupRequest request) {
        return userRepository.existsByUsername(request.username())
            .flatMap(usernameTaken -> {
                if (Boolean.TRUE.equals(usernameTaken)) {
                    return Mono.<Void>error(new IllegalStateException("Username already exists"));
                }
                return userRepository.existsByEmail(request.email())
                    .flatMap(emailTaken -> {
                        if (Boolean.TRUE.equals(emailTaken)) {
                            return Mono.<Void>error(new IllegalStateException("Email already exists"));
                        }
                        return Mono.<Void>empty();
                    });
            });
    }

    public record AuthenticationResult(User user, String token) {
    }
}
