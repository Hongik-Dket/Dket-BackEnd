package com.example.demo.global.zkp.dto;

import java.util.List;

public record WinPath(
        String winnersRoot,
        long sessionId,
        String paymentNullifier,
        List<String> pathElements,
        List<Integer> pathIndices
) {}
