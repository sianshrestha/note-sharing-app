package com.sian.noteshare;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class NoteshareApplicationTests {

    @MockitoBean
    JavaMailSender mailSender;

    @MockitoBean
    private OAuth2AuthorizedClientService authorizedClientService;

	@Test
	void contextLoads() {
	}



}
