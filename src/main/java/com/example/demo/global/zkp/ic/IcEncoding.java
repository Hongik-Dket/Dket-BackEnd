package com.example.demo.global.zkp.ic;

import java.nio.charset.StandardCharsets;

import static com.example.demo.global.util.Hexes.*;
import static com.example.demo.global.util.Keccak.keccak256;

public final class IcEncoding {
    private IcEncoding() {}

    // 문자열 → KECCAK32_BE
    public static byte[] frOfStringKeccak(String normalized) {
        return keccak256(normalized.getBytes(StandardCharsets.UTF_8)); // already 32B
    }

    // ASCII → DIRECT_BE32
    public static byte[] frOfAsciiDirect(String ascii) {
        return directBe32(ascii.getBytes(StandardCharsets.US_ASCII));
    }

    // 정수/코드 → DIRECT_BE32
    public static byte[] frOfIdType(int code) {
        return intToBe32(code);
    }

    // 고정 16바이트 salt → DIRECT_BE32 (상위 16B 0)
    public static byte[] frOfSalt16(byte[] salt16) {
        if (salt16 == null || salt16.length != 16) {
            throw new IllegalArgumentException("salt16 must be 16 bytes");
        }
        byte[] out = new byte[32];
        System.arraycopy(salt16, 0, out, 16, 16);
        return out;
    }

    // 0x + 40 hex → 20바이트 → DIRECT_BE32
    public static byte[] frOfEthAddress(String addr0x) {
        byte[] raw20 = hexToBytes(addr0x); // 0x + 40hex → 20 bytes
        if (raw20.length != 20) throw new IllegalArgumentException("eth address must be 20 bytes");
        return directBe32(raw20);
    }
}