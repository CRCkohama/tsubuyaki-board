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
        when(repository.findTop50ByBodyContainingOrderByCreatedAtDesc("Java")).thenReturn(expected);

        List<Post> results = service.search("Java");

        assertThat(results).isEqualTo(expected);
        verify(repository).findTop50ByBodyContainingOrderByCreatedAtDesc("Java");
    }

    @Test
    @DisplayName("検索_キーワードがnullのとき_最新50件取得にフォールバックする")
    void 検索_キーワードがnullのとき_最新50件取得にフォールバックする() {
        List<Post> expected = List.of(new Post("user1", "最新投稿", LocalDateTime.now()));
        when(repository.findTop50ByOrderByCreatedAtDesc()).thenReturn(expected);

        List<Post> results = service.search(null);

        assertThat(results).isEqualTo(expected);
        verify(repository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("検索_キーワードが空文字のとき_最新50件取得にフォールバックする")
    void 検索_キーワードが空文字のとき_最新50件取得にフォールバックする() {
        List<Post> expected = List.of(new Post("user1", "最新投稿", LocalDateTime.now()));
        when(repository.findTop50ByOrderByCreatedAtDesc()).thenReturn(expected);

        List<Post> results = service.search("");

        assertThat(results).isEqualTo(expected);
        verify(repository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("検索_キーワードがスペースのみのとき_最新50件取得にフォールバックする")
    void 検索_キーワードがスペースのみのとき_最新50件取得にフォールバックする() {
        List<Post> expected = List.of(new Post("user1", "最新投稿", LocalDateTime.now()));
        when(repository.findTop50ByOrderByCreatedAtDesc()).thenReturn(expected);

        // 半角スペース、全角スペース、タブや改行などが含まれるケース
        List<Post> results = service.search(" 　\t\n ");

        assertThat(results).isEqualTo(expected);
        verify(repository).findTop50ByOrderByCreatedAtDesc();
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

        Post result = service.create(form);

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

        service.create(form);

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
}
