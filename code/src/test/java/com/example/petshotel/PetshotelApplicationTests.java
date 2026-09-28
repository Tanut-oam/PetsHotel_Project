package com.example.petshotel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class PetshotelApplicationTests {

	@MockitoBean
    private JavaMailSender mailSender;
	
	@Test
	void contextLoads() {
	}

}
