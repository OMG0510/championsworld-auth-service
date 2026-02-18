package com.shopping.b2c_ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class ChampionsWorldAuthApplicationTests {

	@Test
	void contextLoads() {
	}
	@Test
	void generatePassword() {
		System.out.println(new BCryptPasswordEncoder().encode("admin123"));
	}

}
