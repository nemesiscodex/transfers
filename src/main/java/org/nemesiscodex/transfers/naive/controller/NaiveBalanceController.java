package org.nemesiscodex.transfers.naive.controller;

import lombok.RequiredArgsConstructor;
import org.nemesiscodex.transfers.core.repository.UserRepository;
import org.nemesiscodex.transfers.naive.dto.BalanceResponse;
import org.nemesiscodex.transfers.naive.service.BalanceService;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/naive/balance")
@RequiredArgsConstructor
public class NaiveBalanceController {

    private final BalanceService balanceService;
    private final UserRepository userRepository;

    @GetMapping
    public Mono<BalanceResponse> getCurrentBalance() {
        return ReactiveSecurityContextHolder.getContext()
            .map(context -> (UserDetails) context.getAuthentication().getPrincipal())
            .flatMap(userDetails -> this.userRepository.findByUsername(userDetails.getUsername()))
            .flatMap(user -> this.balanceService.getBalance(user.id())
                .map(amount -> new BalanceResponse(user.id(), amount)));
    }
}

