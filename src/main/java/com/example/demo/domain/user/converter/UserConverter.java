package com.example.demo.domain.user.converter;

import com.example.demo.domain.user.dto.response.PassportInfoDTO;
import com.example.demo.domain.user.entity.PassportIdentity;
import com.example.demo.domain.user.entity.User;
import com.example.demo.global.security.dto.request.PassportSignupDTO;

public class UserConverter {

    public static PassportIdentity toPassportInfo(User user, PassportSignupDTO passportJoinDTO) {
        return PassportIdentity.builder()
                .user(user)
                .passportNumber(passportJoinDTO.getPassportNumber())
                .gender(passportJoinDTO.getGender())
                .firstName(passportJoinDTO.getFirstName())
                .lastName(passportJoinDTO.getLastName())
                .nationality(passportJoinDTO.getNationality())
                .passportExpiryDate(passportJoinDTO.getPassportExpiry())
                .build();
    }

    public static PassportInfoDTO toPassportInfoDTO(PassportIdentity passportIdentity) {
        return PassportInfoDTO.builder()
                .userId(passportIdentity.getUser().getId())
                .passportNumber(passportIdentity.getPassportNumber())
                .gender(passportIdentity.getGender())
                .firstName(passportIdentity.getFirstName())
                .lastName(passportIdentity.getLastName())
                .birth(passportIdentity.getUser().getBirth())
                .nationality(passportIdentity.getNationality())
                .passportExpiry(passportIdentity.getPassportExpiryDate())
                .build();
    }
}
