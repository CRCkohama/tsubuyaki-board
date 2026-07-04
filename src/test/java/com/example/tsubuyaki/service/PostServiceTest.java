package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostServiceTest {

    private PostRepository repository;
    private PostService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(PostRepository.class);
        service = new PostService(repository);
    }

    @Test
    @DisplayName("検索_有効なキーワードのとき_リポジトリのキーワード検索を呼び出す")
    void 検索_有効なキーワードのとき_リポジトリのキーワード検索を呼び出す() {
        List<Post> expected = List.of(new Post("user1", "Javaプログラミング", LocalDateTime.now()));
        // Note: findTop50ByBodyContainingOrderByCreatedAtDesc メソッドはまだ定義されていないため、
        // ここでもコンパイルエラーが発生します。
        when(repository.findTop50ByBodyContainingOrderByCreatedAtDesc("Java")).thenReturn(expected);

        List<Post> results = service.search("Java");

        assertThat(results).isEqualTo(expected);
        verify(repository).findTop50ByBodyContainingOrderByCreatedAtDesc("Java");
    }

    @Test
    @DisplayName("検索_キーワードがnullのとき_最新50件取得にフォールバックする")
    void 検索_キーワードがnullのとき_最新50件取得にフォールバックする() {
        List<Post> expected = List.of(new Post("user1", "最新投稿", LocalDateTime.now()));
        when(repository.findTop50ByOrderByCreatedAtDesc()).thenReturn(expected);

        List<Post> results = service.search(null);

        assertThat(results).isEqualTo(expected);
        verify(repository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("検索_キーワードが空文字のとき_最新50件取得にフォールバックする")
    void 検索_キーワードが空文字のとき_最新50件取得にフォールバックする() {
        List<Post> expected = List.of(new Post("user1", "最新投稿", LocalDateTime.now()));
        when(repository.findTop50ByOrderByCreatedAtDesc()).thenReturn(expected);

        List<Post> results = service.search("");

        assertThat(results).isEqualTo(expected);
        verify(repository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("検索_キーワードがスペースのみのとき_最新50件取得にフォールバックする")
    void 検索_キーワードがスペースのみのとき_最新50件取得にフォールバックする() {
        List<Post> expected = List.of(new Post("user1", "最新投稿", LocalDateTime.now()));
        when(repository.findTop50ByOrderByCreatedAtDesc()).thenReturn(expected);

        // 半角スペース、全角スペース、タブや改行などが含まれるケース
        List<Post> results = service.search(" 　\t\n ");

        assertThat(results).isEqualTo(expected);
        verify(repository).findTop50ByOrderByCreatedAtDesc();
    }
}
