package com.api.gateway.common.utils;

import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.Set;

public class RequestPathUtils {

    /**
     * Gateway에서 Rewrite 되기 전의 원본 요청 경로를 가져옵니다.
     * 존재하지 않으면 현재 요청 경로를 fallback으로 사용합니다.
     */
    public static String getOriginalRequestPath(ServerWebExchange exchange) {
        Set<URI> originalUris = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR);

        if (originalUris != null && !originalUris.isEmpty()) {
            return originalUris.iterator().next().getPath();
        }

        return exchange.getRequest().getURI().getPath();
    }
}
