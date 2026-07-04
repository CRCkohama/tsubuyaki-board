package com.example.tsubuyaki.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 指定されたidのタグが存在しない場合に、HTTP 404としてレスポンスを返すための例外です。
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TagNotFoundException extends RuntimeException {
}
