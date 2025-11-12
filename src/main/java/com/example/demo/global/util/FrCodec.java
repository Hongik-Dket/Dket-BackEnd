package com.example.demo.global.util;

import java.math.BigInteger;

import static com.example.demo.global.util.Hexes.directBe32;
import static com.example.demo.global.util.Hexes.hexToBytes;

public final class FrCodec {

    private FrCodec() {}

    public static final BigInteger BN254_P = new BigInteger(
            "21888242871839275222246405745257275088548364400416034343698204186575808495617"
    );

    public static BigInteger fr(String hex0x) {
        byte[] be = hexToBytes(hex0x);

        if (be.length != 32) {
            be = directBe32(be);
        }

        return new BigInteger(1, be).mod(BN254_P);
    }

    public static String beHexToFrDec(String hex0x) {
        String h = (hex0x.startsWith("0x") || hex0x.startsWith("0X")) ? hex0x.substring(2) : hex0x;
        if (h.isEmpty()) return "0";
        BigInteger n = new BigInteger(h, 16).mod(BN254_P);
        return n.toString(10);
    }

}
