package com.example.demo.domain.ticket.service;

import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.event.repository.SessionRepository;
import com.example.demo.domain.metadata.entity.Metadata;
import com.example.demo.domain.metadata.repository.MetadataRepository;
import com.example.demo.domain.ticket.converter.TicketConverter;
import com.example.demo.domain.ticket.dto.PriceWeiDTO;
import com.example.demo.domain.ticket.dto.TicketDTO;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.infra.image.QrCodeGenerator;
import com.example.demo.global.infra.image.S3UploadService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final MetadataRepository metadataRepository;
    private final UserService userService;
    private final SessionRepository sessionRepository;
    private final ApplyRepository applyRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final QrCodeGenerator qrCodeGenerator;
    private final S3UploadService s3UploadService;

    private final TicketConverter ticketConverter;

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
    }

    @Transactional
    public PriceWeiDTO getPriceWei(Long sessionId) {
        User user = userService.getCurrentUser();
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        validateBuyer(session, user);

        return PriceWeiDTO.builder()
                .priceWei(session.getEvent().getPriceWei())
                .sessionId(sessionId)
                .build();
    }

    @Transactional
    public void completeTicket(String address, Long sessionId, Long tokenId) {
        User user = userRepository.findByWalletAddress(address)
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        Ticket ticket = ticketRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));

        ticket.paidBy(user);

        Apply apply = applyRepository.findBySessionAndUser(session, user)
                .orElseThrow(() -> new CustomException(ErrorStatus.APPLY_NOT_FOUND));

        apply.setApplyStatus(ApplyStatus.PAID);

        String qrCodeUrl = s3UploadService.saveFile(qrCodeGenerator.generateQrCodeFile(ticket.getId()));
        ticket.setQrCode(qrCodeUrl);
    }

    public TicketDTO getTicketById(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));

        User user = userService.getCurrentUser();

        if (!(ticket.getUser().equals(user)) && !(ticket.getSession().getEvent().getOrganizer().equals(user)))
            throw new CustomException(ErrorStatus.TICKEt_INVALID_USER);

        return ticketConverter.toTicketDTO(ticket);
    }

    public TicketDTO getTicketByNumber(String ticketNumber) {
        Metadata metadata = metadataRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new CustomException(ErrorStatus.METADATA_NOT_FOUND));

        Ticket ticket = ticketRepository.findByMetadata(metadata)
                .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));

        User user = userService.getCurrentUser();

        if (!(ticket.getSession().getEvent().getOrganizer().equals(user)))
            throw new CustomException(ErrorStatus.TICKEt_INVALID_USER);

        return ticketConverter.toTicketDTO(ticket);
    }

    private void validateBuyer(Session session, User user) {
        Event event = session.getEvent();

        if (!user.isEligibleFor(event.getAgeLimit()))
            throw new CustomException(ErrorStatus.TICKET_INVALID_BUYER);

        if (!session.isBuyableNow())
            throw new CustomException(ErrorStatus.SESSION_CANNOT_BUY);

        switch (event.getEventStatus()) {
            case APPLY_CLOSED:
                Apply apply = applyRepository.findBySessionAndUser(session, user)
                        .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_INVALID_BUYER));

                if (apply.getApplyStatus() != ApplyStatus.SELECTED)
                    throw new CustomException(ErrorStatus.TICKET_INVALID_BUYER);

                break;

            case IN_PROGRESS:
                break;

            default:
                throw new CustomException(ErrorStatus.SESSION_CANNOT_BUY);
        }
    }

}
