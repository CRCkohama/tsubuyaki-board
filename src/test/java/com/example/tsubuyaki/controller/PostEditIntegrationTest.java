package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.service.ClientHashGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class PostEditIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private com.example.tsubuyaki.repository.PostLikeRepository postLikeRepository;

    @MockitoBean
    private ClientHashGenerator clientHashGenerator;

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
    @DisplayName("編集画面表示_作成者本人のとき_200を返し編集画面を表示する")
    void 編集画面表示_作成者本人のとき_200を返し編集画面を表示する() throws Exception {
        Post post = new Post("user1", "本文", "#000000", LocalDateTime.now());
        post.setClientHash("hash123");
        Post saved = postRepository.save(post);

        when(clientHashGenerator.generate(any(), any())).thenReturn("hash123");

        mockMvc.perform(get("/posts/{id}/edit", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/edit"));
    }

    @Test
    @DisplayName("編集画面表示_他人のとき_403を返す")
    void 編集画面表示_他人のとき_403を返す() throws Exception {
        Post post = new Post("user1", "本文", "#000000", LocalDateTime.now());
        post.setClientHash("hash123");
        Post saved = postRepository.save(post);

        when(clientHashGenerator.generate(any(), any())).thenReturn("hash999");

        mockMvc.perform(get("/posts/{id}/edit", saved.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("投稿更新_作成者本人のとき_更新して詳細画面へリダイレクトする")
    void 投稿更新_作成者本人のとき_更新して詳細画面へリダイレクトする() throws Exception {
        Post post = new Post("user1", "元の本文", "#000000", LocalDateTime.now());
        post.setClientHash("hash123");
        Post saved = postRepository.save(post);

        when(clientHashGenerator.generate(any(), any())).thenReturn("hash123");

        mockMvc.perform(post("/posts/{id}/edit", saved.getId())
                        .param("author", "user1")
                        .param("body", "更新後の本文")
                        .param("color", "#EF4444")
                        .param("tagsInput", "")
                        .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/" + saved.getId()));

        Post updated = postRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getBody()).isEqualTo("更新後の本文");
        assertThat(updated.getColor()).isEqualTo("#EF4444");
        assertThat(updated.getEditedAt()).isNotNull();
    }

    @Test
    @DisplayName("投稿更新_他人のとき_403を返す")
    void 投稿更新_他人のとき_403を返す() throws Exception {
        Post post = new Post("user1", "元の本文", "#000000", LocalDateTime.now());
        post.setClientHash("hash123");
        Post saved = postRepository.save(post);

        when(clientHashGenerator.generate(any(), any())).thenReturn("hash999");

        mockMvc.perform(post("/posts/{id}/edit", saved.getId())
                        .param("author", "user1")
                        .param("body", "更新後の本文")
                        .param("color", "#EF4444")
                        .param("tagsInput", "")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
