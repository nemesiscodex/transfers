package org.nemesiscodex.transfers.core.controller;

import org.nemesiscodex.transfers.core.dto.UserResponse;
import org.nemesiscodex.transfers.core.entity.User;
import org.nemesiscodex.transfers.core.repository.UserRepository;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public Mono<UserResponse> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
            .map(context -> (UserDetails) context.getAuthentication().getPrincipal())
            .flatMap(userDetails -> this.userRepository.findByUsername(userDetails.getUsername()))
            .map(this::toUserResponse);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
            user.id(),
            user.username(),
            user.email(),
            user.createdAt(),
            user.updatedAt()
        );
    }
}
