package com.api.gateway.api.master.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("jwt")
public class JwtProperties {
    private String secret;
    private Long accessExpirationTime;
    private Long refreshExpirationTime;
}
