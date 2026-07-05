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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostTrashIntegrationTest {

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
    @DisplayName("ごみ箱画面_アクセスしたとき_ごみ箱内の投稿が表示される")
    void ごみ箱画面_アクセスしたとき_ごみ箱内の投稿が表示される() throws Exception {
        Post postActive = postRepository.save(new Post("userA", "有効な投稿", "#000000", LocalDateTime.now()));
        
        Post postDeleted = new Post("userB", "ゴミ箱の投稿", "#3B82F6", LocalDateTime.now());
        postDeleted.delete();
        postRepository.save(postDeleted);

        Post postPurged = new Post("userC", "完全削除された投稿", "#EF4444", LocalDateTime.now());
        postPurged.delete();
        postPurged.purge();
        postRepository.save(postPurged);

        mockMvc.perform(get("/posts/trash"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ゴミ箱の投稿")))
                .andExpect(content().string(not(containsString("有効な投稿"))))
                .andExpect(content().string(not(containsString("完全削除された投稿"))));
    }

    @Test
    @DisplayName("投稿復元_有効なID_復元してごみ箱画面へリダイレクトする")
    void 投稿復元_有効なID_復元してごみ箱画面へリダイレクトする() throws Exception {
        Post post = new Post("user1", "復元対象投稿", "#000000", LocalDateTime.now());
        post.delete();
        Post saved = postRepository.save(post);

        mockMvc.perform(post("/posts/{id}/restore", saved.getId())
                        .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/trash"));

        Post updated = postRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("ごみ箱クリア_クリアアクション実行_ごみ箱内の投稿がすべて完全削除される")
    void ごみ箱クリア_クリアアクション実行_ごみ箱内の投稿がすべて完全削除される() throws Exception {
        Post post1 = new Post("user1", "ゴミ箱投稿1", "#000000", LocalDateTime.now());
        post1.delete();
        Post post2 = new Post("user2", "ゴミ箱投稿2", "#3B82F6", LocalDateTime.now());
        post2.delete();
        postRepository.save(post1);
        postRepository.save(post2);

        mockMvc.perform(post("/posts/trash/empty")
                        .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/trash"));

        Post updated1 = postRepository.findById(post1.getId()).orElseThrow();
        Post updated2 = postRepository.findById(post2.getId()).orElseThrow();
        assertThat(updated1.getPurgedAt()).isNotNull();
        assertThat(updated2.getPurgedAt()).isNotNull();
    }
}
