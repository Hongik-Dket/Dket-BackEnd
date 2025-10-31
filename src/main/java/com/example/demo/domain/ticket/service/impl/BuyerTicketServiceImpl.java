package com.example.demo.domain.ticket.service.impl;

import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.resale.enums.ResaleStatus;
import com.example.demo.domain.resale.repository.ResaleRepository;
import com.example.demo.domain.ticket.dto.request.EntryCodeDTO;
import com.example.demo.domain.ticket.dto.response.EntryProofDataDTO;
import com.example.demo.domain.ticket.dto.response.TicketDetailDTO;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.ticket.service.BuyerTicketService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.base.Constants;
import com.example.demo.global.infra.blockchain.service.DketNFTViewService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;

import static com.example.demo.domain.ticket.converter.TicketConverter.toTicketDetailDTO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BuyerTicketServiceImpl implements BuyerTicketService {

    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final DketNFTViewService dketNFTViewService;
    private final ResaleRepository resaleRepository;

    @Value("${web3.nft-contract-address}")
    private String contractAddress;

    @Override
    public TicketDetailDTO getTicketDetail(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));

        User user = userService.getCurrentUser();
        String ownerWalletAddress = dketNFTViewService.getOwnerWallet(ticket.getTokenId());

        if (ownerWalletAddress == null) {
            throw new CustomException(ErrorStatus.TICKET_NOT_FOUND);
        }

        if (!(ownerWalletAddress.equals(user.getWalletAddress()))) {
            throw new CustomException(ErrorStatus.TICKET_INVALID_USER);
        }

        validateTicket(ticket, ownerWalletAddress);

        boolean isResaleListed = resaleRepository
                .existsByTicketIdAndSellerIdAndResaleStatusIn(
                        ticketId,
                        user.getId(),
                        EnumSet.of(ResaleStatus.LISTING, ResaleStatus.AVAILABLE, ResaleStatus.RESERVED)
                );

        return toTicketDetailDTO(ticket, getNftUrl(ticket), isResaleListed);
    }

    @Override
    public EntryProofDataDTO getEntryProofData(Long ticketId, EntryCodeDTO request) {
        User user = userService.getCurrentUser();

        Ticket ticket = ticketRepository.findByIdAndUserId(ticketId, user.getId())
                .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_INVALID_USER));

        if (ticket.getEnteredAt() != null) {
            throw new CustomException(ErrorStatus.TICKET_ALREADY_ENTERED);
        }

        Session session = ticket.getSession();
        if (!session.getDate().equals(LocalDate.now())) {
            throw new CustomException(ErrorStatus.SESSION_NOT_TODAY);
        }
        if (session.getEntryCode() == null
                || !session.getEntryCode().equals(request.getEntryCode())) {
            throw new CustomException(ErrorStatus.TICKET_WRONG_ENTRY_CODE);
        }

        // Todo: proof 생성에 필요한 데이터 조회

        return EntryProofDataDTO.builder().build();

    }

    private void validateTicket(Ticket ticket, String ownerWalletAddress) {
        if (!ticket.getUser().getWalletAddress().equals(ownerWalletAddress)) {
            throw new CustomException(ErrorStatus.TICKET_INVALID);
        }
    }

    private String getNftUrl(Ticket ticket) {
        return Constants.ETHERSCAN_NFT_BASE_URL + contractAddress + "/%s".formatted(ticket.getTokenId());
    }

}
