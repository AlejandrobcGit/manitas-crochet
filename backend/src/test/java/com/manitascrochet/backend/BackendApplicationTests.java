package com.manitascrochet.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Smoke test requiere MongoDB externo; las pruebas unitarias no deben conectarse a infraestructura real")
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
