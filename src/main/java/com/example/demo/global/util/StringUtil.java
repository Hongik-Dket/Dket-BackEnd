package com.example.demo.global.util;

import java.text.Normalizer;
import java.time.LocalDate;

public final class StringUtil {

    private StringUtil() {}

    public static String normalize(String input) {
        if (input == null) {
            return "";
        }

        String t = input.toLowerCase();
        t = Normalizer.normalize(t, Normalizer.Form.NFKD);
        t = t.replaceAll("\\p{M}+", ""); // 악센트 제거
        t = t.replaceAll("[\\p{Z}\\p{P}\\p{S}]+", ""); // 공백/기호 제거

        return t;
    }

    public static String canonical(String input) {
        return input == null ? "" : input.trim().toUpperCase();
    }

    public static String yyyyMMdd(LocalDate d) {
        return d == null ? "" : d.toString().replace("-", "");
    }

    public static String digits(String s) {
        return s == null ? "" : s.replaceAll("[^0-9]", "");
    }

}
