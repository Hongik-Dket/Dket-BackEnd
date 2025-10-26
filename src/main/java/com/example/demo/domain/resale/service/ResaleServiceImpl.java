package com.example.demo.domain.resale.service;

import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.resale.repository.ResaleRepository;
import com.example.demo.domain.resale.dto.request.ResaleListingDTO;
import com.example.demo.domain.resale.entity.Resale;
import com.example.demo.domain.resale.enums.ResaleStatus;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.base.Constants;
import com.example.demo.global.infra.blockchain.service.DketResaleService;
import com.example.demo.global.infra.blockchain.service.ExchangeService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.EnumSet;

import static com.example.demo.domain.resale.converter.ResaleConverter.toResale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResaleServiceImpl implements ResaleService {

    private final ResaleRepository resaleRepository;
    private final UserService userService;
    private final TicketRepository ticketRepository;
    private final ExchangeService exchangeService;
    private final DketResaleService dketResaleService;

    @Override
    @Transactional
    public void createResale(Long ticketId, ResaleListingDTO request) {
        User user = userService.getCurrentUser();

        Ticket ticket;
        try {
            ticket = ticketRepository.findByIdForUpdate(ticketId)
                    .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));
        } catch (PessimisticLockException | LockTimeoutException e) {
            throw new CustomException(ErrorStatus.RESALE_CONFLICT);
        }

        int priceKrw = request.getPrice();
        validateResaleListing(user.getId(), ticket, priceKrw);

        BigInteger priceWei = exchangeService.convertKrwToWei(BigDecimal.valueOf(priceKrw));

        Resale resale = toResale(ticket, user, priceKrw, priceWei);
        resaleRepository.save(resale);
    }

    @Override
    @Transactional
    public void listResale(String ownerWalletAddress, BigInteger tokenId) {
        String owner = ownerWalletAddress.toLowerCase();

        Resale resale = resaleRepository
                .findBySellerWalletAddressAndTicketTokenIdAndResaleStatusIn(
                        owner, tokenId, EnumSet.of(ResaleStatus.AVAILABLE, ResaleStatus.RESERVED))
                .orElseThrow(() -> new CustomException(ErrorStatus.RESALE_NOT_FOUND));

        if (resale.getTxHash() == null) {
            resale.setTxHash(dketResaleService.listResaleOnChain(resale));
        }
    }

    private void validateResaleListing(Long sellerId, Ticket ticket, int priceKrw) {
        if (priceKrw <= 0) {
            throw new CustomException(ErrorStatus.RESALE_INVALID_PRICE);
        }

        if (ticket.getUser() == null || !ticket.getUser().getId().equals(sellerId)) {
            throw new CustomException(ErrorStatus.TICKET_INVALID_USER);
        }

        Session session = ticket.getSession();

        if (!session.getConcert().getIsResaleAllowed()) {
            throw new CustomException(ErrorStatus.RESALE_NOT_ALLOWED);
        }

        if (resaleRepository.existsByTicketIdAndResaleStatusIn(ticket.getId(), EnumSet.of(ResaleStatus.AVAILABLE, ResaleStatus.RESERVED))) {
            throw new CustomException(ErrorStatus.RESALE_ALREADY_LISTED);
        }

        LocalDateTime startAt = session.getDate().atTime(session.getConcert().getStartTime());

        if (ticket.getEnteredAt() == null && startAt.isAfter(LocalDateTime.now())) {
            BigDecimal price = BigDecimal.valueOf(priceKrw);
            BigDecimal basePrice = BigDecimal.valueOf(session.getConcert().getPriceKrw());

            if (price.compareTo(basePrice.multiply(Constants.RESALE_PRICE_LIMIT_RATE)) > 0) {
                throw new CustomException(ErrorStatus.RESALE_PRICE_LIMIT_EXCEEDED);
            }
        }
    }
}
