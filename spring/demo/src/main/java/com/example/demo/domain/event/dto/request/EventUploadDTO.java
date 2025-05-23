package com.example.demo.domain.event.dto.request;

import com.example.demo.domain.event.enums.AgeLimit;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventUploadDTO {

    private String title;
    private AgeLimit ageLimit;
    private String location;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int priceKrw;
    private int capacity;
    private LocalDateTime applyStart;
    private LocalDateTime applyEnd;

}
