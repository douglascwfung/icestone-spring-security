package net.icestone.springsecurity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import net.icestone.springsecurity.controllers.LoginController;

@SpringBootTest
class IcestoneSpringSecurityApplicationTests {

	@Autowired
	private LoginController controller;

	@Test
	void contextLoads()throws Exception {
	    assertThat(controller).isNotNull();
	     }
	
}
