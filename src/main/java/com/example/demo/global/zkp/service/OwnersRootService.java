package com.example.demo.global.zkp.service;

import com.example.demo.global.zkp.dto.OwnPath;

import java.util.Set;

public interface OwnersRootService {

    byte[] recomputeAndSetOwnersRoot(long sessionId, Set<byte[]> ownerLeaves);

    OwnPath getOwnPath(long sessionId, byte[] ic32);

}
