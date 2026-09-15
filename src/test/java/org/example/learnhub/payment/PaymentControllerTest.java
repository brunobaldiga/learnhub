package org.example.learnhub.payment;


import org.example.learnhub.config.SecurityConfiguration;
import org.example.learnhub.payment.controller.PaymentController;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfiguration.class)
public class PaymentControllerTest {

}