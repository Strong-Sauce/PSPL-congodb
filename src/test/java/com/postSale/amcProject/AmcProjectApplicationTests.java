package com.postSale.amcProject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.neo4j.uri=${NEO4J_URI}",
		"spring.neo4j.authentication.username=${NEO4J_USERNAME}",
		"spring.neo4j.authentication.password=${NEO4J_PASSWORD}"
})
class AmcProjectApplicationTests {

	@Test
	void contextLoads() {
	}
}