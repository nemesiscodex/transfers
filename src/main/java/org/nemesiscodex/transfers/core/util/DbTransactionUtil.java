package org.nemesiscodex.transfers.core.util;

import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class DbTransactionUtil {
    @Transactional(propagation = Propagation.REQUIRED)
    public <T> Mono<T> runInTransaction(Supplier<Mono<T>> callable) {
        return callable.get();
    }
}
