package com.example.demo.domain.user.dto.response;

import com.example.demo.domain.user.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassportInfoDTO {

    private Long userId;
    private String passportNumber;
    private Gender gender;
    private String firstName;
    private String lastName;
    private LocalDate birth;
    private String nationality;
    private LocalDate passportExpiry;

}
