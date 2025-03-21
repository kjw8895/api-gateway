package com.api.gateway.api.master.service;

import io.jsonwebtoken.Claims;

public interface JwtService {
    boolean verify(String token);
    Claims parse(String token);
}
