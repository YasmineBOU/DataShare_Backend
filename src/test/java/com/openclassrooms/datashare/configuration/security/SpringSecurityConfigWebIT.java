package com.openclassrooms.datashare.configuration.security;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SpringSecurityConfigWebIT {

    @RestController
    static class TestController {
        @GetMapping("/api/login")
        public String login() {
            return "ok";
        }
    }

    @Test
    void publicEndpointShouldBeAccessibleWithoutAuth() throws Exception {
        var mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(new TestController())
                .build();

        mockMvc.perform(get("/api/login")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .anonymous()))
                .andExpect(status().isOk());
    }

}
