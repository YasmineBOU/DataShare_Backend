package com.openclassrooms.datashare.controller;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.openclassrooms.datashare.configuration.security.JwtAuthenticationFilter;

import com.openclassrooms.datashare.configuration.security.SecurityConstants;
import com.openclassrooms.datashare.dto.AuthDTO;
import com.openclassrooms.datashare.dto.RegisterDTO;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.handler.RestExceptionHandler;
import com.openclassrooms.datashare.service.UserService;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RestExceptionHandler.class)
public class UserControllerTest {

    private static final String EMAIL = "john.doe@example.com";
    private static final String PASSWORD = "Password1";

    private static final Map<String, String> URLS_BY_METHOD = Map.of(
            "register", "/api/register",
            "login", "/api/login",
            "authUser", "/api/auth/me",
            "logout", "/api/logout");

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private Map<String, Object> getResponseBodyAsMap(MvcResult mvcResult) throws Exception {
        String responseContent = mvcResult.getResponse().getContentAsString();
        return objectMapper.readValue(responseContent, Map.class);
    }

    @Nested
    @Tag("register")
    @DisplayName("Tests for register endpoint")
    class RegisterTests {
        private String URL;

        @BeforeEach
        void setUp() {
            URL = URLS_BY_METHOD.get("register");
        }

        @Test
        @DisplayName("Given a null user, when register is called, then a BadRequest status is returned.")
        public void test_register_with_null_user_returns_bad_request() throws Exception {
            // GIVEN
            RegisterDTO registerDTO = new RegisterDTO();

            // WHEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL)
                    .content(objectMapper.writeValueAsString(registerDTO))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @DisplayName("Given an existing user, when register is called, then a conflict status is returned.")
        public void test_register_with_existing_user_returns_conflict() throws Exception {
            // GIVEN
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setEmail(EMAIL);
            registerDTO.setPassword(PASSWORD);

            doThrow(new IllegalArgumentException("User already exists")).when(userService).register(registerDTO);

            // WHEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL)
                    .content(objectMapper.writeValueAsString(registerDTO))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @DisplayName("Given a valid user, when register is called, then user is created")
        public void test_register_with_valid_user_should_create_user() throws Exception {
            // GIVEN
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setEmail(EMAIL);
            registerDTO.setPassword(PASSWORD);

            // WHEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL)
                    .content(objectMapper.writeValueAsString(registerDTO))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isCreated());

            // THEN
            verify(userService, times(1)).register(registerDTO);
        }

        @Test
        @DisplayName("Given a user with password that does not meet complexity requirements, when register is called, then a BadRequest status is returned.")
        public void test_register_with_invalid_password_returns_bad_request() throws Exception {
            // GIVEN
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setEmail(EMAIL);
            registerDTO.setPassword("weakpassword");

            // WHEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL)
                    .content(objectMapper.writeValueAsString(registerDTO))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

    }

    @Nested
    @Tag("login")
    @DisplayName("Tests for login endpoint")
    class LoginTests {
        private String URL;

        @BeforeEach
        void setUp() {
            URL = URLS_BY_METHOD.get("login");
        }

        @Test
        @DisplayName("Given null credentials, when login is called, then a BadRequest status is returned.")
        public void test_login_with_null_credentials_returns_bad_request() throws Exception {

            // WHEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @DisplayName("Given invalid credentials, when login is called, then an BadCredentialsException is returned.")
        public void test_login_with_invalid_credentials_returns_badRequest() throws Exception {
            // GIVEN
            when(userService.login("unexistingEmail@example", "anyPassword"))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            // WHEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @DisplayName("Given valid credentials, when login is called, then JWT token is generated and cookie is set")
        public void test_login_with_valid_credentials_should_return_success_and_set_cookie() throws Exception {
            // GIVEN
            AuthDTO validAuthDTO = new AuthDTO();
            validAuthDTO.setEmail(EMAIL);
            validAuthDTO.setPassword(PASSWORD);
            String jwtToken = "valid-jwt-token-123";
            when(userService.login(EMAIL, PASSWORD)).thenReturn(jwtToken);

            // WHEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL)
                    .content(objectMapper.writeValueAsString(validAuthDTO))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Logged successfully !"))
                    .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.SET_COOKIE))
                    .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.SET_COOKIE,
                            org.hamcrest.Matchers.containsString(SecurityConstants.AUTH_TOKEN_COOKIE_NAME)))
                    .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.SET_COOKIE,
                            org.hamcrest.Matchers
                                    .containsString("Max-Age=" + SecurityConstants.AUTH_COOKIE_MAX_AGE_SECONDS)))
                    .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.SET_COOKIE,
                            org.hamcrest.Matchers.containsString("HttpOnly")))
                    .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.SET_COOKIE,
                            org.hamcrest.Matchers.containsString("Secure"))); // If cookieSecure=true

            // THEN
            verify(userService, times(1)).login(validAuthDTO.getEmail(), validAuthDTO.getPassword());
        }
    }

    @Nested
    @Tag("auth/me")
    @DisplayName("Tests for auth me endpoint")
    class AuthMeTests {
        private String URL;

        @BeforeEach
        void setUp() {
            URL = URLS_BY_METHOD.get("authUser");
        }

        @Test
        @DisplayName("Given an unauthenticated request, when me is called, then a 401 with authenticated=false is returned.")
        public void test_me_with_unauthenticated_request_returns_401_and_authenticated_false() throws Exception {
            // WHEN
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andReturn();

            Map<String, Object> responseBody = getResponseBodyAsMap(mvcResult);

            // THEN
            assertThat(responseBody).containsEntry("authenticated", false);
            assertThat(responseBody).containsEntry("email", null);
        }

        @Test
        @DisplayName("Given an authenticated request, when me is called, then a 200 with authenticated=true and email is returned.")
        public void test_me_with_authenticated_request_returns_200_and_authenticated_true_with_email()
                throws Exception {
            // GIVEN
            User user = new User();
            user.setEmail(EMAIL);

            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // WHEN
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();

            Map<String, Object> responseBody = getResponseBodyAsMap(mvcResult);

            // THEN
            assertThat(responseBody).containsEntry("authenticated", true);
            assertThat(responseBody).containsEntry("email", EMAIL);
        }

        @AfterEach
        void tearDown() {
            SecurityContextHolder.clearContext();
        }

    }

    @Nested
    @Tag("logout")
    @DisplayName("Tests for logout endpoint")
    class LogoutTests {
        private String URL;

        @BeforeEach
        void setUp() {
            URL = URLS_BY_METHOD.get("logout");
        }

        @Test
        @DisplayName("Given a logout request, when logout is called, then cookie is cleared and success message is returned")
        public void test_logout_should_clear_cookie_and_return_success_message() throws Exception {
            // WHEN & THEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Logged out successfully !"))
                    .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.SET_COOKIE))
                    .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.SET_COOKIE,
                            org.hamcrest.Matchers.containsString(SecurityConstants.AUTH_TOKEN_COOKIE_NAME)))
                    .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.SET_COOKIE,
                            org.hamcrest.Matchers.containsString("Max-Age=0")))
                    .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.SET_COOKIE,
                            org.hamcrest.Matchers.containsString("HttpOnly")))
                    .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.SET_COOKIE,
                            org.hamcrest.Matchers.containsString("Secure"))) // Si cookieSecure=true
                    .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.SET_COOKIE,
                            org.hamcrest.Matchers.containsString("Path=/")))
                    .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.SET_COOKIE,
                            org.hamcrest.Matchers.containsString("SameSite=None")));
        }
    }
}
