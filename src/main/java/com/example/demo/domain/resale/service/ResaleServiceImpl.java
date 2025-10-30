package com.example.demo.domain.resale.service;

import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.resale.dto.response.ResaleAuthDTO;
import com.example.demo.domain.resale.dto.response.ResaleCardDTO;
import com.example.demo.domain.resale.dto.response.ResaleDetailDTO;
import com.example.demo.domain.resale.dto.response.ResaleInfoDTO;
import com.example.demo.domain.resale.repository.ResaleRepository;
import com.example.demo.domain.resale.dto.request.ResaleListingDTO;
import com.example.demo.domain.resale.entity.Resale;
import com.example.demo.domain.resale.enums.ResaleStatus;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.base.Constants;
import com.example.demo.global.infra.blockchain.ResaleSigner;
import com.example.demo.global.infra.blockchain.service.DketResaleService;
import com.example.demo.global.infra.blockchain.service.ExchangeService;
import com.example.demo.global.infra.ipfs.PinataService;
import com.example.demo.global.infra.scheduling.SchedulingService;
import com.example.demo.global.infra.scheduling.jobs.resale.CancelListingJob;
import com.example.demo.global.infra.scheduling.jobs.resale.CancelReservationJob;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static com.example.demo.domain.resale.converter.ResaleConverter.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResaleServiceImpl implements ResaleService {

    private final ResaleRepository resaleRepository;
    private final UserService userService;
    private final TicketRepository ticketRepository;
    private final ExchangeService exchangeService;
    private final DketResaleService dketResaleService;
    private final SessionRepository sessionRepository;
    private final PinataService pinataService;
    private final SchedulingService schedulingService;
    private final ResaleSigner resaleSigner;

    @Override
    @Transactional
    public ResaleInfoDTO createResale(Long ticketId, ResaleListingDTO request) {
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

        schedulingService.scheduleResaleJob(resale, CancelListingJob.class);

        return toResaleInfoDTO(resale);
    }

    @Override
    @Transactional
    public void listResale(String ownerWalletAddress, BigInteger tokenId) {
        String owner = ownerWalletAddress.toLowerCase();

        Resale resale = resaleRepository
                .findBySellerWalletAddressAndTicketTokenIdAndResaleStatusIn(
                        owner, tokenId, EnumSet.of(ResaleStatus.LISTING))
                .orElseThrow(() -> new CustomException(ErrorStatus.RESALE_NOT_FOUND));

        if (resale.getTxHash() == null) {
            resale.setTxHash(dketResaleService.listResaleOnChain(resale));
        }
    }

    @Override
    @Transactional
    public void completeResaleListing(Long resaleId) {
        Resale resale;

        try {
            resale = resaleRepository.findByIdForUpdate(resaleId)
                    .orElseThrow(() -> new CustomException(ErrorStatus.RESALE_NOT_FOUND));
        } catch (PessimisticLockException | LockTimeoutException e) {
            throw new CustomException(ErrorStatus.RESALE_CONFLICT);
        }

        resale.completeListing();

        String jobName = "CancelListingJob_" + resaleId;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                schedulingService.cancelJob(jobName);
            }
        });
    }

    @Override
    public List<ResaleCardDTO> getSessionResales(Long sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new CustomException(ErrorStatus.SESSION_NOT_FOUND);
        }

        List<Resale> resaleList = resaleRepository.findSessionResalesSorted(sessionId);

        return resaleList.stream()
                .map(resale -> toResaleCardDTO(
                        resale,
                        pinataService.cidToHttp(resale.getTicket().getMetadata().getPhotoCard().getCid())
                ))
                .toList();
    }

    @Override
    @Transactional
    public ResaleDetailDTO reserveResale(Long resaleId) {
        User user = userService.getCurrentUser();
        Resale resale;

        try {
            resale = resaleRepository.findByIdForUpdate(resaleId)
                    .orElseThrow(() -> new CustomException(ErrorStatus.RESALE_NOT_FOUND));
        } catch (PessimisticLockException | LockTimeoutException e) {
            throw new CustomException(ErrorStatus.RESALE_CONFLICT);
        }

        validateResaleReservation(resale, user);

        resale.setReservation(user);
        schedulingService.scheduleResaleJob(resale, CancelReservationJob.class);

        String photoCardUrl = pinataService.cidToHttp(resale.getTicket().getMetadata().getPhotoCard().getCid());

        return toResaleDetailDTO(resale, photoCardUrl);
    }

    @Override
    @Transactional
    public void cancelResaleReservation(Long resaleId) {
        User user = userService.getCurrentUser();
        Resale resale;

        try {
            resale = resaleRepository.findByIdForUpdate(resaleId)
                    .orElseThrow(() -> new CustomException(ErrorStatus.RESALE_NOT_FOUND));
        } catch (PessimisticLockException | LockTimeoutException e) {
            throw new CustomException(ErrorStatus.RESALE_CONFLICT);
        }

        if (!validateResaleReservationCancellation(resale, user)) {
            return;
        }
        resale.cancelReservation();

        String jobName = "CancelReservationJob_" + resaleId;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                schedulingService.cancelJob(jobName);
            }
        });
    }

    @Override
    @Transactional
    public ResaleAuthDTO authorizeResalePurchase(Long resaleId) {
        User user = userService.getCurrentUser();
        Resale resale;

        try {
            resale = resaleRepository.findByIdForUpdate(resaleId)
                    .orElseThrow(() -> new CustomException(ErrorStatus.RESALE_NOT_FOUND));
        } catch (PessimisticLockException | LockTimeoutException e) {
            throw new CustomException(ErrorStatus.RESALE_CONFLICT);
        }

        validateResalePurchase(resale, user);

        resale.prepare();

        String jobName = "CancelReservationJob_" + resaleId;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                schedulingService.cancelJob(jobName);
                schedulingService.scheduleResaleJob(resale, CancelReservationJob.class);
            }
        });

        ZoneId zone = ZoneId.of("Asia/Seoul");
        long expireAt = resale.getReservationExpiresAt()
                .atZone(zone)
                .toEpochSecond();

        String signature = resaleSigner.signPermitPurchase(
                user.getWalletAddress(),
                resale.getId(),
                resale.getTicket().getTokenId(),
                resale.getPriceWei(),
                BigInteger.valueOf(expireAt)
        );

        return ResaleAuthDTO.builder()
                .tokenId(resale.getTicket().getTokenId())
                .expireAt(BigInteger.valueOf(expireAt))
                .signature(signature)
                .build();

    }

    @Override
    @Transactional
    public void completeResalePurchase(Long resaleId) {
        Resale resale = resaleRepository.findById(resaleId)
                .orElseThrow(() -> new CustomException(ErrorStatus.RESALE_NOT_FOUND));

        resale.sell();
        resale.getTicket().resellTo(resale.getBuyer());
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

        if (resaleRepository.existsByTicketIdAndResaleStatusIn(ticket.getId(), EnumSet.of(ResaleStatus.LISTING, ResaleStatus.AVAILABLE, ResaleStatus.RESERVED))) {
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

    private void validateResaleReservation(Resale resale, User user) {
        if (!resale.getResaleStatus().equals(ResaleStatus.AVAILABLE) || resale.getReservedBy() != null) {
            throw new CustomException(ErrorStatus.RESALE_ALREADY_RESERVED);
        }

        if ((resale.getSeller().equals(user))
        || (resale.getSession().getConcert().getOrganizer().equals(user))
        || (ticketRepository.existsByUserIdAndSessionId(user.getId(), resale.getSession().getId()))) {
            throw new CustomException(ErrorStatus.TICKET_INVALID_BUYER);
        }
    }

    private boolean validateResaleReservationCancellation(Resale resale, User user) {
        if ((resale.getResaleStatus() != ResaleStatus.RESERVED)
        || (resale.getReservationExpiresAt() != null &&
                resale.getReservationExpiresAt().isBefore(LocalDateTime.now()))) {
            return false;
        }

        if (!resale.getReservedBy().getId().equals(user.getId())) {
            throw new CustomException(ErrorStatus.RESALE_RESERVATION_FORBIDDEN);
        }

        return true;
    }

    private void validateResalePurchase(Resale resale, User user) {
        if ((resale.getResaleStatus() != ResaleStatus.RESERVED)
                || ((resale.getReservationExpiresAt() != null)
                        && (resale.getReservationExpiresAt().isBefore(LocalDateTime.now())))
                || (!resale.getReservedBy().getId().equals(user.getId()))
        ) {
            throw new CustomException(ErrorStatus.RESALE_NOT_RESERVED_USER);
        }
    }
}
