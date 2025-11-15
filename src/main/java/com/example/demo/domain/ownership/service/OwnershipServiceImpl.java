package com.example.demo.domain.ownership.service;

import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.ownership.entity.OwnersAggregate;
import com.example.demo.domain.ownership.entity.OwnersEvent;
import com.example.demo.domain.ownership.entity.Ownership;
import com.example.demo.domain.ownership.repository.OwnersAggregateRepository;
import com.example.demo.domain.ownership.repository.OwnersEventRepository;
import com.example.demo.domain.ownership.repository.OwnershipRepository;
import com.example.demo.domain.ticket.entity.Ticket;
import com.example.demo.domain.ticket.repository.TicketRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.example.demo.global.zkp.poseidon.Poseidon;
import com.example.demo.global.zkp.poseidon.PoseidonMerkleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

import static com.example.demo.global.base.Constants.OWN_TAG;
import static com.example.demo.global.util.Hexes.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnershipServiceImpl implements OwnershipService {

    private final Poseidon poseidon;

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final TicketRepository ticketRepository;
    private final OwnersEventRepository ownersEventRepository;
    private final OwnersAggregateRepository ownersAggregateRepository;
    private final OwnershipRepository ownershipRepository;
    private final PoseidonMerkleService poseidonMerkleService;

    @Override
    @Transactional
    public void createOwnership(String buyer, Long sessionId, BigInteger tokenId,
                                String txHash, Long blockNo, Integer logIdx
    ) {
        if (ownersEventRepository.existsByTxHashAndLogIndex(txHash, logIdx)) {
            return;
        }

        User user = userRepository.findByWalletAddress(buyer)
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        Ticket ticket = ticketRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new CustomException(ErrorStatus.TICKET_NOT_FOUND));

        if (ownershipRepository.existsActiveOwnershipBySessionIdAndUserId(session.getId(), user.getId())) {
            throw new CustomException(ErrorStatus.OWN_ALREADY_OWNED);
        }

        updateOwnership(session, user, ticket, blockNo);

        OwnersEvent event = OwnersEvent.builder()
                .sessionId(session.getId())
                .blockNumber(blockNo)
                .txHash(txHash)
                .logIndex(logIdx)
                .ownerAddress(buyer)
                .tokenId(tokenId)
                .build();
        ownersEventRepository.save(event);
    }

    @Transactional
    protected void updateOwnership(Session session, User user, Ticket ticket, Long blockNo) {
        BigInteger ic = new BigInteger(1, hexToBytes(user.getIcCommitment()));
        BigInteger sid = BigInteger.valueOf(session.getId());
        BigInteger h = poseidon.hash(ic, sid);
        h = poseidon.hash(h, OWN_TAG);

        String leafHex = to0xHex(bigIntToBe32(h));

        OwnersAggregate aggregate = ownersAggregateRepository.findBySessionId(session.getId())
                .orElseGet(() -> ownersAggregateRepository.save(
                        OwnersAggregate.builder()
                                .sessionId(session.getId())
                                .build()
                ));

        Integer maxOrd = ownershipRepository.findMaxOrdIndexByAggregateId(aggregate.getId());
        int ordIndex = (maxOrd == null) ? 0 : maxOrd + 1;

        Ownership ownership = Ownership.builder()
                .ownersAggregate(aggregate)
                .leafHex(leafHex)
                .ordIndex(ordIndex)
                .ticket(ticket)
                .user(user)
                .build();

        aggregate.addItem(ownership);
        ownershipRepository.save(ownership);


        List<String> leafHexes = ownershipRepository.findOwnerLeafHexes(aggregate.getId());
        String poseidonRoot = poseidonMerkleService.rootHex(leafHexes);

        aggregate.update(leafHexes.size(), poseidonRoot, blockNo);
    }

}
