package com.example.demo.global.infra.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.quartz.JobKey;

import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingResponseDTO {
    private Set<JobKey> JobKeys;
}
