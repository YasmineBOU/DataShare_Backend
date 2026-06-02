package com.openclassrooms.datashare.configuration;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    private List<String> forbiddenExtensions;

    public List<String> getForbiddenExtensions() {
        return forbiddenExtensions;
    }

    public void setForbiddenExtensions(List<String> forbiddenExtensions) {
        this.forbiddenExtensions = forbiddenExtensions;
    }

}
