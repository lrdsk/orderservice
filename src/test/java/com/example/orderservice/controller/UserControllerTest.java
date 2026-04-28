package com.example.orderservice.controller;

import com.example.orderservice.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest extends BaseIntegrationTest {
    @Test
    @Sql(scripts = {"/sql/clear-all.sql", "/sql/insert-into-users.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAllUsers_shouldReturnList_whenAdmin() throws Exception {
        //given when then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @Sql(scripts = {"/sql/clear-all.sql", "/sql/insert-into-users.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void deleteUserById_shouldDeleteUserAndReturnOk() throws Exception {
        //given
        UUID userId = UUID.fromString("f64a7fbb-00d8-4d15-b0e4-6e8ad74482cb");

        //when then
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
