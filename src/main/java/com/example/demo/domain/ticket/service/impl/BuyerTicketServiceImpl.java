package com.example.demo.domain.ticket.service.impl;

import com.example.demo.domain.resale.enums.ResaleStatus;
import com.example.demo.domain.resale.repository.ResaleRepository;
import com.example.demo.domain.ticket.dto.TicketDetailDTO;
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

    private void validateTicket(Ticket ticket, String ownerWalletAddress) {
        if (!ticket.getUser().getWalletAddress().equals(ownerWalletAddress)) {
            throw new CustomException(ErrorStatus.TICKET_INVALID);
        }
    }

    private String getNftUrl(Ticket ticket) {
        return Constants.ETHERSCAN_NFT_BASE_URL + contractAddress + "/%s".formatted(ticket.getTokenId());
    }

}
