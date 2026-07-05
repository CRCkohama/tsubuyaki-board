package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostDeleteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private com.example.tsubuyaki.repository.PostLikeRepository postLikeRepository;

    @BeforeEach
    void setUp() {
        postLikeRepository.deleteAll();
        postRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        postLikeRepository.deleteAll();
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("投稿削除_有効なID_削除して一覧へリダイレクトする")
    void 投稿削除_有効なID_削除して一覧へリダイレクトする() throws Exception {
        Post post = postRepository.save(new Post("user1", "削除テスト投稿", "#000000", LocalDateTime.now()));

        mockMvc.perform(post("/posts/{id}/delete", post.getId())
                        .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        Post updated = postRepository.findById(post.getId()).orElseThrow();
        // getDeletedAt()は未定義のため、コンパイルエラーREDになります。
        assertThat(updated.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("投稿詳細_削除済みID_404エラーを返す")
    void 投稿詳細_削除済みID_404エラーを返す() throws Exception {
        Post post = new Post("user1", "削除済み投稿", "#000000", LocalDateTime.now());
        // delete()は未定義のため、コンパイルエラーREDになります。
        post.delete();
        Post saved = postRepository.save(post);

        mockMvc.perform(get("/posts/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }
}
