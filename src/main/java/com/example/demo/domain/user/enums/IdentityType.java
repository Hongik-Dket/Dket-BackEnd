package com.example.demo.domain.user.enums;

public enum IdentityType {
    PASS(1),       // PASS 인증
    PASSPORT(2);    // 여권

    public final int code;
    IdentityType(int code) { this.code = code; }
}
