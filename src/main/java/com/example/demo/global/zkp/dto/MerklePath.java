package com.example.demo.global.zkp.dto;

import java.util.List;

public record MerklePath (
        List<byte[]> pathElements,
        List<Integer> pathIndices
) {}
