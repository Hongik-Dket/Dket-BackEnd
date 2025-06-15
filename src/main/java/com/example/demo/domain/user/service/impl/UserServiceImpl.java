package com.example.demo.domain.user.service.impl;

import com.example.demo.domain.metadata.dto.PhotoCardDTO;
import com.example.demo.domain.metadata.entity.PhotoCard;
import com.example.demo.domain.ticket.converter.TicketConverter;
import com.example.demo.domain.ticket.dto.TicketDTO;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.user.dto.response.WalletDTO;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
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

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletService walletService;
    private final TicketConverter ticketConverter;
    private final PinataService pinataService;

    @Override
    public User getCurrentUser() {
        // ToDO: 로그인 구현
        return userRepository.findById(1L)
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));
    }

    @Override
    public User loginWithWallet(String walletAddress) {
        if (walletAddress == null || walletAddress.isBlank()) {
            throw new CustomException(ErrorStatus.INVALID_INPUT);
        }
        if (!walletAddress.matches("^0x[a-fA-F0-9]{40}$")) {
            throw new CustomException(ErrorStatus.INVALID_WALLET_ADDRESS);
        }
        return userRepository.findByWalletAddress(walletAddress)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setWalletAddress(walletAddress);
                    return userRepository.save(newUser);
                });
    }

    @Override
    public WalletDTO getWalletInfo() {
        User user = getCurrentUser();
        String address = user.getWalletAddress();

        return WalletDTO.builder()
                .walletAddress(address)
                .balance(walletService.getEthBalance(address))
                .build();
    }

    @Override
    public List<TicketDTO> getMyTickets() {
        User user = getCurrentUser();
        List<Ticket> ticketList = getSortedMyTickets(user);

        List<TicketDTO> result = new ArrayList<>();
        for (Ticket ticket : ticketList)
            result.add(ticketConverter.toTicketDTO(ticket));

        return result;
    }

    @Override
    public List<PhotoCardDTO> getMyPhotoCards() {
        User user = getCurrentUser();
        List<Ticket> ticketList = getSortedMyTickets(user);

        List<PhotoCardDTO> result = new ArrayList<>();
        for (Ticket ticket : ticketList) {
            PhotoCard photoCard = ticket.getMetadata().getPhotoCard();
            String ipfsUrl = pinataService.cidToHttp(photoCard.getCid());

            result.add(toPhotoCardDTO(photoCard, ipfsUrl));
        }
        return result;
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
}
