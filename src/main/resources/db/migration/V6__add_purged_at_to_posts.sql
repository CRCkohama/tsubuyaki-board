-- postsテーブルに完全論理削除日時（purged_at）を追加
ALTER TABLE posts ADD purged_at TIMESTAMP NULL;
