package com.api.gateway.api.master.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties("auth")
public class AuthProperties {
    private List<WhitelistRule> whitelist = new ArrayList<>();

    @Getter
    @Setter
    public static class WhitelistRule {
        private String path;
        private HttpMethod method;
    }
}
