package com.archiservice.user.component;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@Data
@ConfigurationProperties(prefix = "tag")
public class TagMappingComponent {
    private Set<String> mainTags;
    private Map<String, String> subToMainMapping;
}
