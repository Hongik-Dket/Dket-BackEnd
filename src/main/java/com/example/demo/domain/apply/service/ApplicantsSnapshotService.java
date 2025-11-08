package com.example.demo.domain.apply.service;

import com.example.demo.domain.apply.entity.ApplicantsSnapshot;
import com.example.demo.domain.concert.entity.Session;

public interface ApplicantsSnapshotService {

    ApplicantsSnapshot createSnapshot(Session session);

}
