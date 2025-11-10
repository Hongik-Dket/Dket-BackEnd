package com.example.demo.global.zkp.service;

import com.example.demo.global.zkp.dto.MerklePath;

import java.util.List;

public interface MerkleService {

    byte[] buildRoot(int depth, List<byte[]> leaves);

    MerklePath proofForIndex(int depth, List<byte[]> leaves, int index);

}
