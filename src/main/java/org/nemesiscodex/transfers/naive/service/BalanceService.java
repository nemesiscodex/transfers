package org.nemesiscodex.transfers.naive.service;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.nemesiscodex.transfers.naive.entity.NaiveBalance;
import org.nemesiscodex.transfers.naive.repository.NaiveBalanceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BalanceService {
    private final NaiveBalanceRepository balanceRepository;

    public Mono<BigDecimal> getBalance(UUID userId) {
        return balanceRepository.findByUserIdAndCloseLedgerIdIsNull(userId)
            .map(NaiveBalance::amount)
            .defaultIfEmpty(BigDecimal.ZERO);
    }
}
