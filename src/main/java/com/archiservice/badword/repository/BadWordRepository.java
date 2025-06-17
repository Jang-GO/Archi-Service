package com.archiservice.badword.repository;

import com.archiservice.badword.domain.BadWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadWordRepository extends JpaRepository<BadWord, Long> {

    @Query("SELECT bw.word FROM BadWord bw")
    List<String> findAllWords();
}

