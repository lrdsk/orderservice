package com.example.orderservice.controller;

import com.example.orderservice.BaseIntegrationTest;
import com.example.orderservice.dto.OrderRequestDTO;
import com.example.orderservice.dto.OrderStatusRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@Sql(scripts = {
        "/sql/clear-all.sql",
        "/sql/insert-into-users.sql",
        "/sql/insert-into-orders.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class OrderControllerTest extends BaseIntegrationTest {

    private static final String USER1_USERNAME = "username";
    private static final String ADMIN_USERNAME = "admin";

    // Вспомогательный метод для установки аутентификации
    private void setAuthentication(String username, String role) {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        UserDetails userDetails = new User(username, "", authorities);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // Очистка контекста после каждого теста
    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // =========================== POST /api/orders ===========================

    @Test
    void createOrder_shouldReturnOk_whenValidRequest() throws Exception {
        setAuthentication(USER1_USERNAME, "USER");

        OrderRequestDTO request = new OrderRequestDTO("New test order");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    // =========================== GET /api/orders ===========================

    @Test
    void getAllOrdersForCurrentUser_shouldReturnPageOfOrders() throws Exception {
        setAuthentication(USER1_USERNAME, "USER");

        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].description", containsString("user1")))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void getAllOrdersForCurrentUser_shouldReturnEmptyPage_whenUserHasNoOrders() throws Exception {
        setAuthentication("username2", "USER");

        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    // =========================== GET /api/orders/all ===========================

    @Test
    void getAllOrders_shouldReturnAllOrders() throws Exception {
        mockMvc.perform(get("/api/orders/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    // =========================== PUT /api/orders/{id} ===========================

    @Test
    void changeOrderStatus_shouldUpdateStatusAndReturnOrder() throws Exception {
        UUID orderId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        OrderStatusRequestDTO statusRequest = new OrderStatusRequestDTO("COMPLETED");
        String json = objectMapper.writeValueAsString(statusRequest);

        mockMvc.perform(put("/api/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderId.toString())))
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    // =========================== DELETE /api/orders/{id} ===========================

    @Test
    void deleteOrderById_shouldDeleteOrderAndReturnOk() throws Exception {
        setAuthentication("username", "USER");
        UUID orderId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isOk());

        setAuthentication(USER1_USERNAME, "USER");
        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }
}