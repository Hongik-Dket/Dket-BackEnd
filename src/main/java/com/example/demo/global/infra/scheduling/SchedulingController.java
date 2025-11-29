package com.example.demo.global.infra.scheduling;

import com.example.demo.global.infra.scheduling.dto.SchedulingResponseDTO;
import com.example.demo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.demo.global.response.status.SuccessStatus._OK;

// 배포 서버 관리용
@RestController
@RequestMapping("/api/scheduling")
@RequiredArgsConstructor
public class SchedulingController {

    private final SchedulingService schedulerService;

    @Operation(summary = "스케줄링 조회")
    @GetMapping("")
    public ApiResponse<SchedulingResponseDTO> getJobKeys() {
        return ApiResponse.onSuccess(_OK, schedulerService.getJobKeys());
    }

    @Operation(summary = "전체 스케줄링")
    @PostMapping("")
    public ApiResponse<?> scheduleAll() {
        schedulerService.scheduleAll();

        return ApiResponse.onSuccess(_OK, null);
    }
}
