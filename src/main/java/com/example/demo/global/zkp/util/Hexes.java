package com.example.demo.global.zkp.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public final class Hexes {
    private Hexes() {}

    public static String to0xHex(byte[] b) {
        if (b == null) return null;
        StringBuilder sb = new StringBuilder(2 + b.length * 2);
        sb.append("0x");

        for (byte x : b) sb.append(String.format("%02x", x));

        return sb.toString();
    }

    public static byte[] hexToBytes(String s) {
        String h = s.startsWith("0x") || s.startsWith("0X") ? s.substring(2) : s;
        if (h.isEmpty()) return new byte[0];

        if (h.length() % 2 != 0) h = "0" + h;

        int n = h.length() / 2;
        byte[] out = new byte[n];
        for (int i = 0; i < n; i++) {
            out[i] = (byte) Integer.parseInt(h.substring(2*i, 2*i+2), 16);
        }

        return out;
    }

    public static byte[] intToBe32(int v) {
        return bigIntToBe32(BigInteger.valueOf(v));
    }

    public static byte[] bigIntToBe32(BigInteger v) {
        byte[] raw = v.toByteArray();
        if (raw.length == 32) return raw;

        byte[] out = new byte[32];
        if (raw.length > 32) {
            System.arraycopy(raw, raw.length - 32, out, 0, 32);
        } else {
            System.arraycopy(raw, 0, out, 32 - raw.length, raw.length);
        }

        return out;
    }

    public static byte[] directBe32(byte[] raw) {
        if (raw.length == 32) return raw;

        byte[] out = new byte[32];
        if (raw.length > 32) {
            System.arraycopy(raw, raw.length - 32, out, 0, 32);
        } else {
            System.arraycopy(raw, 0, out, 32 - raw.length, raw.length);
        }
        return out;
    }

    public static List<byte[]> toBytesList(List<String> hexStrings) {
        List<byte[]> result = new ArrayList<>();
        for (String hex : hexStrings) {
            result.add(hexToBytes(hex));
        }
        return result;
    }
}
