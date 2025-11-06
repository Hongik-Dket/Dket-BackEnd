package com.example.demo.global.crypto.impl;

import com.example.demo.global.crypto.Poseidon;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class PoseidonNativeIntegrationTest {

    @Test
    void jni_poseidon_should_work_and_be_deterministic() {
        // given
        Poseidon poseidon = new PoseidonNative();

        // when
        BigInteger h1 = poseidon.hash(BigInteger.ONE, BigInteger.TWO);
        BigInteger h2 = poseidon.hash(BigInteger.ONE, BigInteger.TWO);
        BigInteger h3 = poseidon.hash(BigInteger.TWO, BigInteger.ONE); // 순서 바꾸면 달라져야 함

        // then
        assertNotNull(h1);
        assertTrue(h1.signum() >= 0, "hash must be non-negative");
        assertEquals(h1, h2, "same inputs must produce the same output");
        assertNotEquals(h1, h3, "different order should produce different output");

        System.out.println("✅ JNI Poseidon(1,2) = 0x" + h1.toString(16));
    }
}