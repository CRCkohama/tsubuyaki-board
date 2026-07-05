package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
import com.example.tsubuyaki.service.PostLikeService;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.service.TagNotFoundException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class TagController {

    private final PostService postService;
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostLikeService postLikeService;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public TagController(PostService postService, PostRepository postRepository,
                         TagRepository tagRepository, PostLikeService postLikeService) {
        this.postService = postService;
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.postLikeService = postLikeService;
    }

    /**
     * 指定されたタグ名に紐付いている投稿一覧を新着順で表示します。
     * タグが存在しない場合は404エラーを返します。
     */
    @GetMapping("/tags/{name}")
    public String listByTag(@PathVariable String name, Model model) {
        Tag tag = tagRepository.findByName(name).orElseThrow(TagNotFoundException::new);
        
        List<Post> posts = postRepository.findByTagsNameAndDeletedAtIsNullAndPurgedAtIsNullOrderByCreatedAtDesc(tag.getName());
        model.addAttribute("posts", posts);
        model.addAttribute("tagName", tag.getName());

        // 一覧表示されている各投稿のいいね数を集計してModelへ格納します。
        Map<Long, Long> likeCounts = posts.stream().collect(Collectors.toMap(
                Post::getId,
                post -> postLikeService.countByPostId(post.getId())
        ));
        model.addAttribute("likeCounts", likeCounts);

        return "tags/list";
    }

    /**
     * 投稿から特定のタグの関連付けを解除（削除）します。
     * 処理後は、元の投稿詳細画面へリダイレクトします。
     */
    @PostMapping("/posts/{postId}/tags/{tagId}/delete")
    public String removeTag(@PathVariable Long postId, @PathVariable Long tagId) {
        postService.removeTagFromPost(postId, tagId);
        return "redirect:/posts/" + postId;
    }
}
