package com.example.demo.domain.resale;

import com.example.demo.domain.resale.entity.Resale;
import com.example.demo.domain.resale.enums.ResaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ResaleRepository extends JpaRepository<Resale, Long> {

    boolean existsByTicketIdAndResaleStatusIn(Long ticketId, Collection<ResaleStatus> resaleStatus);
}
