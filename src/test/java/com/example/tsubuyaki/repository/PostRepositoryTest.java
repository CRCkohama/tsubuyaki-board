package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("キーワード検索_部分一致する投稿があるとき_新着順で最大50件取得できる")
    void キーワード検索_部分一致する投稿があるとき_新着順で最大50件取得できる() {
        LocalDateTime base = LocalDateTime.parse("2026-06-01T09:00:00");
        
        // 検索キーワード「プログラミング」を含む投稿を保存
        postRepository.save(new Post("user1", "プログラミングは楽しいです", base.plusMinutes(1)));
        postRepository.save(new Post("user2", "Javaプログラミングの学習", base.plusMinutes(2)));
        
        // 検索キーワードを含まない投稿を保存
        postRepository.save(new Post("user3", "こんにちは世界", base.plusMinutes(3)));
        
        // 50件制限を確認するために「プログラミング」を含む投稿をさらに50件（合計52件）保存
        for (int i = 4; i <= 53; i++) {
            postRepository.save(new Post("user" + i, "プログラミング課題" + i, base.plusMinutes(i)));
        }

        // キーワード「プログラミング」で検索を実行
        // 注: このメソッドはまだ PostRepository に定義されていないため、コンパイルエラー（RED）になります。
        List<Post> results = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("プログラミング");

        // 検索結果の検証
        assertThat(results).hasSize(50); // 最大50件制限
        assertThat(results.get(0).getBody()).isEqualTo("プログラミング課題53"); // 最新（created_atが最も未来）が先頭
        assertThat(results.get(49).getBody()).isEqualTo("プログラミング課題4"); // 50番目の投稿（インデックス49）の本文を検証
        
        // 検索結果に「こんにちは世界」が含まれていないことを検証
        for (Post post : results) {
            assertThat(post.getBody()).contains("プログラミング");
            assertThat(post.getBody()).doesNotContain("こんにちは世界");
        }
    }

    @Test
    @DisplayName("キーワード検索_部分一致する投稿がないとき_空リストが返る")
    void キーワード検索_部分一致する投稿がないとき_空リストが返る() {
        postRepository.save(new Post("user1", "こんにちは世界", LocalDateTime.now()));

        List<Post> results = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("プログラミング");

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("投稿保存_カラー情報を指定したとき_正しく永続化され取得できる")
    void 投稿保存_カラー情報を指定したとき_正しく永続化され取得できる() {
        // 新しいカラー引数付きのコンストラクタを呼び出します（未実装のためコンパイルエラーREDになります）。
        Post post = new Post("user1", "カラーテスト投稿", "#EF4444", LocalDateTime.now());
        Post saved = postRepository.save(post);

        Post found = postRepository.findById(saved.getId()).orElseThrow();
        // getColor() も未実装のためコンパイルエラーREDになります。
        assertThat(found.getColor()).isEqualTo("#EF4444");
    }
}
