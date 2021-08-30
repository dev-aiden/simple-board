package com.aiden.dev.simpleboard.modules.main;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.CurrentAccount;
import com.aiden.dev.simpleboard.modules.post.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final PostService postService;

    @GetMapping("/")
    public String home(@RequestParam(value = "page", defaultValue = "1") int page, @CurrentAccount Account account, Model model) {
        if(account != null) {
            model.addAttribute(account);
        }

        Page<Post> posts = postService.getAllPost(PageRequest.of(page - 1, 10));
        model.addAttribute("posts", posts);
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
