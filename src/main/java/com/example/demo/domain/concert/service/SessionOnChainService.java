package com.example.demo.domain.concert.service;

import com.example.demo.domain.concert.entity.Session;

public interface SessionOnChainService {

    void commitApplicants(Session session);

}
