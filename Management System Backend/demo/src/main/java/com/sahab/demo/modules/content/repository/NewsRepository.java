
        package com.sahab.demo.modules.content.repository;

import com.sahab.demo.modules.content.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {

    List<News> findAllByPublishedTrueOrderByCreatedAtDesc();

    List<News> findAllByOrderByCreatedAtDesc();

    long countByPublishedTrue();
}
