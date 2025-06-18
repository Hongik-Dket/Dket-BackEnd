package com.example.demo.global.infra.blockchain.service;

import java.math.BigInteger;

public interface DketNFTViewService {

    String getOwnerWallet(BigInteger tokenId);

    String getTokenUri(BigInteger tokenId);
}
