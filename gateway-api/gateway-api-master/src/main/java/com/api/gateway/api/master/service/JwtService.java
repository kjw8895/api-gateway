package com.api.gateway.api.master.service;

public interface JwtService {
    boolean verify(String token);
}
