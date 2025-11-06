package com.example.demo.global.zkp.ic;

import com.example.demo.domain.user.entity.PassportIdentity;
import com.example.demo.global.zkp.poseidon.Poseidon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

import static com.example.demo.global.zkp.ic.util.Hexes.to0xHex;

@Service
@RequiredArgsConstructor
public class IcService {
    private final Poseidon poseidon; // JNI 구현 (PoseidonNative)

    /** 16바이트 salt 생성기 */
    public static byte[] newSalt16() {
        byte[] s = new byte[16];
        new SecureRandom().nextBytes(s);
        return s;
    }

    /** PASS → IC */
//    public IcCommitment createFromPass(PassIdentity pass, byte[] salt16, String walletAddress0x) {
//        List<byte[]> inputs = IcInputBuilder.buildPassInputs(pass, salt16, walletAddress0x);
//        BigInteger h = poseidon.hash(bytes32ToBigInts(inputs));
//        return new IcCommitment(to0xHex(to32(h)), to0xHex(salt16));
//    }

    /** PASSPORT → IC */
    public IcCommitment createFromPassport(PassportIdentity passportIdentity, LocalDate birth, byte[] salt16, String walletAddress0x) {
        List<byte[]> inputs = IcInputBuilder.buildPassportInputs(passportIdentity, birth, salt16, walletAddress0x);
        BigInteger h = poseidon.hash(bytes32ToBigInts(inputs));
        return new IcCommitment(to0xHex(to32(h)), to0xHex(salt16));
    }

    private static BigInteger[] bytes32ToBigInts(List<byte[]> feInputs) {
        BigInteger[] arr = new BigInteger[feInputs.size()];
        for (int i = 0; i < feInputs.size(); i++) {
            arr[i] = new BigInteger(1, feInputs.get(i));
        }
        return arr;
    }

    private static byte[] to32(BigInteger x) {
        byte[] raw = x.toByteArray();
        byte[] out = new byte[32];
        if (raw.length > 32) {
            System.arraycopy(raw, raw.length - 32, out, 0, 32);
        } else {
            System.arraycopy(raw, 0, out, 32 - raw.length, raw.length);
        }
        return out;
    }

    // 결과 DTO
    public record IcCommitment(String icCommitment, String icUserSalt) {}
}