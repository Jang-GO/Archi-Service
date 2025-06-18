package com.archiservice.badword.repository;

import com.archiservice.badword.domain.AllowedWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllowedWordRepository extends JpaRepository<AllowedWord, Long> {

    @Query("SELECT aw.word FROM AllowedWord aw")
    List<String> findAllWords();
}

