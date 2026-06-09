package com.openclassrooms.datashare.configuration.logging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Configuration class for enabling and customizing request logging in the
 * application.
 * This class defines a {@link CommonsRequestLoggingFilter} bean to log incoming
 * HTTP requests,
 * including query strings, payloads, and other details.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Enabling detailed logging of HTTP requests.</li>
 * <li>Configuring the logging filter to include query strings, payloads, and
 * headers.</li>
 * <li>Setting a maximum payload length to avoid logging excessively large
 * request bodies.</li>
 * </ul>
 *
 * @see CommonsRequestLoggingFilter
 */
@Configuration
public class RequestLoggingFilterConfig {

    /**
     * Creates and configures a {@link CommonsRequestLoggingFilter} bean to log HTTP
     * requests.
     *
     * <p>
     * The filter is configured to:
     * <ul>
     * <li>Include the query string in the logs.</li>
     * <li>Include the request payload (body) in the logs.</li>
     * <li>Set a maximum payload length of 10,000 characters.</li>
     * <li>Exclude headers from the logs.</li>
     * <li>Prefix the log message with "REQUEST DATA: " for clarity.</li>
     * </ul>
     *
     * @return A configured {@link CommonsRequestLoggingFilter} instance.
     */
    @Bean
    public CommonsRequestLoggingFilter commonsRequestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        return filter;
    }
}
