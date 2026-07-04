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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class PostListIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("投稿一覧_DB空のとき_空メッセージを表示する")
    void 投稿一覧_DB空のとき_空メッセージを表示する() throws Exception {
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_51件あるとき_新着50件だけを表示する")
    void 投稿一覧_51件あるとき_新着50件だけを表示する() throws Exception {
        LocalDateTime base = LocalDateTime.parse("2026-06-01T09:00:00");
        for (int i = 1; i <= 51; i++) {
            String sequence = "%03d".formatted(i);
            postRepository.save(new Post("user-" + sequence, "本文-" + sequence, base.plusMinutes(i)));
        }

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("posts"))
                .andExpect(content().string(containsString("本文-051")))
                .andExpect(content().string(containsString("本文-002")))
                .andExpect(content().string(not(containsString("本文-001"))))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<Post> posts = (List<Post>) result.getModelAndView().getModel().get("posts");
        assertThat(posts).hasSize(50);
    }

    @Test
    @DisplayName("投稿一覧_表示時_更新ボタンから_postsへ遷移できる")
    void 投稿一覧_表示時_更新ボタンから_postsへ遷移できる() throws Exception {
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<button")))
                .andExpect(content().string(containsString("更新")))
                .andExpect(content().string(containsString("action=\"/posts\"")));
    }

    @Test
    @DisplayName("投稿一覧_投稿があるとき_投稿者_内容_投稿日の順に表示する")
    void 投稿一覧_投稿があるとき_投稿者_内容_投稿日の順に表示する() throws Exception {
        postRepository.save(new Post(
                "alice",
                "M1の投稿一覧を実装します",
                LocalDateTime.parse("2026-06-01T09:30:00")));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("M1の投稿一覧を実装します")))
                .andExpect(content().string(containsString("2026-06-01 09:30")))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        int authorIndex = html.indexOf("alice");
        int bodyIndex = html.indexOf("M1の投稿一覧を実装します");
        int createdAtIndex = html.indexOf("2026-06-01 09:30");

        assertThat(authorIndex).isLessThan(bodyIndex);
        assertThat(bodyIndex).isLessThan(createdAtIndex);
    }

    @Test
    @DisplayName("投稿一覧_検索キーワードを指定したとき_部分一致する投稿のみ表示しキーワードをリテインする")
    void 投稿一覧_検索キーワードを指定したとき_部分一致する投稿のみ表示しキーワードをリテインする() throws Exception {
        postRepository.save(new Post("user1", "Javaプログラミングの基礎", LocalDateTime.now()));
        postRepository.save(new Post("user2", "Spring BootでWebアプリ開発", LocalDateTime.now()));

        mockMvc.perform(get("/posts").param("q", "Java"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("q", "Java"))
                .andExpect(content().string(containsString("Javaプログラミングの基礎")))
                .andExpect(content().string(not(containsString("Spring BootでWebアプリ開発"))));
    }

    @Test
    @DisplayName("投稿一覧_検索結果が0件のとき_該当なしメッセージを表示する")
    void 投稿一覧_検索結果が0件のとき_該当なしメッセージを表示する() throws Exception {
        postRepository.save(new Post("user1", "Javaプログラミングの基礎", LocalDateTime.now()));

        mockMvc.perform(get("/posts").param("q", "Ruby"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("q", "Ruby"))
                .andExpect(content().string(containsString("該当する投稿は見つかりませんでした")));
    }

    @Test
    @DisplayName("投稿一覧_検索キーワードがスペースのみのとき_全件を表示する")
    void 投稿一覧_検索キーワードがスペースのみのとき_全件を表示する() throws Exception {
        postRepository.save(new Post("user1", "Javaプログラミングの基礎", LocalDateTime.now()));
        postRepository.save(new Post("user2", "Spring BootでWebアプリ開発", LocalDateTime.now()));

        mockMvc.perform(get("/posts").param("q", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("q", "   "))
                .andExpect(content().string(containsString("Javaプログラミングの基礎")))
                .andExpect(content().string(containsString("Spring BootでWebアプリ開発")));
    }
}

