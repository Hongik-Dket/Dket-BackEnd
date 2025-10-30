package com.example.demo.domain.user.converter;

import com.example.demo.domain.user.entity.PassportInfo;
import com.example.demo.domain.user.entity.User;
import com.example.demo.global.security.dto.request.PassportSignupDTO;

public class UserConverter {

    public static PassportInfo toPassportInfo(User user, PassportSignupDTO passportJoinDTO) {
        return PassportInfo.builder()
                .user(user)
                .passportNumber(passportJoinDTO.getPassportNumber())
                .gender(passportJoinDTO.getGender())
                .firstName(passportJoinDTO.getFirstName())
                .lastName(passportJoinDTO.getLastName())
                .nationality(passportJoinDTO.getNationality())
                .passportExpiryDate(passportJoinDTO.getPassportExpiry())
                .build();
    }
}
