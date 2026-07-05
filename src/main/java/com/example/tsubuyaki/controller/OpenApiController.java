package com.example.tsubuyaki.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 簡易OpenAPIドキュメントを配信するコントローラー。
 */
@RestController
public class OpenApiController {

    /**
     * クラスパスから openapi.json をロードして返却します。
     *
     * @return openapi.json の内容を含むJSONレスポンス
     * @throws IOException ファイル読み込み失敗時
     */
    @GetMapping(value = "/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getApiDocs() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/openapi.json");
        byte[] bytes = Files.readAllBytes(Paths.get(resource.getURI()));
        String content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok(content);
    }
}
