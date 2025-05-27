package com.example.demo.global.infra.blockchain;

import com.example.demo.domain.apply.repository.ApplyRepository;
import com.example.demo.domain.event.entity.Event;
import com.example.demo.domain.event.entity.Session;
import com.example.demo.domain.event.repository.EventRepository;
import com.example.demo.domain.event.repository.SessionRepository;
import com.example.demo.global.infra.blockchain.dto.CreateEventRequestDTO;
import com.example.demo.global.infra.blockchain.dto.CreateEventResponseDTO;
import com.example.demo.global.infra.blockchain.dto.CreateSessionRequestDTO;
import com.example.demo.global.infra.blockchain.dto.OpenPublicSaleRequestDTO;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.example.demo.global.infra.blockchain.converter.BlockchainConverter.toCreateEventRequestDTO;

@Service
@RequiredArgsConstructor
public class BlockchainService {

    private final WebClient webClient;
    private final PlatformTransactionManager transactionManager;
    private final EventRepository eventRepository;
    private final SessionRepository sessionRepository;
    private final ApplyRepository applyRepository;

    public void createEventOnChain(Event event, List<String> photoCardURIS) {

        CreateEventRequestDTO requestDTO = toCreateEventRequestDTO(event, photoCardURIS);

        webClient.post()
                .uri("/api/organizer/event")
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(CreateEventResponseDTO.class)
                .onErrorMap(e -> new CustomException(ErrorStatus.BLOCKCHAIN_REQUEST_FAILED))
                .subscribe(response -> {
                    TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
                    txTemplate.execute(status -> {
                        event.setOnChainInfo(response.getResult().getTxHash(), response.getResult().getPriceWei());
                        eventRepository.save(event);

                        return null;
                    });
                });
    }

    public void createSessionOnChain(Event event) {

        List<Session> sessionList = sessionRepository.findByEventId(event.getId());
        CreateSessionRequestDTO requestDTO = null;

        for (Session session : sessionList) {
            List<String> applications = applyRepository.findWalletAddressesBySessionId(session.getId());

            requestDTO = CreateSessionRequestDTO.builder()
                    .eventId(event.getId())
                    .sessionId(session.getId())
                    .applications(applications)
                    .build();

            webClient.post()
                    .uri("/api/organizer/session")
                    .bodyValue(requestDTO)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            Mono.error(new CustomException(ErrorStatus.BLOCKCHAIN_REQUEST_FAILED))
                    )
                    .bodyToMono(Void.class)
                    .subscribe();
        }
    }

    public void openPublicSaleOnChain(Event event) {
        OpenPublicSaleRequestDTO requestDTO = OpenPublicSaleRequestDTO.builder()
                .eventId(event.getId())
                .build();

        webClient.post()
                .uri("/api/organizer/open-public")
                .bodyValue(requestDTO)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new CustomException(ErrorStatus.BLOCKCHAIN_REQUEST_FAILED))
                )
                .bodyToMono(Void.class)
                .subscribe();
    }
}