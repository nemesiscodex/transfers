package org.nemesiscodex.transfers.naive.repository;

import java.util.UUID;
import org.nemesiscodex.transfers.naive.entity.NaiveTransfer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NaiveTransferRepository extends ReactiveCrudRepository<NaiveTransfer, UUID> {
}

