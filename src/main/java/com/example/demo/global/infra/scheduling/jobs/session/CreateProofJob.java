package com.example.demo.global.infra.scheduling.jobs.session;

import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.proof.service.ProofService;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateProofJob implements Job {

    private final SessionRepository sessionRepository;
    private final TicketRepository ticketRepository;
    private final ProofService proofService;

    @Override
    public void execute(JobExecutionContext context) {
        Long sessionId = context.getJobDetail().getJobDataMap().getLong("sessionId");

        log.info("closePaymentJob: session [{}]", sessionId);

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        List<Ticket> ticketList = ticketRepository.findBySessionIdAndUserNotNull(session.getId());

        for (Ticket ticket : ticketList) {
            proofService.issueOwnProof(ticket.getId());
        }

    }

}
