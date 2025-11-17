package com.example.demo.domain.ticket.service.impl;

import com.example.demo.domain.proof.entity.OwnProof;
import com.example.demo.domain.proof.repository.OwnProofRepository;
import com.example.demo.domain.resale.enums.ResaleStatus;
import com.example.demo.domain.resale.repository.ResaleRepository;
import com.example.demo.domain.ticket.dto.request.ProofRequestDTO;
import com.example.demo.domain.ticket.dto.response.IdentityTypeDTO;
import com.example.demo.domain.ticket.dto.response.TicketResponseDTO;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.ticket.service.OrganizerTicketService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.infra.blockchain.service.DketNFTService;
import com.example.demo.global.infra.blockchain.service.DketNFTViewService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.demo.global.util.Hexes.directBe32;
import static com.example.demo.global.util.Hexes.hexToBytes;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizerTicketServiceImpl implements OrganizerTicketService {

    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final DketNFTViewService dketNFTViewService;
    private final ResaleRepository resaleRepository;
    private final DketNFTService dketNFTService;
    private final OwnProofRepository ownProofRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void enterTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));

        User user = userService.getCurrentUser();
        validateOrganizer(ticket, user);
        validateTicket(ticket);

        if (!ownProofRepository.existsByTicketId(ticketId)) {
            if (ticket.getEnteredAt() != null) {
                throw new CustomException(ErrorStatus.TICKET_ALREADY_ENTERED);
            }

            if (dketNFTViewService.isEntered(ticket.getTokenId())) {
                throw new CustomException(ErrorStatus.TICKET_ALREADY_ENTERED);
            }
        }

        ticket.enter();
        log.info("UPDATE   ticket [{}] entered", ticket.getId());
    }

    @Override
    public TicketResponseDTO validateTicketWithoutProof(String ticketNumber) {
        if (ticketNumber == null || ticketNumber.isEmpty()) {
            throw new CustomException(ErrorStatus.COMMON_WRONG_PARAMETER);
        }

        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));

        User user = userService.getCurrentUser();

        if (ticket.getEnteredAt() != null) {
            throw new CustomException(ErrorStatus.TICKET_ALREADY_ENTERED);
        }

        validateOrganizer(ticket, user);
        validateTicket(ticket);

        if (dketNFTViewService.isEntered(ticket.getTokenId())) {
            throw new CustomException(ErrorStatus.TICKET_ALREADY_ENTERED);
        }

        return TicketResponseDTO.builder()
                .ticketId(ticket.getId())
                .build();
    }

    @Override
    @Transactional
    public IdentityTypeDTO verifyOwnProofAndEnter(ProofRequestDTO request) {
        OwnProof ownProof = ownProofRepository.findById(request.getProofId())
                .orElseThrow(() -> new CustomException(ErrorStatus.PROOF_NOT_FOUND));

        Ticket ticket;
        try {
            ticket = ticketRepository.findByIdForUpdate(ownProof.getTicketId())
                    .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));
        } catch (CustomException e) {
            log.error(e.getMessage(), e);
            throw new CustomException(ErrorStatus.TICKET_CONFLICT);
        }

        List<BigInteger> proof;
        try {
            List<String> rawHex = objectMapper.readValue(ownProof.getProofJson(), new TypeReference<List<String>>() {});
            proof =  rawHex.stream()
                    .map(h -> new BigInteger(1, hexToBytes(h)))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomException(ErrorStatus.JSON_CONVERT_FAILED);
        }

        byte[] nullifier = directBe32(hexToBytes(ownProof.getNullifier()));

        dketNFTService.enterTicketOnChain(ticket, proof, nullifier);
        ticket.enter();
        log.info("UPDATE   ticket [{}] entered", ticket.getId());

        return IdentityTypeDTO.builder()
                .identityType(ticket.getUser().getIdentityType())
                .ticketId(ticket.getId())
                .build();
    }

    private void validateOrganizer(Ticket ticket, User user) {
        if (!(ticket.getSession().getConcert().getOrganizer().getId().equals(user.getId()))) {
            throw new CustomException(ErrorStatus.TICKET_INVALID_USER);
        }
    }

    private void validateTicket(Ticket ticket) {
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
    }

}
