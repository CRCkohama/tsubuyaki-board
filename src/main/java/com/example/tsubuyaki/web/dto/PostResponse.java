package com.example.tsubuyaki.web.dto;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API用の投稿レスポンスDTOクラス。
 */
public class PostResponse {

    private final Long id;
    private final String author;
    private final String body;
    private final String color;
    private final String createdAt;
    private final List<String> tags;
    private final int deleteStatus;
    private final String deletedAt;
    private final String purgedAt;
    private final String editedAt;

    /**
     * PostエンティティからレスポンスDTOを構築します。
     *
     * @param post 投稿エンティティ
     */
    public PostResponse(Post post) {
        this.id = post.getId();
        this.author = post.getAuthor();
        this.body = post.getBody();
        this.color = post.getColor();
        
        // 日時情報をISO-8601形式の文字列に変換します
        this.createdAt = post.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        // 関連するタグの名前だけを抽出したリストを作成します
        this.tags = post.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());

        // 削除ステータスおよび日時の設定
        if (post.isPurged()) {
            this.deleteStatus = 2; // 完全削除済み
            this.purgedAt = post.getPurgedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            this.deletedAt = post.getDeletedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else if (post.isDeleted()) {
            this.deleteStatus = 1; // ゴミ箱内（論理削除済み）
            this.purgedAt = null;
            this.deletedAt = post.getDeletedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else {
            this.deleteStatus = 0; // 有効
            this.purgedAt = null;
            this.deletedAt = null;
        }

        // 編集日時の設定
        this.editedAt = post.getEditedAt() != null
                ? post.getEditedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;
    }

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }

    public String getColor() {
        return color;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public List<String> getTags() {
        return tags;
    }

    public int getDeleteStatus() {
        return deleteStatus;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public String getPurgedAt() {
        return purgedAt;
    }

    public String getEditedAt() {
        return editedAt;
    }
}
