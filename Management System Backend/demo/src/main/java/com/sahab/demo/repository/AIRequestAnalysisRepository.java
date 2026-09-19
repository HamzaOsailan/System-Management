package com.sahab.demo.repository;

import com.sahab.demo.entity.AIRequestAnalysis;
import com.sahab.demo.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AIRequestAnalysisRepository
        extends JpaRepository<AIRequestAnalysis, Long> {

    Optional<AIRequestAnalysis> findByRequest(Request request);

    void deleteByRequest(Request request);
}