-- postsテーブルに投稿作成者のクライアントハッシュ（client_hash）および編集日時（edited_at）を追加
ALTER TABLE posts ADD client_hash VARCHAR2(8 CHAR) NULL;
ALTER TABLE posts ADD edited_at TIMESTAMP NULL;
