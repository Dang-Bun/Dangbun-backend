package com.dangbun;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@SpringBootTest
@ActiveProfiles("test")
class DangbunApplicationTests {

	@MockBean
	private S3Presigner s3Presigner;
	
	@Test
	void contextLoads() {
	}

}
