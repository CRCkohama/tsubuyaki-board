package com.example.tsubuyaki.domain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.Set;
import java.util.HashSet;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @SequenceGenerator(name = "posts_seq_gen", sequenceName = "posts_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "posts_seq_gen")
    private Long id;

    @Column(name = "author", length = 30, nullable = false)
    private String author;

    @Column(name = "body", length = 280, nullable = false)
    private String body;

    // アバター表示用のRGBカラーコード（#HEX値）を格納します。
    @Column(name = "color", length = 7, nullable = false)
    private String color;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    protected Post() {
        // JPA
    }

    // 互換性維持のためのカラー未指定コンストラクタ。デフォルト色としてブラック（#000000）を割り当てます。
    public Post(String author, String body, LocalDateTime createdAt) {
        this(author, body, "#000000", createdAt);
    }

    // アバターカラー付きのメインコンストラクタです。
    public Post(String author, String body, String color, LocalDateTime createdAt) {
        this.author = author;
        this.body = body;
        this.color = color;
        this.createdAt = createdAt;
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

    // アバターのRGBカラー（#HEX形式）を取得します。
    public String getColor() {
        return color;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // 投稿に紐付いているタグを取得します。
    public Set<Tag> getTags() {
        return tags;
    }

    // 投稿から指定されたタグの関連付けを解除（削除）します。
    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Post other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
