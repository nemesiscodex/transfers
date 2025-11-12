package org.nemesiscodex.transfers.core.service;

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
                String encodedPassword = passwordEncoder.encode(request.password().trim());
                User newUser = User.from(request, encodedPassword);
                return userRepository.save(newUser)
                    .flatMap(user -> userRepository.findById(user.id()));
            }));
    }

    public Mono<AuthenticationResult> login(LoginRequest request) {
        return userRepository.findByUsername(request.username())
            .filter(user -> passwordEncoder.matches(request.password(), user.passwordHash()))
            .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid username or password")))
            .map(user -> new AuthenticationResult(user, this.jwtService.generateToken(user.id(), user.username())));
    }

    private Mono<Void> validateUniqueness(SignupRequest request) {
        var existEmail = userRepository.existsByEmail(request.email());
        var existUsername = userRepository.existsByUsername(request.username());
        return Mono.zip(existEmail, existUsername)
            .flatMap(tuple -> {
                if (Boolean.TRUE.equals(tuple.getT1()) || Boolean.TRUE.equals(tuple.getT2())) {
                    return Mono.error(new IllegalStateException("Username or email already exists"));
                }
                return Mono.empty();
            });
    }

    public record AuthenticationResult(User user, String token) {
    }
}
