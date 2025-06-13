package com.example.demo.domain.metadata.service;

import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.event.repository.SessionRepository;
import com.example.demo.domain.metadata.entity.Metadata;
import com.example.demo.domain.metadata.entity.PhotoCard;
import com.example.demo.domain.metadata.repository.MetadataRepository;
import com.example.demo.domain.metadata.repository.PhotoCardRepository;
import com.example.demo.global.infra.ipfs.PinataService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetadataService {

    private final SessionRepository sessionRepository;
    private final PhotoCardRepository photoCardRepository;
    private final MetadataRepository metadataRepository;
    private final PinataService pinataService;
    private final MetadataCommandService metadataCommandService;

    @Transactional
    public void createMetadata(BigInteger sessionId, BigInteger randomWord) {
        Session session = sessionRepository.findById(Long.valueOf(String.valueOf(sessionId)))
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        int capacity = session.getEvent().getCapacity();
        List<Long> photoCardIds = session.getEvent().getPhotoCards()
                .stream().map(PhotoCard::getId).collect(Collectors.toList());

        List<Integer> seatList = generateShuffledSeats(capacity, randomWord);

        List<Long> photoCardIndicesList = generateRandomPhotoCardIds(capacity, photoCardIds, randomWord);
        Map<Long, PhotoCard> photoCardMap = photoCardRepository.findAllByIdIn(photoCardIndicesList).stream()
                .collect(Collectors.toMap(PhotoCard::getId, Function.identity()));

        List<Metadata> metadataList = new ArrayList<>();

        for (int i = 0; i < capacity; i++) {
            String seatCode = convertSeatCode(seatList.get(i));

            String ticketNumber = "N" + String.format("%04d", session.getEvent().getId())
                    + String.format("%05d", sessionId) + seatCode;

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

        for (Metadata metadata : metadataList)
            uploadMetadata(metadata.getId());

    }

    private void uploadMetadata(Long metadataId) {
        Metadata metadata = metadataRepository.findById(metadataId)
                .orElseThrow(() -> new CustomException(ErrorStatus.METADATA_NOT_FOUND));

        String json = convertToJson(metadata);

        try {
            InputStream jsonStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

            pinataService.uploadJsonFile(jsonStream, "Ticket_metadata_" + metadataId + ".json")
                    .thenAccept(result -> {
                        metadataCommandService.setMetadataCid(metadata, result);
                    });
        } catch (Exception e) {
            System.out.println(e.getMessage());
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

    private String convertToJson(Metadata metadata) {
        return """
            {
              "name": "%s",
              "description": "%s",
              "image": "ipfs://%s",
              "attributes": [
                {
                  "trait_type": "Event",
                  "value": "%s"
                },
                {
                  "trait_type": "Date",
                  "value": "%s"
                },
                {
                  "trait_type": "Ticket Number",
                  "value": "%s"
                },
                {
                  "trait_type": "Seat",
                  "value": "%s"
                }
              ]
            }
            """.formatted(
                "Dket NFT Ticket",
                "This NFT represents a ticket for the %s".formatted(metadata.getSession().getEvent().getTitle()),
                metadata.getPhotoCard().getCid(), // 포토카드 CID
                metadata.getSession().getEvent().getTitle(), // 공연 정보
                metadata.getSession().getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),    // 날짜 정보
                metadata.getTicketNumber(),     // 티켓 번호
                metadata.getSeatCode()          // 좌석 정보
        );
    }

}
