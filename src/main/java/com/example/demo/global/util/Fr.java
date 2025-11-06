package com.example.demo.global.util;

import java.math.BigInteger;

public final class Fr {

    private static final BigInteger R = new BigInteger(
            "21888242871839275222246405745257275088548364400416034343698204186575808495617"
    );

    private Fr() {}

    public static BigInteger toField(byte[] bytes) {
        return new BigInteger(1, bytes).mod(R);
    }
}