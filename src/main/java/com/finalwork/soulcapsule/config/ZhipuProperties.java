package com.finalwork.soulcapsule.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "zhipu.api")
public class ZhipuProperties {

    private String key;
}
