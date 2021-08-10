package com.aiden.dev.simpleboard.modules.account;

import com.aiden.dev.simpleboard.modules.account.form.ProfileForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    private final AccountService accountService;

    @GetMapping("/settings/profile")
    public String profileUpdateForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new ProfileForm(account));
        return "settings/profile";
    }

    @PostMapping("/settings/profile")
    public String updateProfile(@CurrentAccount Account account, @Valid ProfileForm profileForm, Errors errors, Model model, RedirectAttributes attributes) {
        if(errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/profile";
        }

        accountService.updateProfile(account, profileForm);
        attributes.addFlashAttribute("message",  "프로필이 수정되었습니다.");
        return "redirect:/settings/profile";
    }
}
