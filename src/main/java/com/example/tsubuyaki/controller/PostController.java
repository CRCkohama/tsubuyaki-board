package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.ClientHashGenerator;
import com.example.tsubuyaki.service.PostLikeService;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.tsubuyaki.repository.PostRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class PostController {

    private final PostService postService;
    private final PostRepository postRepository;
    private final PostLikeService postLikeService;
    private final ClientHashGenerator clientHashGenerator;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public PostController(PostService postService, PostRepository postRepository,
            PostLikeService postLikeService, ClientHashGenerator clientHashGenerator) {
        this.postService = postService;
        this.postRepository = postRepository;
        this.postLikeService = postLikeService;
        this.clientHashGenerator = clientHashGenerator;
    }

    @GetMapping({ "/", "/posts" })
    public String list(@RequestParam(value = "q", required = false) String query, Model model) {
        // キーワード検索、または全件新着順表示（フォールバック）をServiceへ委譲して結果を取得します。
        List<Post> posts = postService.search(query);
        model.addAttribute("posts", posts);

        // 各投稿のいいね総数を集計し、投稿IDをキーとしたマップをModelへ格納します。
        Map<Long, Long> likeCounts = posts.stream().collect(Collectors.toMap(
                Post::getId,
                post -> postLikeService.countByPostId(post.getId())
        ));
        model.addAttribute("likeCounts", likeCounts);

        // 入力した検索キーワードを画面の検索ボックスに再表示（リテイン）するためにModelへ格納します。
        model.addAttribute("q", query);
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        // パス変数のidに対応する投稿を取得し、詳細テンプレートで参照できるようpost属性へ格納する。
        model.addAttribute("post", postService.findById(id));
        // 詳細画面で現在のいいね総数を表示できるよう、投稿idに紐づく件数をModelへ格納する。
        model.addAttribute("likeCount", postLikeService.countByPostId(id));
        return "posts/detail";
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        // 接続元IPアドレスとUser-AgentからclientHashを作り、同一クライアントのトグル判定に使う。
        String clientHash = clientHashGenerator.generate(request.getRemoteAddr(), request.getHeader("User-Agent"));
        // いいねが未登録なら保存し、登録済みなら解除するトグル処理をServiceへ委譲する。
        postLikeService.toggle(id, clientHash);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute("postForm") PostForm postForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }

        postService.create(postForm);
        return "redirect:/posts";
    }

    /**
     * 指定された投稿を論理削除します。
     * 削除処理完了後はタイムライン（/posts）へリダイレクトします。
     */
    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id) {
        postService.deletePost(id);
        return "redirect:/posts";
    }

    /**
     * ごみ箱（削除一覧）画面を表示します。
     */
    @GetMapping("/posts/trash")
    public String trashList(Model model) {
        List<Post> posts = postRepository.findByDeletedAtIsNotNullAndPurgedAtIsNullOrderByDeletedAtDesc();
        model.addAttribute("posts", posts);

        // 各投稿のいいね数を集計してModelへ格納します。
        Map<Long, Long> likeCounts = posts.stream().collect(Collectors.toMap(
                Post::getId,
                post -> postLikeService.countByPostId(post.getId())
        ));
        model.addAttribute("likeCounts", likeCounts);

        return "posts/trash";
    }

    /**
     * 削除された投稿をごみ箱から元に戻します。
     */
    @PostMapping("/posts/{id}/restore")
    public String restore(@PathVariable Long id) {
        postService.restorePost(id);
        return "redirect:/posts/trash";
    }

    /**
     * ごみ箱を空にします。
     */
    @PostMapping("/posts/trash/empty")
    public String emptyTrash() {
        postService.emptyTrash();
        return "redirect:/posts/trash";
    }
}
