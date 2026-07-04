package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 最新の投稿50件を新着順で取得します。
    List<Post> findTop50ByOrderByCreatedAtDesc();

    // 指定されたキーワード（bodyの一部）に部分一致する投稿を、新着順で最大50件取得します。
    // Spring Data JPAの派生クエリを使用することで、SQLインジェクション脆弱性を防ぎます。
    List<Post> findTop50ByBodyContainingOrderByCreatedAtDesc(String body);

    // 未いいね状態ではpost_likesにロック対象行が無いため、必ず存在する親の投稿行をロックして同時トグルを直列化する。
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Optional<Post> findByIdForUpdate(@Param("id") Long id);

    // 指定されたタグIDを持つ投稿が存在するかチェックします。タグ削除時の「浮いたタグ」判定に用います。
    boolean existsByTagsId(Long tagId);

    // 指定されたタグ名に関連する投稿を新着順で取得します。
    List<Post> findByTagsNameOrderByCreatedAtDesc(String tagName);
}
