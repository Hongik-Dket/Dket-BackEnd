package com.example.demo.domain.lottery.service;

import com.example.demo.domain.lottery.entity.ApplicantsSnapshot;
import com.example.demo.domain.concert.entity.Session;

import java.util.List;

public interface ApplicantsSnapshotService {

    ApplicantsSnapshot createSnapshot(Session session);

    List<byte[]> buildLeavesForDraw(Session session);

}
