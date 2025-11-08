package com.example.demo.global.event;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadyToMint {
    private Long sessionId;
}
