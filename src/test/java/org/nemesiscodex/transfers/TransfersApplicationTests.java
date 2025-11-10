package org.nemesiscodex.transfers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.nemesiscodex.transfers.config.TestContainers;

@Import(TestContainers.class)
@SpringBootTest
class TransfersApplicationTests {

	@Test
	void contextLoads() {
	}

}
