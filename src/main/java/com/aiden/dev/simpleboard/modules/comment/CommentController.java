package com.aiden.dev.simpleboard.modules.comment;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.CurrentAccount;
import com.aiden.dev.simpleboard.modules.comment.form.WriteCommentForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/write")
    public String writeComment(@CurrentAccount Account account, @Valid WriteCommentForm writeCommentForm, Errors errors) {
        if(errors.hasErrors()) {
            return "redirect:/post/" + writeCommentForm.getPostId();
        }

        if(!account.isEmailVerified()) {
            throw new IllegalArgumentException("잘못된 접근입니다.");
        }

        commentService.writeNewComment(writeCommentForm, account);
        return "redirect:/post/" + writeCommentForm.getPostId();
    }
}
