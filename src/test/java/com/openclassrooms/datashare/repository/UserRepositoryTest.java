package com.openclassrooms.datashare.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.openclassrooms.datashare.entities.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:datashare_testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=USER",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
        "spring.jpa.show-sql=false"
})
@Tag("UserRepositoryTest")
@DisplayName("Tests for UserRepository")
public class UserRepositoryTest {

    private static final String EMAIL = "john.doe@example.com";

    @Autowired
    private UserRepository userRepository;

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("encoded-password");
        return user;
    }

    @Nested
    @Tag("findByEmail")
    @DisplayName("Tests for findByEmail method")
    class FindByEmailTests {

        @Test
        @DisplayName("Given an existing email, when findByEmail is called, then the matching user is returned.")
        void test_findByEmail_returnsMatchingUser() {
            // GIVEN
            User savedUser = userRepository.saveAndFlush(createUser(EMAIL));

            // WHEN
            Optional<User> foundUser = userRepository.findByEmail(EMAIL);

            // THEN
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
            assertThat(foundUser.get().getEmail()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("Given a non-existing email, when findByEmail is called, then an empty Optional is returned.")
        void test_findByEmail_returns_empty_optional() {
            // WHEN
            Optional<User> foundUser = userRepository.findByEmail("unknownUser@example.com");

            // THEN
            assertThat(foundUser).isEmpty();
        }
    }

}
