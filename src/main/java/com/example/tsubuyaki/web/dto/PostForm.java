package com.example.tsubuyaki.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PostForm {

    @NotBlank(message = "投稿者名を入力してください")
    @Size(min = 1, max = 30, message = "投稿者名は 30 文字以内で入力してください")
    private String author;

    @NotBlank(message = "本文を入力してください")
    @Size(min = 1, max = 280, message = "本文は 280 文字以内で入力してください")
    private String body;

    // アバター用カラーコード（#HEX形式）。必須バリデーションとフォーマット検証を設定。
    // デフォルト色としてブラックを設定しておきます。
    @NotBlank(message = "アバターのカラーを選択してください")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "アバターカラーの形式が正しくありません")
    private String color = "#000000";

    public PostForm() {
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    // アバターカラーを取得します。
    public String getColor() {
        return color;
    }

    // アバターカラーを設定します。
    public void setColor(String color) {
        this.color = color;
    }
}
