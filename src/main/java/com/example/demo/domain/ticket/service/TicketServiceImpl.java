package com.example.demo.domain.ticket.service;

import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.metadata.entity.Metadata;
import com.example.demo.domain.metadata.repository.MetadataRepository;
import com.example.demo.domain.resale.enums.ResaleStatus;
import com.example.demo.domain.resale.repository.ResaleRepository;
import com.example.demo.domain.ticket.dto.TicketDetailDTO;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.base.Constants;
import com.example.demo.global.infra.blockchain.service.DketNFTViewService;
import com.example.demo.global.infra.image.QrCodeGenerator;
import com.example.demo.global.infra.image.S3UploadService;
import com.example.demo.global.infra.ipfs.PinataService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.demo.domain.ticket.converter.TicketConverter.toTicketDetailDTO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {

    private final MetadataRepository metadataRepository;
    private final UserService userService;
    private final SessionRepository sessionRepository;
    private final ApplyRepository applyRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final QrCodeGenerator qrCodeGenerator;
    private final S3UploadService s3UploadService;
    private final DketNFTViewService dketNFTViewService;
    private final ResaleRepository resaleRepository;
    private final PinataService pinataService;

    @Value("${web3.nft-contract-address}")
    private String contractAddress;

    @Override
    @Transactional
    public void batchRegisterTicket(List<BigInteger> tokenIdList, List<String> cidList) {
        List<Metadata> metadataList = metadataRepository.findAllByCidIn(cidList);
        Map<String, Metadata> cidToMetadata = metadataList.stream()
                .collect(Collectors.toMap(Metadata::getCid, Function.identity()));

        for (int i = 0; i < tokenIdList.size(); i++) {
            BigInteger tokenId = tokenIdList.get(i);
            String cid = cidList.get(i);

            Metadata metadata = cidToMetadata.get(cid);
            if (metadata == null)
                throw new CustomException(ErrorStatus.METADATA_NOT_FOUND);

            Ticket ticket = Ticket.builder()
                    .metadata(metadata)
                    .session(metadata.getSession())
                    .tokenId(tokenId)
                    .build();

            metadata.getSession().addTicket(ticket);
        }

        metadataList.get(0).getSession().setIsBuyable(true);
    }

    @Override
    @Transactional
    public void completeTicket(String address, Long sessionId, Long tokenId) {
        User user = userRepository.findByWalletAddress(address)
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        Ticket ticket = ticketRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));

        ticket.paidBy(user);

        applyRepository.findBySessionIdAndUserId(sessionId, user.getId())
                .ifPresent(apply -> apply.setApplyStatus(ApplyStatus.PAID));

        String qrCodeUrl = s3UploadService.saveFile(qrCodeGenerator.generateQrCodeFile(ticket.getId()));
        ticket.setQrCode(qrCodeUrl);

        int paidCount = ticketRepository.countBySessionIdAndPaidAtIsNotNull(sessionId);
        if (paidCount == session.getConcert().getCapacity()) {
            session.setIsBuyable(false);
        }
    }

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

        String photoCardUrl = pinataService.cidToHttp(ticket.getMetadata().getPhotoCard().getCid());

        return toTicketDetailDTO(ticket, getNftUrl(ticket), isResaleListed, photoCardUrl);
    }

    @Override
    @Transactional
    public void enterTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));

        User user = userService.getCurrentUser();

        validateOrganizer(ticket, user);

        if (
            !(ticket.getSession().getDate().equals(LocalDate.now()) &&
                    ticket.getSession().getConcert().getStartTime().isAfter(LocalTime.now()))
                    || ticket.getEnteredAt() != null
        ) {
            throw new CustomException(ErrorStatus.TICKET_INVALID);
        }

        ticket.enter();
    }

    @Override
    public String getNftUrl(Ticket ticket) {
        return Constants.ETHERSCAN_NFT_BASE_URL + contractAddress + "/%s".formatted(ticket.getTokenId());
    }

    private void validateOrganizer(Ticket ticket, User user) {
        if (!(ticket.getSession().getConcert().getOrganizer().getId().equals(user.getId()))) {
            throw new CustomException(ErrorStatus.TICKET_INVALID_USER);
        }
    }

    private void validateTicket(Ticket ticket, String ownerWalletAddress) {
        if (!ticket.getUser().getWalletAddress().equals(ownerWalletAddress)) {
            throw new CustomException(ErrorStatus.TICKET_INVALID);
        }
    }
}
