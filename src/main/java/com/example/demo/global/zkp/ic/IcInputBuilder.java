package com.example.demo.global.zkp.ic;

import com.example.demo.domain.user.entity.PassportIdentity;
import com.example.demo.domain.user.enums.IdentityType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.example.demo.global.base.Constants.DOMAIN_TAG_HASH;
import static com.example.demo.global.zkp.ic.IcEncoding.*;
import static com.example.demo.global.zkp.ic.TextNormalizer.*;
import static com.example.demo.global.zkp.ic.TextNormalizer.toYyyyMmDdDigits;

public final class IcInputBuilder {
    private IcInputBuilder() {}

    /** 공통 헤더: DOMAIN_TAG, ID_TYPE, SALT16, WALLET_ADDRESS */
    private static void addCommonHeader(List<byte[]> out, IdentityType idType, byte[] salt16, String walletAddress0x) {
        out.add(DOMAIN_TAG_HASH);               // 1) DOMAIN_TAG (keccak32)
        out.add(frOfIdType(idType.code));       // 2) ID_TYPE (1=PASS, 2=PASSPORT)
        out.add(frOfSalt16(salt16));            // 3) SALT16 (상위 16B 0패딩)
        out.add(frOfEthAddress(walletAddress0x)); // 4) WALLET 20B → BE32
    }

    /** PASS용 입력들 */
//    public static List<byte[]> buildPassInputs(PassIdentity raw, byte[] salt16, String walletAddress) {
//        // 정규화
//        String addr0x = normalizeEthAddress(walletAddress);
//        String kor = normalizeKorName(raw.getNameKor());
//        String dobDigits = toYyyyMmDdDigits(raw.getBirth()); // LocalDate → "YYYYMMDD"
//
//        // CI / 전화
//        String ci = raw.getCi();
//        String phone = raw.getPhoneDigits();
//
//        if ((ci == null || ci.isBlank()) && (phone == null || phone.isBlank())) {
//            throw new IllegalArgumentException("Either CI or phoneDigits must be provided");
//        }
//
//        byte[] idField;
//        if (ci != null && !ci.isBlank()) {
//            String ciNorm = normalizeCommon(ci);           // CI는 문자열 그대로 keccak
//            idField = frOfStringKeccak(ciNorm);
//        } else {
//            String phoneDigits = normalizePhoneDigits(phone);
//            idField = frOfAsciiDirect(phoneDigits);        // 숫자만 DIRECT_BE32
//        }
//
//        List<byte[]> inputs = new ArrayList<>();
//        addCommonHeader(inputs, IdentityType.PASS, salt16, addr0x);
//        // PASS-specific
//        inputs.add(idField);                               // 5) CI(keccak) or phone(DIRECT)
//        inputs.add(frOfStringKeccak(kor));                 // 6) nameKor(keccak)
//        inputs.add(frOfAsciiDirect(dobDigits));            // 7) DOB(YYYYMMDD DIRECT)
//
//        return inputs;
//    }

    /** PASSPORT용 입력들 (성/이름 분리 포함) */
    public static List<byte[]> buildPassportInputs(
            PassportIdentity raw, LocalDate birth, byte[] salt16, String walletAddress) {
        String addr0x = normalizeEthAddress(walletAddress);
        String first = normalizeEn(raw.getFirstName());
        String last = normalizeEn(raw.getLastName());

        String passportNo = normalizePassportNo(raw.getPassportNumber());
        String nat = normalizeEn(raw.getNationality());
        String dobDigits = toYyyyMmDdDigits(birth);
        String expDigits = toYyyyMmDdDigits(raw.getPassportExpiryDate());

        List<byte[]> inputs = new ArrayList<>();
        addCommonHeader(inputs, IdentityType.PASSPORT, salt16, addr0x);

        // PASSPORT-specific (circomlibjs 호환: 문자열은 keccak)
        inputs.add(frOfStringKeccak(passportNo));          // 5) 여권번호(keccak)
        inputs.add(frOfStringKeccak(first));               // 6) 이름(keccak)
        inputs.add(frOfStringKeccak(last));                // 7) 성(keccak)
        inputs.add(frOfAsciiDirect(dobDigits));            // 8) DOB (DIRECT)
        inputs.add(frOfAsciiDirect(expDigits));            // 9) EXP (DIRECT)
        inputs.add(frOfStringKeccak(nat));                  // 10) 국적 (keccak)

        return inputs;
    }
}