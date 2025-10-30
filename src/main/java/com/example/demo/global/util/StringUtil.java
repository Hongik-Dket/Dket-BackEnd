package com.example.demo.global.util;

import java.text.Normalizer;

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

}
