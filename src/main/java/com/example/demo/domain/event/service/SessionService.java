package com.example.demo.domain.event.service;

import java.util.List;

public interface SessionService {

    void saveWinners(Long sessionId, List<String> winners);

}
