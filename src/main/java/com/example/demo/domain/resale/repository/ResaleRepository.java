package com.example.demo.domain.resale.repository;

import com.example.demo.domain.resale.entity.Resale;
import com.example.demo.domain.resale.enums.ResaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface ResaleRepository extends JpaRepository<Resale, Long> {

    boolean existsByTicketIdAndResaleStatusIn(Long ticketId, Collection<ResaleStatus> resaleStatus);

    Optional<Resale> findBySellerWalletAddressAndTicketTokenIdAndResaleStatusIn(
            String walletAddress,
            BigInteger tokenId,
            Collection<ResaleStatus> resaleStatuses
    );

    boolean existsByTicketIdAndSellerIdAndResaleStatusIn(
            Long ticketId,
            Long sellerId,
            Collection<ResaleStatus> statuses
    );
}
