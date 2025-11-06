package com.example.demo.global.crypto.impl;

import com.example.demo.global.crypto.Poseidon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PoseidonNative implements Poseidon {

    static {
        loadNative();
    }

    private static void loadNative() {
        String base = "poseidon";
        String mapped = System.mapLibraryName(base);

        try {
            try (var in = PoseidonNative.class.getResourceAsStream("/" + mapped)) {
                if (in != null) {
                    java.nio.file.Path tmp = java.nio.file.Files.createTempFile(base + "-", mapped);
                    java.nio.file.Files.copy(in, tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    tmp.toFile().deleteOnExit();
                    System.load(tmp.toString());
                    return;
                }
            }

            java.nio.file.Path p = java.nio.file.Paths.get(
                    System.getProperty("user.dir"),
                    "native","poseidon","target","release", mapped
            );
            if (java.nio.file.Files.exists(p)) {
                System.load(p.toAbsolutePath().toString());
                return;
            }

            throw new UnsatisfiedLinkError("Native library not found: " + mapped);
        } catch (Throwable e) {
            throw new UnsatisfiedLinkError("Failed to load native lib: " + e.getMessage());
        }
    }

    private static native byte[] poseidonHash(byte[] concatenated32Be, int elemCount);

    private static final BigInteger BN254_P = new BigInteger(
            "21888242871839275222246405745257275088548364400416034343698204186575808495617"
    );

    private static byte[] to32be(BigInteger x) {
        if (x.signum() < 0) throw new IllegalArgumentException("Poseidon input must be non-negative");

        x = x.mod(BN254_P);
        byte[] raw = x.toByteArray();

        int start = (raw.length > 32 && raw[0] == 0) ? 1 : 0;
        int len   = raw.length - start;
        if (len > 32) {
            start = raw.length - 32;
            len   = 32;
        }

        byte[] out = new byte[32];
        System.arraycopy(raw, start, out, 32 - len, len);
        return out;
    }

    @Override
    public BigInteger hash(BigInteger... inputs) {
        if (inputs == null || inputs.length == 0) {
            inputs = new BigInteger[]{ BigInteger.ZERO };
        }

        byte[] concat = new byte[32 * inputs.length];

        for (int i = 0; i < inputs.length; i++) {
            byte[] be = to32be(inputs[i]);
            System.arraycopy(be, 0, concat, i * 32, 32);
        }

        byte[] out = poseidonHash(concat, inputs.length);

        return new BigInteger(1, out);
    }
}
