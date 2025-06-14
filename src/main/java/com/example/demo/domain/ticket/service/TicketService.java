package com.example.demo.domain.ticket.service;

import com.example.demo.domain.metadata.entity.Metadata;
import com.example.demo.domain.metadata.repository.MetadataRepository;
import com.example.demo.domain.ticket.entity.Ticket;
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
}
