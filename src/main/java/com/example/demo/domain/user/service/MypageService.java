package com.example.demo.domain.user.service;

import com.example.demo.domain.metadata.dto.PhotoCardDTO;
import com.example.demo.domain.ticket.dto.TicketDTO;
import com.example.demo.domain.user.dto.response.WalletDTO;

import java.util.List;

public interface MypageService {
    WalletDTO getWalletInfo();

    List<TicketDTO> getMyTickets();

    List<PhotoCardDTO> getMyPhotoCards();

}
