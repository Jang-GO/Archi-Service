package com.archiservice.user.component;

import com.archiservice.user.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ContractScheduler {

    private final ContractService contractService;

    @Scheduled(cron = "0 0 0 * * *")
    public void renewContractAction() {
        contractService.renewContract(LocalDate.now());
    }

}

