package com.esse.crm.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CorsConfigIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowCorsFromLocalhost4200() throws Exception {
        mockMvc.perform(options("/api/accounts")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,PATCH,OPTIONS"));
    }

    @Test
    void shouldDenyCorsFromOtherOrigins() throws Exception {
        mockMvc.perform(options("/api/accounts")
                .header("Origin", "http://other-domain.com")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
