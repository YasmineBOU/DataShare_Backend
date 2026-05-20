package com.openclassrooms.datashare.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UserTest")
@DisplayName("Tests for User entity")
public class UserTest {

    private static final String EMAIL = "john.doe@example.com";

    @Nested
    @Tag("userDetailsContract")
    @DisplayName("Tests for Spring Security UserDetails contract")
    class UserDetailsContractTests {

        @Test
        @DisplayName("Given a user, when UserDetails methods are called, then contract values are consistent.")
        void test_userDetailsContract_returnsExpectedValues() {
            // GIVEN
            User user = new User();
            user.setEmail(EMAIL);

            // WHEN && THEN
            assertEquals(EMAIL, user.getUsername());
            assertTrue(user.getAuthorities().isEmpty());
        }
    }

}
