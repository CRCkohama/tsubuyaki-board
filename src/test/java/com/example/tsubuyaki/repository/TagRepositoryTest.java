package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("タグ保存_有効な名前を指定したとき_正しく永続化され取得できる")
    void タグ保存_有効な名前を指定したとき_正しく永続化され取得できる() {
        Tag tag = new Tag("SpringBoot");
        Tag saved = tagRepository.save(tag);

        assertThat(saved.getId()).isNotNull();

        Optional<Tag> found = tagRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("SpringBoot");
    }

    @Test
    @DisplayName("タグ保存_重複する名前を指定したとき_一意制約違反でエラーが発生する")
    void タグ保存_重複する名前を指定したとき_一意制約違反でエラーが発生する() {
        tagRepository.save(new Tag("Java"));
        tagRepository.flush();

        assertThatThrownBy(() -> {
            tagRepository.save(new Tag("Java"));
            tagRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("タグ検索_名前で検索したとき_存在するタグが取得できる")
    void タグ検索_名前で検索したとき_存在するタグが取得できる() {
        Tag tag = tagRepository.save(new Tag("Web"));
        tagRepository.flush();

        Optional<Tag> found = tagRepository.findByName("Web");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(tag.getId());
    }
}
