package org.example.learnhub.payment;


import org.example.learnhub.config.SecurityConfiguration;
import org.example.learnhub.config.TokenService;
import org.example.learnhub.exception.EntityNotFoundException;
import org.example.learnhub.payment.controller.PaymentController;
import org.example.learnhub.payment.dto.PurchaseResponse;
import org.example.learnhub.payment.service.PaymentService;
import org.example.learnhub.user.entity.User;
import org.example.learnhub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfiguration.class)
public class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleHierarchy roleHierarchy;

    @MockitoBean
    private PaymentService service;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TokenService tokenService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "CREATOR", "ADMIN"})
    void shouldReturn201WhenUserPurchasesCourse(String role) throws Exception {
        Integer courseId = 1;

        PurchaseResponse response = new PurchaseResponse(
                1,
                courseId,
                "Java Course",
                BigDecimal.valueOf(99.99),
                LocalDateTime.now()
        );

        Collection<? extends GrantedAuthority> authorities = roleHierarchy.getReachableGrantedAuthorities(
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        when(service.purchase(any(), anyInt())).thenReturn(response);

        mockMvc.perform(post("/api/payments/{courseId}", courseId)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        user,
                                        null,
                                        authorities
                                )
                        )))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "CREATOR", "ADMIN"})
    void shouldReturn200WhenPaymentExists(String role) throws Exception {
        Integer paymentId = 1;

        PurchaseResponse response = new PurchaseResponse(
                paymentId,
                1,
                "Java Course",
                BigDecimal.valueOf(99.99),
                LocalDateTime.now()
        );

        Collection<? extends GrantedAuthority> authorities = roleHierarchy.getReachableGrantedAuthorities(
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        when(service.findById(any(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/api/payments/{paymentId}", paymentId)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        user,
                                        null,
                                        authorities
                                )
                        )))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "CREATOR", "ADMIN"})
    void shouldReturn404WhenPaymentDoesNotExist(String role) throws Exception {
        Integer paymentId = 1;

        Collection<? extends GrantedAuthority> authorities = roleHierarchy.getReachableGrantedAuthorities(
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        when(service.findById(any(), anyInt())).thenThrow(new EntityNotFoundException("Payment not found"));

        mockMvc.perform(get("/api/payments/{paymentId}", paymentId)
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        user,
                                        null,
                                        authorities
                                )
                        )))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "CREATOR", "ADMIN"})
    void shouldReturn200WhenUserRequestsPaymentHistory(String role) throws Exception {
        PurchaseResponse response = new PurchaseResponse(
                1,
                1,
                "Java Course",
                BigDecimal.valueOf(99.99),
                LocalDateTime.now()
        );

        List<PurchaseResponse> list = List.of(response);

        Page<PurchaseResponse> page = new PageImpl<>(list, PageRequest.of(0, 10), list.size());

        Collection<? extends GrantedAuthority> authorities = roleHierarchy.getReachableGrantedAuthorities(
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        when(service.history(any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/payments")
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        user,
                                        null,
                                        authorities
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }
}