package org.nemesiscodex.transfers.core.repository;

import java.util.UUID;
import org.nemesiscodex.transfers.core.entity.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, UUID> {

    Mono<User> findByUsername(String username);

    Mono<Boolean> existsByUsername(String username);

    Mono<Boolean> existsByEmail(String email);
}
