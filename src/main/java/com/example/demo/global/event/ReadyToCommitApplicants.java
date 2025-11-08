package com.example.demo.global.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadyToCommitApplicants {

    private Long sessionId;
    private Long applicantsSnapshotId;

}
