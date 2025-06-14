package com.example.demo.domain.ticket.service;

import com.example.demo.domain.apply.entity.Apply;
import com.example.demo.domain.apply.enums.ApplyStatus;
import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.event.enums.EventStatus;
import com.example.demo.domain.event.repository.SessionRepository;
import com.example.demo.domain.metadata.entity.Metadata;
import com.example.demo.domain.metadata.repository.MetadataRepository;
import com.example.demo.domain.ticket.dto.ApprovalDTO;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
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
    public ApprovalDTO approveTicket(Long sessionId) {
        User user = userService.getCurrentUser();
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        validateBuyer(session, user);

        // Todo: DketNFT service



    }

    private void validateBuyer(Session session, User user) {
        Event event = session.getEvent();

        switch (event.getEventStatus()) {
            case APPLY_CLOSED:
                Apply apply = applyRepository.findBySessionAndUser(session, user)
                        .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_INVALID_BUYER));

                if (apply.getApplyStatus() != ApplyStatus.SELECTED)
                    throw new CustomException(ErrorStatus.TICKET_INVALID_BUYER);

                break;

            case IN_PROGRESS:
                if (LocalDate.now().isAfter(session.getDate()))
                    throw new CustomException(ErrorStatus.SESSION_CANNOT_BUY);

                if (LocalDate.now().equals(session.getDate())
                        && !LocalTime.now().isBefore(event.getStartTime().minusHours(2)))
                    throw new CustomException(ErrorStatus.SESSION_CANNOT_BUY);

                break;

            case APPLY_NOT_OPENED:
            case APPLY_OPEN:
            case ENDED:
                throw new CustomException(ErrorStatus.SESSION_CANNOT_BUY);
        }



    }
}
