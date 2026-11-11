package com.sahab.demo.modules.content.repository;

import com.sahab.demo.modules.content.entity.NewsRead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsReadRepository extends JpaRepository<NewsRead, Long> {

    boolean existsByUser_IdAndNews_Id(Long userId, Long newsId);

    Optional<NewsRead> findByUser_IdAndNews_Id(Long userId, Long newsId);
}