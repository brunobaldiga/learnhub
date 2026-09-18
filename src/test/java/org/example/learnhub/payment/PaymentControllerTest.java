package org.example.learnhub.payment;

import org.example.learnhub.config.JwtAuthenticationEntryPoint;
import org.example.learnhub.config.SecurityConfiguration;
import org.example.learnhub.config.TokenService;
import org.example.learnhub.exception.EntityNotFoundException;
import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;
import org.example.learnhub.payment.controller.PaymentController;
import org.example.learnhub.payment.dto.PurchaseResponse;
import org.example.learnhub.payment.service.PaymentService;
import org.example.learnhub.user.dto.RoleType;
import org.example.learnhub.user.entity.User;
import org.example.learnhub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfiguration.class)
class PaymentControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PaymentService service;
    @MockitoBean
    UserRepository userRepository;
    @MockitoBean
    TokenService tokenService;
    @MockitoBean
    JwtAuthenticationEntryPoint authenticationEntryPoint;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).roleType(RoleType.USER).build();
    }

    private UsernamePasswordAuthenticationToken auth(RoleType role) {
        User principal = User.builder().id(1).roleType(role).build();
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private PurchaseResponse response(Integer id) {
        return new PurchaseResponse(
                id, 10, "Java Course", BigDecimal.valueOf(99.99), CurrencyCode.USD,
                BigDecimal.valueOf(5.25), CurrencyCode.BRL, BigDecimal.valueOf(524.95), LocalDateTime.now()
        );
    }

    @ParameterizedTest
    @EnumSource(RoleType.class)
    void shouldAllowAllAuthenticatedRolesToPurchase(RoleType role) throws Exception {
        when(service.purchase(any(), eq(10), any())).thenReturn(response(1));

        mockMvc.perform(post("/api/payments/10")
                        .with(authentication(auth(role)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currency":"BRL"
                                }"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paidCurrency").value("BRL"))
                .andExpect(jsonPath("$.paidPrice").value(524.95));
    }

    @ParameterizedTest
    @EnumSource(RoleType.class)
    void shouldFindOwnedPayment(RoleType role) throws Exception {
        when(service.findById(any(), eq(1))).thenReturn(response(1));

        mockMvc.perform(get("/api/payments/1")
                        .with(authentication(auth(role))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldMapMissingPaymentTo404() throws Exception {
        when(service.findById(any(), eq(1))).thenThrow(new EntityNotFoundException("Payment not found"));

        mockMvc.perform(get("/api/payments/1")
                        .with(authentication(auth(RoleType.USER))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnPaymentHistory() throws Exception {
        PurchaseResponse response = response(1);
        when(service.history(any(), any(), any(), any())).thenReturn(
                new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1)
        );

        mockMvc.perform(get("/api/payments")
                        .param("page", "0")
                        .param("size", "10")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31")
                        .with(authentication(auth(RoleType.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
