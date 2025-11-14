package com.example.demo.global.zkp.signature.repository;

import com.example.demo.global.zkp.signature.entity.Challenge;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChallengeRepository extends CrudRepository<Challenge, Long> {

    Optional<Challenge> findById(String id);

}
