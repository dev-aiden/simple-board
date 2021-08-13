package com.aiden.dev.simpleboard.modules.post;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.CurrentAccount;
import com.aiden.dev.simpleboard.modules.main.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/write")
    public String writePostForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new WritePostForm());
        return "post/write";
    }

    @PostMapping("/write")
    public String writePost(@CurrentAccount Account account, @Valid WritePostForm writePostForm, Errors errors, RedirectAttributes attributes) {
        if(errors.hasErrors()) {
            return "post/write";
        }

        postService.writeNewPost(writePostForm, account);

        attributes.addFlashAttribute("alertType", "alert-info");
        attributes.addFlashAttribute("message", "게시글이 작성되었습니다.");
        return "redirect:/";
    }
}
