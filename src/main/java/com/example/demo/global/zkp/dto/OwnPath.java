package com.example.demo.global.zkp.dto;

import java.util.List;

public record OwnPath (
        String ownersRoot,
        long sessionId,
        String sessionNullifier,
        List<String> pathElements,
        List<Integer> pathIndices
) {}
