package com.example.demo.global.zkp.service;

public interface NullifierService {

    // paymentNullifier = Poseidon(IC, sessionId, TAG_PAY)
    byte[] paymentNullifier(byte[] ic32, long sessionId);

    // sessionNullifier = Poseidon(IC, sessionId, TAG_ENTER)
    byte[] sessionNullifier(byte[] ic32, long sessionId);

    // leaf = Poseidon(IC, sessionId)
    byte[] leaf(byte[] ic32, long sessionId);

}
