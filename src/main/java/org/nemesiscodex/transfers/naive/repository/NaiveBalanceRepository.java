package org.nemesiscodex.transfers.naive.repository;

import java.util.UUID;
import org.nemesiscodex.transfers.naive.entity.NaiveBalance;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface NaiveBalanceRepository extends ReactiveCrudRepository<NaiveBalance, UUID> {
    Mono<NaiveBalance> findByUserIdAndCloseLedgerIdIsNull(UUID userId);
}

