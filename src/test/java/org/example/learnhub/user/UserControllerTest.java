package org.example.learnhub.user;

import org.example.learnhub.config.SecurityConfiguration;
import org.example.learnhub.user.controller.UserController;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;

@WebMvcTest(UserController.class)
@Import(SecurityConfiguration.class)
public class UserControllerTest {

}