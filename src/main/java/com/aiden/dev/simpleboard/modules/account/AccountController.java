package com.aiden.dev.simpleboard.modules.account;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {

    @GetMapping("/sign-up")
    public String singUp(Model model) {
        return "account/sign-up";
    }
}
