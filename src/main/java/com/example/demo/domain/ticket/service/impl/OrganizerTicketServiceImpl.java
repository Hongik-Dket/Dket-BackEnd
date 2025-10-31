package com.example.demo.domain.ticket.service.impl;

import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.ticket.service.OrganizerTicketService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizerTicketServiceImpl implements OrganizerTicketService {

    private final TicketRepository ticketRepository;
    private final UserService userService;

    @Override
    @Transactional
    public void enterTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));

        User user = userService.getCurrentUser();

        validateOrganizer(ticket, user);

        if (ticket.getEnteredAt() != null) {
            throw new CustomException(ErrorStatus.TICKET_ALREADY_ENTERED);
        }

        ticket.enter();
    }

    private void validateOrganizer(Ticket ticket, User user) {
        if (!(ticket.getSession().getConcert().getOrganizer().getId().equals(user.getId()))) {
            throw new CustomException(ErrorStatus.TICKET_INVALID_USER);
        }
    }

}
