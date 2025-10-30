package com.example.demo.global.security.dto.request;

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
public class PassportSignupDTO {

    private String passportNumber;
    private Gender gender;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String nationality;
    private LocalDate passportExpiry;

}
