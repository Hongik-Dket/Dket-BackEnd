package com.example.demo.domain.user.service.impl;

import com.example.demo.domain.metadata.dto.PhotoCardDTO;
import com.example.demo.domain.metadata.dto.PhotoCardDetailDTO;
import com.example.demo.domain.ticket.dto.TicketDTO;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.ticket.service.TicketService;
import com.example.demo.domain.user.dto.response.WalletDTO;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.MypageService;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.infra.blockchain.service.WalletService;
import com.example.demo.global.infra.ipfs.PinataService;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.demo.domain.metadata.converter.PhotoCardConverter.toPhotoCardDTO;
import static com.example.demo.domain.metadata.converter.PhotoCardConverter.toPhotoCardDetailDTO;
import static com.example.demo.domain.ticket.converter.TicketConverter.toTicketDTO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageServiceImpl implements MypageService {

    private final UserService userService;
    private final WalletService walletService;
    private final PinataService pinataService;
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;

    @Override
    public WalletDTO getWalletInfo() {
        User user = userService.getCurrentUser();
        String address = user.getWalletAddress();

        return WalletDTO.builder()
                .walletAddress(address)
                .balance(walletService.getEthBalance(address))
                .build();
    }

    @Override
    public List<TicketDTO> getMyTickets() {
        User user = userService.getCurrentUser();
        List<Ticket> ticketList = getSortedMyTickets(user);

        List<TicketDTO> result = new ArrayList<>();
        for (Ticket ticket : ticketList)
            result.add(toTicketDTO(ticket));

        return result;
    }

    @Override
    public List<PhotoCardDTO> getMyPhotoCards() {
        User user = userService.getCurrentUser();
        List<Ticket> ticketList = getSortedMyTickets(user);

        List<PhotoCardDTO> result = new ArrayList<>();
        for (Ticket ticket : ticketList) {
            String ipfsUrl = pinataService.cidToHttp(ticket.getMetadata().getPhotoCard().getCid());

            result.add(toPhotoCardDTO(ticket, ipfsUrl));
        }
        return result;
    }

    @Override
    public PhotoCardDetailDTO getMyPhotoCardDetail(Long ticketId) {
        User user = userService.getCurrentUser();

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));

        validateUser(user, ticket);

        String ipfsUrl = pinataService.cidToHttp(ticket.getMetadata().getPhotoCard().getCid());
        String nftUrl = ticketService.getNftUrl(ticket);

        return toPhotoCardDetailDTO(ticket, ipfsUrl, nftUrl);
    }

    private List<Ticket> getSortedMyTickets(User user) {
        LocalDate now = LocalDate.now();

        Comparator<Ticket> bySessionDate = Comparator.comparing(ticket -> ticket.getSession().getDate());

        List<Ticket> futureTickets = user.getTickets().stream()
                .filter(ticket -> !ticket.getSession().getDate().isBefore(now))
                .sorted(bySessionDate)
                .collect(Collectors.toList());

        List<Ticket> pastTickets = user.getTickets().stream()
                .filter(ticket -> ticket.getSession().getDate().isBefore(now))
                .sorted(bySessionDate.reversed())
                .collect(Collectors.toList());

        List<Ticket> ticketList = new ArrayList<>();
        ticketList.addAll(futureTickets);
        ticketList.addAll(pastTickets);

        return ticketList;
    }

    private void validateUser(User user, Ticket ticket) {
        if (!ticket.getUser().equals(user))
            throw new CustomException(ErrorStatus.TICKET_INVALID_USER);
    }
}
