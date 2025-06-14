package com.example.demo.domain.event.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgeLimit {
    ALL(0),        // 전체 관람가
    AGE_12(12),     // 12세 관람가
    AGE_15(15),     // 15세 관람가
    AGE_18(18);     // 18세 관람가

    private final int minimumAge;
}
