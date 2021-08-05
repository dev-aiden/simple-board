package com.aiden.dev.simpleboard.modules.account;

import com.aiden.dev.simpleboard.modules.account.form.SignUpForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {

    @GetMapping("/sign-up")
    public String singUp(Model model) {
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }
}
