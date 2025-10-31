package com.example.demo.domain.user.service.impl;

import com.example.demo.domain.metadata.dto.PhotoCardDTO;
import com.example.demo.domain.ticket.dto.TicketDTO;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.user.dto.response.WalletDTO;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.MypageService;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.infra.blockchain.service.WalletService;
import com.example.demo.global.infra.ipfs.PinataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.demo.domain.metadata.converter.PhotoCardConverter.toPhotoCardDTO;
import static com.example.demo.domain.ticket.converter.TicketConverter.toTicketDTO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageServiceImpl implements MypageService {

    private final UserService userService;
    private final WalletService walletService;
    private final PinataService pinataService;
    private final TicketRepository ticketRepository;

    @Override
    public WalletDTO getWalletInfo() {
        User user = userService.getCurrentUser();
        String address = user.getWalletAddress();

        return WalletDTO.builder()
                .walletAddress(address)
                .balance(walletService.getEthBalance(address))
                .build();
    }

    @Override
    public List<TicketDTO> getMyTickets() {
        User user = userService.getCurrentUser();

        return ticketRepository.findAllSortedByDateAndFutureFirst(user, LocalDate.now())
                .stream()
                .map(ticket -> toTicketDTO(ticket))
                .collect(Collectors.toList());
    }

    @Override
    public List<PhotoCardDTO> getMyPhotoCards() {
        User user = userService.getCurrentUser();
        List<Ticket> ticketList = ticketRepository.findAllSortedByDateAndFutureFirstWithPhotoCard(user, LocalDate.now());

        List<PhotoCardDTO> result = new ArrayList<>();
        for (Ticket ticket : ticketList) {
            result.add(toPhotoCardDTO(ticket));
        }
        return result;
    }
}
