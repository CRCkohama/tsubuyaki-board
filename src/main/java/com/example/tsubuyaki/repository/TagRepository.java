package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    // タグ名から既存のタグオブジェクトを検索します（重複登録を防ぐため）
    Optional<Tag> findByName(String name);
}
