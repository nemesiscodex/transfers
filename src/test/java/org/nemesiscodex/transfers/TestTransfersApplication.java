package org.nemesiscodex.transfers;

import org.springframework.boot.SpringApplication;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestTransfersApplication {

    public static void main(String[] args) {
        SpringApplication.from(TransfersApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
