package com.example.demo.domain.metadata.service.impl;

import com.example.demo.domain.concert.entity.Concert;
import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.metadata.dto.MetadataJson;
import com.example.demo.domain.metadata.entity.Metadata;
import com.example.demo.domain.metadata.entity.PhotoCard;
import com.example.demo.domain.metadata.repository.MetadataRepository;
import com.example.demo.domain.metadata.repository.PhotoCardRepository;
import com.example.demo.domain.metadata.service.MetadataCommandService;
import com.example.demo.domain.metadata.service.MetadataService;
import com.example.demo.global.infra.blockchain.service.DketNFTService;
import com.example.demo.global.infra.ipfs.PinataService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetadataServiceImpl implements MetadataService {

    private final SessionRepository sessionRepository;
    private final PhotoCardRepository photoCardRepository;
    private final MetadataRepository metadataRepository;
    private final PinataService pinataService;
    private final MetadataCommandService metadataCommandService;
    private final ObjectMapper objectMapper;
    private final DketNFTService dketNFTService;

    @Override
    @Transactional
    public List<Long> createMetadata(BigInteger sessionId, BigInteger randomWord) {
        Session session = sessionRepository.findById(sessionId.longValue())
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        List<Long> photoCardIds = session.getConcert().getPhotoCards()
                .stream().map(PhotoCard::getId).collect(Collectors.toList());

        int capacity = session.getConcert().getCapacity();
        List<Integer> seatList = generateShuffledSeats(capacity, randomWord);

        List<Long> photoCardIndicesList = generateRandomPhotoCardIds(capacity, photoCardIds, randomWord);
        Map<Long, PhotoCard> photoCardMap = photoCardRepository.findAllByIdIn(photoCardIndicesList).stream()
                .collect(Collectors.toMap(PhotoCard::getId, Function.identity()));

        List<Metadata> metadataList = new ArrayList<>();

        for (int i = 0; i < capacity; i++) {
            String seatCode = convertSeatCode(seatList.get(i));
            String ticketNumber = generateTicketNumber(session.getConcert(), sessionId, seatCode);

            Metadata metadata = Metadata.builder()
                    .session(session)
                    .ticketNumber(ticketNumber)
                    .photoCard(photoCardMap.get(photoCardIndicesList.get(i)))
                    .seatCode(seatCode)
                    .build();

            session.addMetadata(metadata);
            metadataList.add(metadata);
        }

        metadataRepository.saveAll(metadataList);

        List<Long> metadataIds = metadataList.stream()
                .map(Metadata::getId)
                .collect(Collectors.toList());

        return metadataIds;
    }

    @Override
    public void uploadAllMetadataAsync(List<Long> metadataIds) {
        Session session = metadataRepository.findSessionByMetadataId(metadataIds.get(0));

        List<CompletableFuture<Void>> futures = metadataIds.stream()
                .map(this::uploadMetadataAsync)
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .whenComplete((v, e) -> {
                    if (e == null) {
                        dketNFTService.mintSessionTicket(session);
                    } else {
                        log.error("세션 [{}] 메타데이터 업로드 실패", session.getId(), e);
                        throw new CustomException(ErrorStatus.IPFS_UPLOAD_FAILED);
                    }
                });
    }

    @Override
    @Async("pinataUploadExecutor")
    public CompletableFuture<Void> uploadMetadataAsync(Long metadataId) {
        try {
            Metadata metadata = metadataRepository.findById(metadataId)
                    .orElseThrow(() -> new CustomException(ErrorStatus.METADATA_NOT_FOUND));

            String json = convertToJson(metadata);
            InputStream jsonStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

            return pinataService.uploadJsonFile(jsonStream, "Ticket_metadata_" + metadataId + ".json")
                    .thenAccept(cid -> {
                        metadataCommandService.setMetadataCid(metadataId, cid);
                    });

        } catch (Exception e) {
            log.error("Metadata [{}] IPFS 업로드 중 예외 발생", metadataId, e);
            throw new CustomException(ErrorStatus.IPFS_UPLOAD_FAILED);
        }
    }

    private List<Integer> generateShuffledSeats(int seatCount, BigInteger seed) {
        List<Integer> list = new ArrayList<>(seatCount);
        for (int i = 1; i <= seatCount; i++)
            list.add(i);

        byte[] seedBytes = seed.toByteArray();
        SecureRandom secureRandom = new SecureRandom(seedBytes);

        Collections.shuffle(list, secureRandom);

        return list;
    }

    private List<Long> generateRandomPhotoCardIds(int drawCount, List<Long> photoCardIds, BigInteger seed) {
        List<Long> result = new ArrayList<>(drawCount);
        SecureRandom random = new SecureRandom(seed.toByteArray());

        int size = photoCardIds.size();
        for (int i = 0; i < drawCount; i++)
            result.add(photoCardIds.get(random.nextInt(size)));

        return result;
    }

    private String convertSeatCode(Integer seatNumber) {
        if (seatNumber == null || seatNumber < 0 || seatNumber > 999999)
            throw new CustomException(ErrorStatus.TICKET_INVALID_SEAT);

        return String.format("%06d", seatNumber);
    }

    private String generateTicketNumber(Concert concert, BigInteger sessionId, String seatCode) {
        Long concertId = concert.getId();
        String ticketNumber;

        if (concert.getIsResaleAllowed()) {
            ticketNumber = "R";
        } else {
            ticketNumber = "N";
        }

        return ticketNumber + String.format("%04d", concertId)
                + String.format("%05d", sessionId)
                + seatCode;
    }

    private String convertToJson(Metadata metadata) {
        MetadataJson metadataJson = MetadataJson.builder()
                .name(":Dket NFT Ticket")
                .description("This NFT represents a ticket for the " + metadata.getSession().getConcert().getTitle())
                .image("ipfs://" + metadata.getPhotoCard().getCid())
                .attributes(List.of(
                        new MetadataJson.Attribute("Concert", metadata.getSession().getConcert().getTitle()),
                        new MetadataJson.Attribute("Date", metadata.getSession().getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))),
                        new MetadataJson.Attribute("Ticket Number", metadata.getTicketNumber()),
                        new MetadataJson.Attribute("Seat", metadata.getSeatCode())
                ))
                .build();

        try {
            return objectMapper.writeValueAsString(metadataJson);
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorStatus.METADATA_JSON_CONVERT_FAILED);
        }
    }

}
