package com.example.demo.domain.metadata.service;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MetadataService {
    List<Long> createMetadata(BigInteger sessionId, BigInteger randomWord);

    void uploadAllMetadataAsync(List<Long> metadataIds);

    CompletableFuture<Void> uploadMetadataAsync(Long metadataId);
}
