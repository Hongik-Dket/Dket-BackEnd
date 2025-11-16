package com.example.demo.domain.ticket.service.impl;

import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.metadata.entity.Metadata;
import com.example.demo.domain.metadata.repository.MetadataRepository;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.ticket.service.TicketService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
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
public class TicketServiceImpl implements TicketService {

    private final MetadataRepository metadataRepository;
    private final SessionRepository sessionRepository;
    private final ApplyRepository applyRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

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

        Session session = metadataList.get(0).getSession();
        session.setIsMinted();

        if (session.isDrawn()) {
            session.setIsBuyable(true);
        }
    }

    @Override
    @Transactional
    public void completeTicket(String address, Long sessionId, BigInteger tokenId) {
        User user = userRepository.findByWalletAddress(address)
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        Ticket ticket = ticketRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));

        ticket.paidBy(user);

        applyRepository.findBySessionIdAndUserId(sessionId, user.getId())
                .ifPresent(apply -> apply.setApplyStatus(ApplyStatus.PAID));

        int paidCount = ticketRepository.countBySessionIdAndPaidAtIsNotNull(sessionId);
        if (paidCount == session.getConcert().getCapacity()) {
            session.setIsBuyable(false);
        }
    }

}
