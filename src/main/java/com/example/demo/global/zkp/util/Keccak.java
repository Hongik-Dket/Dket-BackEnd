package com.example.demo.global.zkp.util;

import org.web3j.crypto.Hash;

public final class Keccak {
    private Keccak() {}

    public static byte[] keccak256(byte[] in) {
        return Hash.sha3(in);
    }
}