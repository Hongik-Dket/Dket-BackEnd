package com.example.demo.domain.apply.service;

import com.example.demo.domain.apply.entity.ApplicantsSnapshot;
import com.example.demo.domain.concert.entity.Session;

import java.util.List;

public interface ApplicantsSnapshotService {

    ApplicantsSnapshot createSnapshot(Session session);

    List<byte[]> buildLeavesForDraw(Session session);

}
