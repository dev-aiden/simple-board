package com.aiden.dev.simpleboard.modules.main;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.CurrentAccount;
import com.aiden.dev.simpleboard.modules.post.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final PostService postService;

    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        String referrer = request.getHeader("Referer");
        request.getSession().setAttribute("prevPage", referrer);
        return "login";
    }

    @GetMapping("/")
    public String home(@RequestParam(value = "category", defaultValue = "") String category,
                       @RequestParam(value = "keyword", defaultValue = "") String keyword,
                       @RequestParam(value = "page", defaultValue = "1") int page,
                       @CurrentAccount Account account, Model model) {
        Page<Post> posts = postService.getPosts(PageRequest.of(page - 1, 10), category, keyword);
        model.addAttribute("posts", posts);
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword);

        if(account != null) {
            model.addAttribute(account);
        }

        return "index";
    }
}
