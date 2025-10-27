package com.example.demo.domain.resale.dto.response;

import com.example.demo.domain.resale.enums.ResaleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResaleCardDTO {

    private Long resaleId;
    private Long ticketId;
    private int priceKrw;
    private String seatCode;
    private ResaleStatus resaleStatus;
    private String photoCardUrl;

}
