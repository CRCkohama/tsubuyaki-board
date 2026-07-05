-- postsテーブルに論理削除日時（deleted_at）を追加
ALTER TABLE posts ADD deleted_at TIMESTAMP NULL;
