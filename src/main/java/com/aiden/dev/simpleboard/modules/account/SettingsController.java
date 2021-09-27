package com.aiden.dev.simpleboard.modules.account;

import com.aiden.dev.simpleboard.modules.account.form.NotificationForm;
import com.aiden.dev.simpleboard.modules.account.form.PasswordForm;
import com.aiden.dev.simpleboard.modules.account.form.ProfileForm;
import com.aiden.dev.simpleboard.modules.account.validator.PasswordFormValidator;
import com.aiden.dev.simpleboard.modules.account.validator.ProfileFormValidator;
import com.aiden.dev.simpleboard.modules.comment.CommentService;
import com.aiden.dev.simpleboard.modules.main.PostService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final PasswordFormValidator passwordFormValidator;
    private final ProfileFormValidator profileFormValidator;
    private final AccountService accountService;
    private final PostService postService;
    private final CommentService commentService;
    private final ModelMapper modelMapper;

    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(passwordFormValidator);
    }

    @InitBinder("profileForm")
    public void profileFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(profileFormValidator);
    }

    @GetMapping("/profile")
    public String updateProfileForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, ProfileForm.class));
        return "settings/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@CurrentAccount Account account, @Valid ProfileForm profileForm, Errors errors, Model model, RedirectAttributes attributes) {
        if(errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/profile";
        }

        accountService.updateProfile(account, profileForm);
        attributes.addFlashAttribute("message",  "프로필이 수정되었습니다.");
        return "redirect:/settings/profile";
    }

    @GetMapping("/password")
    public String updatePasswordForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return "settings/password";
    }

    @PostMapping("/password")
    public String updatePassword(@CurrentAccount Account account, @Valid PasswordForm passwordForm, Errors errors, Model model, RedirectAttributes attributes) {
        if(errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/password";
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "비밀번호를 변경했습니다.");
        return "redirect:/settings/password";
    }

    @GetMapping("/notification")
    public String updateNotificationForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NotificationForm.class));
        return "settings/notification";
    }

    @PostMapping("/notification")
    public String updateNotification(@CurrentAccount Account account, @Valid NotificationForm notificationForm, Errors errors, Model model, RedirectAttributes attributes) {
        if(errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/notification";
        }

        accountService.updateNotification(account, notificationForm);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:/settings/notification";
    }

    @GetMapping("/account")
    public String deleteAccountForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        return "settings/account";
    }

    @DeleteMapping("/account")
    public String deleteAccount(@CurrentAccount Account account, RedirectAttributes attributes) {
        commentService.deleteComments(account);
        postService.deletePosts(account);
        accountService.deleteAccount(account);
        SecurityContextHolder.clearContext();

        attributes.addFlashAttribute("alertType", "alert-danger");
        attributes.addFlashAttribute("message", "계정이 삭제되었습니다.");
        return "redirect:/";
    }
}
