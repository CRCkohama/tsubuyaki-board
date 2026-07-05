package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.web.dto.PostResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 投稿データのREST APIを提供するエンドポイントコントローラー。
 */
@RestController
public class PostApiController {

    private final PostRepository postRepository;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public PostApiController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * 投稿一覧をJSON形式で取得します。
     *
     * @param all trueの場合、削除済みやゴミ箱クリア済みの投稿もすべて含めて返却します。
     *            デフォルト(false)の場合は、有効な投稿のみを返却します。
     * @return 投稿レスポンスDTOのリスト（最大50件）
     */
    @GetMapping("/api/posts")
    public List<PostResponse> getPosts(
            @RequestParam(value = "all", required = false, defaultValue = "false") boolean all) {
        
        List<Post> posts;
        if (all) {
            // 削除状態に関わらず、すべての投稿から最新50件を新着順で取得します
            posts = postRepository.findTop50ByOrderByCreatedAtDesc();
        } else {
            // 有効な投稿（論理削除およびゴミ箱から完全削除されていないもの）のみを最新50件新着順で取得します
            posts = postRepository.findTop50ByDeletedAtIsNullAndPurgedAtIsNullOrderByCreatedAtDesc();
        }

        // エンティティリストをDTOリストへ変換してJSONレスポンスとします
        return posts.stream()
                .map(PostResponse::new)
                .collect(Collectors.toList());
    }
}
