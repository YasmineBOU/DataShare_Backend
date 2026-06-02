package com.openclassrooms.datashare.configuration.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

public class SpringSecurityConfigWebIT {

    @RestController
    static class TestController {
        @GetMapping("/api/login")
        public String login() {
            return "ok";
        }

        @GetMapping("/api/files/list")
        @PreAuthorize("isAuthenticated()")
        public List<String> listFiles() {
            return List.of("file1.txt", "file2.txt");
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

    // @Test
    // void privateEndpointShouldNotBeAccessibleWithoutAuth() throws Exception {
    // var mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
    // .standaloneSetup(new TestController())
    // .build();

    // mockMvc.perform(get("/api/files/list")
    // .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
    // .anonymous()))
    // .andExpect(status().isUnauthorized());
    // }

}
