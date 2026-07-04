package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.web.dto.PostForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;

    public PostService(PostRepository repository) {
        this.repository = repository;
    }

    public List<Post> latest() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public Post findById(Long id) {
        // 詳細画面では1件だけ取得し、存在しないidの場合はControllerへ404用の例外を伝える。
        return repository.findById(id).orElseThrow(PostNotFoundException::new);
    }

    @Transactional
    public Post create(PostForm form) {
        return repository.save(new Post(form.getAuthor(), form.getBody(), LocalDateTime.now()));
    }

    /**
     * 指定されたキーワードで投稿を検索します。
     * キーワードが未指定、空文字、または全角・半角スペースのみの場合は、
     * 検索を行わずに従来の全件新着順表示（latest()）を返します。
     * 
     * @param query 検索キーワード
     * @return 検索結果または全件の投稿リスト（最大50件）
     */
    public List<Post> search(String query) {
        // キーワードがnull、または全角・半角スペースを除去した結果が空文字の場合はフォールバックします。
        // String.strip() は全角スペース（\u3000）もトリミング対象に含みます。
        if (query == null || query.strip().isEmpty()) {
            return latest();
        }
        // 部分一致する投稿を新着順で最大50件検索します。
        return repository.findTop50ByBodyContainingOrderByCreatedAtDesc(query);
    }
}
