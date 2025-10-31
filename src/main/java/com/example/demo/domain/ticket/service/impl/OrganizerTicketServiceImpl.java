package com.example.demo.domain.ticket.service.impl;

import com.example.demo.domain.resale.enums.ResaleStatus;
import com.example.demo.domain.resale.repository.ResaleRepository;
import com.example.demo.domain.ticket.dto.response.TicketResponseDTO;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.ticket.service.OrganizerTicketService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.infra.blockchain.service.DketNFTViewService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizerTicketServiceImpl implements OrganizerTicketService {

    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final DketNFTViewService dketNFTViewService;
    private final ResaleRepository resaleRepository;

    @Override
    @Transactional
    public void enterTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));

        User user = userService.getCurrentUser();

        validateOrganizer(ticket, user);

        if (ticket.getEnteredAt() != null) {
            throw new CustomException(ErrorStatus.TICKET_ALREADY_ENTERED);
        }

        ticket.enter();
    }

    @Override
    public TicketResponseDTO validateTicketWithoutProof(String ticketNumber) {
        if (ticketNumber == null || ticketNumber.isEmpty()) {
            throw new CustomException(ErrorStatus.COMMON_WRONG_PARAMETER);
        }

        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));

        User user = userService.getCurrentUser();
        validateOrganizer(ticket, user);
        validateTicket(ticket);

        return TicketResponseDTO.builder()
                .ticketId(ticket.getId())
                .build();
    }

    private void validateOrganizer(Ticket ticket, User user) {
        if (!(ticket.getSession().getConcert().getOrganizer().getId().equals(user.getId()))) {
            throw new CustomException(ErrorStatus.TICKET_INVALID_USER);
        }
    }

    private void validateTicket(Ticket ticket) {
        if (ticket.getEnteredAt() != null) {
            throw new CustomException(ErrorStatus.TICKET_ALREADY_ENTERED);
        }

        if (!ticket.getSession().getDate().equals(LocalDate.now())) {
            throw new CustomException(ErrorStatus.SESSION_NOT_TODAY);
        }

        if (ticket.getUser() == null || ticket.getPaidAt() == null) {
            throw new CustomException(ErrorStatus.TICKET_INVALID_USER);
        }

        if (resaleRepository.existsByTicketIdAndResaleStatusIn(ticket.getId(), EnumSet.of(ResaleStatus.PENDING))) {
            throw new CustomException(ErrorStatus.TICKET_RESALE_PENDING);
        }

        String ownerWalletAddress = dketNFTViewService.getOwnerWallet(ticket.getTokenId());
        if (!ticket.getUser().getWalletAddress().equals(ownerWalletAddress)) {
            throw new CustomException(ErrorStatus.TICKET_INVALID);
        }

        if (dketNFTViewService.isEntered(ticket.getTokenId())) {
            throw new CustomException(ErrorStatus.TICKET_ALREADY_ENTERED);
        }
    }

}
