package com.example.demo.domain.ticket.dto.response;

import com.example.demo.domain.user.enums.IdentityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityTypeDTO {

    private IdentityType identityType;
    private Long ticketId;

}
