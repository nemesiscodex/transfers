package org.nemesiscodex.transfers.core.security;

import org.nemesiscodex.transfers.core.repository.UserRepository;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    public ReactiveUserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return this.userRepository.findByUsername(username)
            .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)))
            .map(user -> User.withUsername(user.username())
                .password(user.passwordHash())
                // Security: grant the default role; future enhancements can pull roles from persistence
                .roles("USER")
                .build());
    }
}

