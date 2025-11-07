package com.example.demo.global.zkp.service;

import com.example.demo.global.zkp.dto.WinPath;

import java.util.List;

public interface WinnersRootService {

    byte[] computeAndSetWinnersRoot(long sessionId, List<byte[]> winnerLeaves, long payDeadlineEpochMillis);

    WinPath getWinPath(long sessionId, byte[] ic32);
}
