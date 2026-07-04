package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class TagControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private com.example.tsubuyaki.repository.PostLikeRepository postLikeRepository;

    @BeforeEach
    void setUp() {
        postLikeRepository.deleteAll();
        postRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        postLikeRepository.deleteAll();
        postRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @Test
    @DisplayName("タグ別投稿一覧_存在するタグ_関連投稿を新着順で表示する")
    void タグ別投稿一覧_存在するタグ_関連投稿を新着順で表示する() throws Exception {
        Tag tag = tagRepository.save(new Tag("java"));

        Post post1 = new Post("user1", "javaのテスト投稿1", "#000000", LocalDateTime.now());
        post1.getTags().add(tag);
        postRepository.save(post1);

        Post post2 = new Post("user2", "javaのテスト投稿2", "#3B82F6", LocalDateTime.now().plusSeconds(1));
        post2.getTags().add(tag);
        postRepository.save(post2);

        // 関連のない別投稿
        Post post3 = new Post("user3", "無関係な投稿", "#10B981", LocalDateTime.now());
        postRepository.save(post3);

        mockMvc.perform(get("/tags/java"))
                .andExpect(status().isOk())
                .andExpect(view().name("tags/list"))
                .andExpect(content().string(containsString("javaのテスト投稿1")))
                .andExpect(content().string(containsString("javaのテスト投稿2")))
                .andExpect(content().string(not(containsString("無関係な投稿"))));
    }

    @Test
    @DisplayName("タグ別投稿一覧_存在しないタグ_404エラーを返す")
    void タグ別投稿一覧_存在しないタグ_404エラーを返す() throws Exception {
        mockMvc.perform(get("/tags/nonexist"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("タグ削除アクション_POST_関連付けを削除し詳細画面にリダイレクトする")
    void タグ削除アクション_POST_関連付けを削除し詳細画面にリダイレクトする() throws Exception {
        Tag tag = tagRepository.save(new Tag("spring"));
        Post post = new Post("user1", "springのテスト投稿", "#000000", LocalDateTime.now());
        post.getTags().add(tag);
        Post savedPost = postRepository.save(post);

        mockMvc.perform(post("/posts/{postId}/tags/{tagId}/delete", savedPost.getId(), tag.getId())
                        .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/" + savedPost.getId()));

        // 保存された投稿を取得し、タグの紐付けが解除されているか検証
        Post updatedPost = postRepository.findById(savedPost.getId()).orElseThrow();
        assertThat(updatedPost.getTags()).isEmpty();
    }
}
