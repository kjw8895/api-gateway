package com.api.gateway.api.master.filter;

import com.api.gateway.api.master.config.property.AuthProperties;
import com.api.gateway.api.master.service.JwtService;
import com.api.gateway.common.constant.HttpHeaderConstant;
import com.api.gateway.common.utils.RequestPathUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@EnableConfigurationProperties({AuthProperties.class})
@RequiredArgsConstructor
public class AuthFilter implements GlobalFilter, Ordered {
    private final JwtService jwtService;
    private final AuthProperties authProperties;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        PathPattern pattern = new PathPatternParser().parse("/public/**");
        boolean allowedDomainPattern = pattern.matches(PathContainer.parsePath(path));

        String originalPath = RequestPathUtils.getOriginalRequestPath(exchange);
        HttpMethod method = exchange.getRequest().getMethod();
        boolean allowedOriginPattern = authProperties.getWhitelist().stream()
                .anyMatch(rule -> rule.getMethod().equals(method) && new AntPathMatcher().match(rule.getPath(), originalPath));

        if (allowedDomainPattern || allowedOriginPattern) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();

        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return onError(exchange, "no authorization header", HttpStatus.UNAUTHORIZED);
        }

        String authorizationHeader = Objects.requireNonNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0);
        String token = authorizationHeader.replace("Bearer", "");

        if (!jwtService.verify(token)) {
            return onError(exchange, "JWT Token is not valid", HttpStatus.UNAUTHORIZED);
        }

        Claims claims = jwtService.parse(token);
        Integer id = claims.get("id", Integer.class);
        String email = claims.get("email", String.class);
        String status = claims.get("status", String.class);
        List<String> roles = claims.get("roles", List.class);

        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header(HttpHeaderConstant.X_USER_ID, String.valueOf(id))
                .header(HttpHeaderConstant.X_USER_EMAIL, email)
                .header(HttpHeaderConstant.X_USER_STATUS, status)
                .header(HttpHeaderConstant.X_USER_ROLES, String.join(",", roles))
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        return chain.filter(mutatedExchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        log.error(err);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE; // 필터 순서 (높을수록 먼저 실행됨)
    }
}
