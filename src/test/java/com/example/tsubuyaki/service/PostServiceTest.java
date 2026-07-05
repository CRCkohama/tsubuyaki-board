package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostServiceTest {

    private PostRepository repository;
    private com.example.tsubuyaki.repository.TagRepository tagRepository;
    private PostService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(PostRepository.class);
        tagRepository = Mockito.mock(com.example.tsubuyaki.repository.TagRepository.class);
        service = new PostService(repository, tagRepository);
    }

    @Test
    @DisplayName("検索_有効なキーワードのとき_リポジトリのキーワード検索を呼び出す")
    void 検索_有効なキーワードのとき_リポジトリのキーワード検索を呼び出す() {
        List<Post> expected = List.of(new Post("user1", "Javaプログラミング", LocalDateTime.now()));
        // Note: findTop50ByBodyContainingOrderByCreatedAtDesc メソッドはまだ定義されていないため、
        // ここでもコンパイルエラーが発生します。
        when(repository.findTop50ByBodyContainingAndDeletedAtIsNullAndPurgedAtIsNullOrderByCreatedAtDesc("Java")).thenReturn(expected);

        List<Post> results = service.search("Java");

        assertThat(results).isEqualTo(expected);
        verify(repository).findTop50ByBodyContainingAndDeletedAtIsNullAndPurgedAtIsNullOrderByCreatedAtDesc("Java");
    }

    @Test
    @DisplayName("検索_キーワードがnullのとき_最新50件取得にフォールバックする")
    void 検索_キーワードがnullのとき_最新50件取得にフォールバックする() {
        List<Post> expected = List.of(new Post("user1", "最新投稿", LocalDateTime.now()));
        when(repository.findTop50ByDeletedAtIsNullAndPurgedAtIsNullOrderByCreatedAtDesc()).thenReturn(expected);

        List<Post> results = service.search(null);

        assertThat(results).isEqualTo(expected);
        verify(repository).findTop50ByDeletedAtIsNullAndPurgedAtIsNullOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("検索_キーワードが空文字のとき_最新50件取得にフォールバックする")
    void 検索_キーワードが空文字のとき_最新50件取得にフォールバックする() {
        List<Post> expected = List.of(new Post("user1", "最新投稿", LocalDateTime.now()));
        when(repository.findTop50ByDeletedAtIsNullAndPurgedAtIsNullOrderByCreatedAtDesc()).thenReturn(expected);

        List<Post> results = service.search("");

        assertThat(results).isEqualTo(expected);
        verify(repository).findTop50ByDeletedAtIsNullAndPurgedAtIsNullOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("検索_キーワードがスペースのみのとき_最新50件取得にフォールバックする")
    void 検索_キーワードがスペースのみのとき_最新50件取得にフォールバックする() {
        List<Post> expected = List.of(new Post("user1", "最新投稿", LocalDateTime.now()));
        when(repository.findTop50ByDeletedAtIsNullAndPurgedAtIsNullOrderByCreatedAtDesc()).thenReturn(expected);

        // 半角スペース、全角スペース、タブや改行などが含まれるケース
        List<Post> results = service.search(" 　\t\n ");

        assertThat(results).isEqualTo(expected);
        verify(repository).findTop50ByDeletedAtIsNullAndPurgedAtIsNullOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("作成_カラー情報を指定したとき_リポジトリ保存時にカラー情報が引き渡される")
    void 作成_カラー情報を指定したとき_リポジトリ保存時にカラー情報が引き渡される() {
        com.example.tsubuyaki.web.dto.PostForm form = new com.example.tsubuyaki.web.dto.PostForm();
        form.setAuthor("user1");
        form.setBody("サービス層のカラーテスト投稿");
        // colorのsetter/getterはまだ未実装のためコンパイルエラーREDになります。
        form.setColor("#EF4444");

        Post savedPost = new Post("user1", "サービス層のカラーテスト投稿", "#EF4444", LocalDateTime.now());
        when(repository.save(Mockito.any(Post.class))).thenReturn(savedPost);

        Post result = service.create(form, "hash123");

        assertThat(result).isNotNull();
        // PostService.create メソッド内で、渡されたカラーが Post にマッピングされているかをモックキャプチャなどで検証したいですが、
        // 戻り値の Post（モックで返却するもの）または repository.save(arg) の引数検証を行います。
        verify(repository).save(Mockito.argThat(post -> "#EF4444".equals(post.getColor())));
    }

    @Test
    @DisplayName("作成_タグ情報を指定したとき_カンマ区切りでパースされて保存される")
    void 作成_タグ情報を指定したとき_カンマ区切りでパースされて保存される() {
        com.example.tsubuyaki.web.dto.PostForm form = new com.example.tsubuyaki.web.dto.PostForm();
        form.setAuthor("user1");
        form.setBody("本文");
        form.setColor("#000000");
        // tagsInputフィールドは未実装のためコンパイルエラーREDになります。
        form.setTagsInput("java, spring, Web");

        Post savedPost = new Post("user1", "本文", "#000000", LocalDateTime.now());
        when(repository.save(Mockito.any(Post.class))).thenReturn(savedPost);
        // タグ名「java」「spring」「Web」はいずれも新規タグであるとしてモックを設定
        when(tagRepository.findByName(Mockito.anyString())).thenReturn(java.util.Optional.empty());
        // saveされたTagそのものを返すモック挙動を設定してnull追加を防ぎます。フルパッケージ名でTagを指定します。
        when(tagRepository.save(Mockito.any(com.example.tsubuyaki.domain.Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(form, "hash123");

        // repository.save()に渡されたPostに、3つのタグが関連付けられていることを検証します。
        verify(repository).save(Mockito.argThat(post -> {
            return post.getTags() != null && post.getTags().size() == 3;
        }));
    }

    @Test
    @DisplayName("タグ削除_投稿からタグを指定したとき_関連付けが解除され浮いたタグも削除される")
    void タグ削除_投稿からタグを指定したとき_関連付けが解除され浮いたタグも削除される() {
        Post post = new Post("user1", "本文", "#000000", LocalDateTime.now());
        com.example.tsubuyaki.domain.Tag tag = new com.example.tsubuyaki.domain.Tag("java");
        post.getTags().add(tag);

        when(repository.findById(1L)).thenReturn(java.util.Optional.of(post));
        when(tagRepository.findById(2L)).thenReturn(java.util.Optional.of(tag));
        // 中間テーブル等で、このタグを他に使用している投稿がない状態（浮いたタグ）をモック設定
        when(repository.existsByTagsId(2L)).thenReturn(false);

        // removeTagFromPostメソッドは未実装のためコンパイルエラーREDになります。
        service.removeTagFromPost(1L, 2L);

        // 投稿からタグが削除されていることをアサート
        assertThat(post.getTags()).isEmpty();
        // 浮いたタグがDBから削除されることをアサート
        verify(tagRepository).delete(tag);
    }

    @Test
    @DisplayName("投稿削除_存在するIDのとき_deletedAtに値が設定され保存される")
    void 投稿削除_存在するIDのとき_deletedAtに値が設定され保存される() {
        Post post = new Post("user1", "削除テスト用投稿", "#000000", LocalDateTime.now());
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(post));

        // deletePost() は未定義のためコンパイルエラーREDになります。
        service.deletePost(1L);

        // getDeletedAt() も未定義のためコンパイルエラーREDになります。
        assertThat(post.getDeletedAt()).isNotNull();
        verify(repository).save(post);
    }

    @Test
    @DisplayName("投稿削除_存在しないIDのとき_例外を発生させる")
    void 投稿削除_存在しないIDのとき_例外を発生させる() {
        when(repository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.deletePost(99L))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("詳細取得_削除済みIDのとき_例外を発生させる")
    void 詳細取得_削除済みIDのとき_例外を発生させる() {
        Post post = new Post("user1", "削除済み投稿", "#000000", LocalDateTime.now());
        // delete() も未定義のためコンパイルエラーREDになります。
        post.delete();
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(post));

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("投稿復元_存在する削除済みIDのとき_deletedAtがnullになり保存される")
    void 投稿復元_存在する削除済みIDのとき_deletedAtがnullになり保存される() {
        Post post = new Post("user1", "復元テスト用投稿", "#000000", LocalDateTime.now());
        post.delete();
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(post));

        // restorePost() は未定義のためコンパイルエラーREDになります。
        service.restorePost(1L);

        assertThat(post.getDeletedAt()).isNull();
        verify(repository).save(post);
    }

    @Test
    @DisplayName("ごみ箱クリア_ごみ箱内に投稿が存在するとき_すべてにpurgedAtが設定され保存される")
    void ごみ箱クリア_ごみ箱内に投稿が存在するとき_すべてにpurgedAtが設定され保存される() {
        Post post1 = new Post("user1", "削除投稿1", "#000000", LocalDateTime.now());
        post1.delete();
        Post post2 = new Post("user2", "削除投稿2", "#3B82F6", LocalDateTime.now());
        post2.delete();

        // findByDeletedAtIsNotNullAndPurgedAtIsNull は未定義のためコンパイルエラーREDになります。
        when(repository.findByDeletedAtIsNotNullAndPurgedAtIsNull()).thenReturn(List.of(post1, post2));

        // emptyTrash() は未定義のためコンパイルエラーREDになります。
        service.emptyTrash();

        // getPurgedAt() は未定義のためコンパイルエラーREDになります。
        assertThat(post1.getPurgedAt()).isNotNull();
        assertThat(post2.getPurgedAt()).isNotNull();
        // IDがnullのエンティティはequals()が重複するため、saveの総呼び出し回数をtimes(2)で検証します
        verify(repository, Mockito.times(2)).save(Mockito.any(Post.class));
    }

    @Test
    @DisplayName("投稿編集_本人ハッシュが一致するとき_本文と色が更新されeditedAtが設定される")
    void 投稿編集_本人ハッシュが一致するとき_本文と色が更新されeditedAtが設定される() throws Exception {
        Post post = new Post("user1", "元の本文", "#000000", LocalDateTime.now());
        // 反射等を使わずにSetterまたはコンストラクタ経由でclientHashを設定（未実装のためコンパイルエラーREDになります）
        post.setClientHash("hash123");
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(post));

        com.example.tsubuyaki.web.dto.PostForm form = new com.example.tsubuyaki.web.dto.PostForm();
        form.setAuthor("user1");
        form.setBody("新しい本文");
        form.setColor("#123456");
        form.setTagsInput("");

        // updatePost() は未定義のためコンパイルエラーREDになります。
        service.updatePost(1L, form, "hash123");

        assertThat(post.getBody()).isEqualTo("新しい本文");
        assertThat(post.getColor()).isEqualTo("#123456");
        // getEditedAt() も未定義のためコンパイルエラーREDになります。
        assertThat(post.getEditedAt()).isNotNull();
        verify(repository).save(post);
    }

    @Test
    @DisplayName("投稿編集_本人ハッシュが一致しないとき_例外を発生させる")
    void 投稿編集_本人ハッシュが一致しないとき_例外を発生させる() {
        Post post = new Post("user1", "元の本文", "#000000", LocalDateTime.now());
        post.setClientHash("hash123");
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(post));

        com.example.tsubuyaki.web.dto.PostForm form = new com.example.tsubuyaki.web.dto.PostForm();
        form.setAuthor("user1");
        form.setBody("新しい本文");
        form.setColor("#123456");

        assertThatThrownBy(() -> service.updatePost(1L, form, "hash999"))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }
}
