package com.example.demo.global.util;

import java.math.BigInteger;

public final class FrCodec {

    private FrCodec() {}

    public static final BigInteger P = new BigInteger(
            "21888242871839275222246405745257275088548364400416034343698204186575808495617");

    public static String beHexToFrDec(String hex0x) {
        String h = (hex0x.startsWith("0x") || hex0x.startsWith("0X")) ? hex0x.substring(2) : hex0x;
        if (h.isEmpty()) return "0";
        BigInteger n = new BigInteger(h, 16).mod(P);
        return n.toString(10);
    }

    public static String longToFrDec(long v) {
        return BigInteger.valueOf(v).mod(P).toString(10);
    }

}
