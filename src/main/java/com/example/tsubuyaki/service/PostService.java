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
    private final com.example.tsubuyaki.repository.TagRepository tagRepository;

    public PostService(PostRepository repository, com.example.tsubuyaki.repository.TagRepository tagRepository) {
        this.repository = repository;
        this.tagRepository = tagRepository;
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
        Post post = new Post(form.getAuthor(), form.getBody(), form.getColor(), LocalDateTime.now());

        // カンマ区切りのタグ入力文字列をパースして登録します。
        String tagsInput = form.getTagsInput();
        if (tagsInput != null && !tagsInput.strip().isEmpty()) {
            // カンマ、全角カンマ、読点で分割します。
            String[] rawTags = tagsInput.split("[,，、]");
            for (String rawTag : rawTags) {
                String tagName = rawTag.strip();
                if (!tagName.isEmpty()) {
                    // 既存タグがあれば再利用し、なければ新規永続化します。
                    com.example.tsubuyaki.domain.Tag tag = tagRepository.findByName(tagName)
                            .orElseGet(() -> tagRepository.save(new com.example.tsubuyaki.domain.Tag(tagName)));
                    post.getTags().add(tag);
                }
            }
        }

        return repository.save(post);
    }

    /**
     * 指定された投稿から特定のタグの関連付けを解除（削除）します。
     * 解除された結果、どの投稿からも紐付けられなくなったタグは自動でクリーンアップ（物理削除）します。
     */
    @Transactional
    public void removeTagFromPost(Long postId, Long tagId) {
        Post post = repository.findById(postId).orElseThrow(PostNotFoundException::new);
        com.example.tsubuyaki.domain.Tag tag = tagRepository.findById(tagId).orElseThrow(TagNotFoundException::new);

        post.removeTag(tag);
        repository.save(post);

        // 浮いたタグの判定：このタグに紐づくPostが存在しなければ、DBから削除します。
        if (!repository.existsByTagsId(tagId)) {
            tagRepository.delete(tag);
        }
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
