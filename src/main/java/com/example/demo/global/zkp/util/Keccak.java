package com.example.demo.global.zkp.util;

import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import org.web3j.crypto.Hash;

import java.util.List;

public final class Keccak {
    private Keccak() {}

    public static byte[] keccak256(byte[] in) {
        return Hash.sha3(in);
    }

    public static String keccakListHash(List<String> leafHexList) {
        if (leafHexList == null || leafHexList.isEmpty()) {
            throw new CustomException(ErrorStatus.KECCAK_WRONG_PARAMETER);
        }

        org.bouncycastle.jcajce.provider.digest.Keccak.Digest256 d =
                new org.bouncycastle.jcajce.provider.digest.Keccak.Digest256();

        for (String hex : leafHexList) {
            byte[] b = Hexes.hexToBytes(hex);

            if (b.length != 32) {
                throw new CustomException(ErrorStatus.KECCAK_WRONG_PARAMETER);
            }

            d.update(b, 0, 32);
        }

        return Hexes.to0xHex(d.digest());
    }
}