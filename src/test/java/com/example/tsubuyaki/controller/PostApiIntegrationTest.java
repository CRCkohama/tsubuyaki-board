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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostApiIntegrationTest {

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
    @DisplayName("API投稿取得_パラメータなし_有効な投稿のみを取得する")
    void API投稿取得_パラメータなし_有効な投稿のみを取得する() throws Exception {
        Post postActive = postRepository.save(new Post("user1", "有効な投稿", "#000000", LocalDateTime.now()));
        
        Post postDeleted = new Post("user2", "ゴミ箱の投稿", "#3B82F6", LocalDateTime.now());
        postDeleted.delete();
        postRepository.save(postDeleted);

        Post postPurged = new Post("user3", "完全削除された投稿", "#EF4444", LocalDateTime.now());
        postPurged.delete();
        postPurged.purge();
        postRepository.save(postPurged);

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].author", is("user1")))
                .andExpect(jsonPath("$[0].deleteStatus", is(0)))
                .andExpect(jsonPath("$[0].deletedAt").value(is(org.hamcrest.Matchers.nullValue())))
                .andExpect(jsonPath("$[0].purgedAt").value(is(org.hamcrest.Matchers.nullValue())));
    }

    @Test
    @DisplayName("API投稿取得_allがtrueのとき_削除済みおよび完全削除済みも取得する")
    void API投稿取得_allがtrueのとき_削除済みおよび完全削除済みも取得する() throws Exception {
        Post postActive = postRepository.save(new Post("user1", "有効な投稿", "#000000", LocalDateTime.now().minusMinutes(2)));
        
        Post postDeleted = new Post("user2", "ゴミ箱の投稿", "#3B82F6", LocalDateTime.now().minusMinutes(1));
        postDeleted.delete();
        postRepository.save(postDeleted);

        Post postPurged = new Post("user3", "完全削除された投稿", "#EF4444", LocalDateTime.now());
        postPurged.delete();
        postPurged.purge();
        postRepository.save(postPurged);

        mockMvc.perform(get("/api/posts").param("all", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                // 新着順（user3 -> user2 -> user1）の並び順検証
                .andExpect(jsonPath("$[0].author", is("user3")))
                .andExpect(jsonPath("$[0].deleteStatus", is(2)))
                .andExpect(jsonPath("$[0].deletedAt", is(notNullValue())))
                .andExpect(jsonPath("$[0].purgedAt", is(notNullValue())))
                
                .andExpect(jsonPath("$[1].author", is("user2")))
                .andExpect(jsonPath("$[1].deleteStatus", is(1)))
                .andExpect(jsonPath("$[1].deletedAt", is(notNullValue())))
                .andExpect(jsonPath("$[1].purgedAt").value(is(org.hamcrest.Matchers.nullValue())))
                
                .andExpect(jsonPath("$[2].author", is("user1")))
                .andExpect(jsonPath("$[2].deleteStatus", is(0)))
                .andExpect(jsonPath("$[2].deletedAt").value(is(org.hamcrest.Matchers.nullValue())))
                .andExpect(jsonPath("$[2].purgedAt").value(is(org.hamcrest.Matchers.nullValue())));
    }

    @Test
    @DisplayName("APIドキュメント取得_docsへアクセス_OpenAPI定義を返却する")
    void APIドキュメント取得_docsへアクセス_OpenAPI定義を返却する() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.openapi", is("3.0.0")))
                .andExpect(jsonPath("$.paths['/api/posts']").exists());
    }
}
