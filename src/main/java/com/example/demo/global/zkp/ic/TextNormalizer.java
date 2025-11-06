package com.example.demo.global.zkp.ic;

import java.text.Normalizer;

public final class TextNormalizer {
    private TextNormalizer() {}

    private static String nfkc(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFKC);
    }

    // 공통: trim → 내부 공백 1칸 → NFKC
    public static String normalizeCommon(String s) {
        if (s == null) return null;
        String t = s.trim().replaceAll("\\s+", " ");
        return nfkc(t);
    }

    // 한글 이름: 공통만 (대소문자 변환 없음)
    public static String normalizeKorName(String nameKor) {
        String s = normalizeCommon(nameKor);
        if (s == null || s.isEmpty()) throw new IllegalArgumentException("Empty kor name");
        return s;
    }

    // 영문: 공통 + 대문자 + 필터(A-Z 공백만)
    public static String normalizeEn(String en) {
        String s = normalizeCommon(en);
        if (s == null) throw new IllegalArgumentException("null english");
        s = s.toUpperCase().replaceAll("[^A-Z ]", "");
        s = s.trim().replaceAll("\\s+", " ");
        if (s.isEmpty()) throw new IllegalArgumentException("Empty english");
        return s;
    }

    // 여권번호: 공통 + 대문자 + A-Z0-9만
    public static String normalizePassportNo(String no) {
        String s = normalizeCommon(no);
        if (s == null) throw new IllegalArgumentException("null passport no");
        s = s.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (s.isEmpty()) throw new IllegalArgumentException("Empty passport no");
        return s;
    }

    // 전화번호(E.164 입력 가정) → '+' 제거, 숫자만
    public static String normalizePhoneDigits(String e164) {
        if (e164 == null) throw new IllegalArgumentException("null phone");
        String s = e164.trim().replace("+", "").replaceAll("[^0-9]", "");
        if (s.isEmpty()) throw new IllegalArgumentException("Empty phone digits");
        return s;
    }

    // 날짜 → YYYYMMDD (숫자만 8자리)
    public static String toYyyyMmDdDigits(java.time.LocalDate d) {
        if (d == null) throw new IllegalArgumentException("null date");
        return String.format("%04d%02d%02d", d.getYear(), d.getMonthValue(), d.getDayOfMonth());
    }

    // 지갑주소(0x.. 20바이트) → 소문자/길이 체크만 (EIP-55 미필요)
    public static String normalizeEthAddress(String addr) {
        if (addr == null) throw new IllegalArgumentException("null eth address");
        String s = normalizeCommon(addr).toLowerCase();
        if (s.startsWith("0x")) s = s.substring(2);
        if (!s.matches("[0-9a-f]{40}")) throw new IllegalArgumentException("Invalid eth address");
        return "0x" + s;
    }
}
