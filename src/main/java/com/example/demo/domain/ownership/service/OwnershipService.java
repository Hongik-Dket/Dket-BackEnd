package com.example.demo.domain.ownership.service;

import java.math.BigInteger;

public interface OwnershipService {

    void createOwnership(String buyer, Long sessionId, BigInteger tokenId, String txHash, Long blockNo, Integer logIdx);

}
