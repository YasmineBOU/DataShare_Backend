package com.openclassrooms.datashare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main class for the DataShare application.
 * This class serves as the entry point for the Spring Boot application and
 * configures:
 * <ul>
 * <li>Component scanning (via {@link SpringBootApplication}).</li>
 * <li>Scheduling support (via {@link EnableScheduling}).</li>
 * </ul>
 *
 * <p>
 * The application manages file sharing functionalities, including:
 * <ul>
 * <li>User registration and authentication.</li>
 * <li>File upload, download, and deletion.</li>
 * <li>Scheduled cleanup of expired files.</li>
 * </ul>
 *
 * @see SpringBootApplication
 * @see EnableScheduling
 */
@SpringBootApplication
@EnableScheduling
public class DataShareApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataShareApplication.class, args);
    }

}
