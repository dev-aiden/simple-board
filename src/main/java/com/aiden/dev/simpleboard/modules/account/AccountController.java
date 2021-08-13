package com.aiden.dev.simpleboard.modules.account;

import com.aiden.dev.simpleboard.modules.account.form.FindPasswordForm;
import com.aiden.dev.simpleboard.modules.account.form.SignUpForm;
import com.aiden.dev.simpleboard.modules.account.validator.FindPasswordFormValidator;
import com.aiden.dev.simpleboard.modules.account.validator.SignUpFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final FindPasswordFormValidator findPasswordFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @InitBinder("signUpForm")
    public void signUpFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @InitBinder("findPasswordForm")
    public void findPasswordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(findPasswordFormValidator);
    }

    @GetMapping("/sign-up")
    public String singUp(Model model) {
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        if(errors.hasErrors()) {
            return "account/sign-up";
        }

        accountService.login(accountService.processNewAccount(signUpForm));

        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);

        if(account == null) {
            model.addAttribute("error", "wrong.email");
            return "account/checked-email";
        }

        if(!account.isValidEmailCheckToken(token)) {
            model.addAttribute("error", "wrong.token");
            return "account/checked-email";
        }

        accountService.completeSignUp(account);
        return "account/checked-email";
    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentAccount Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentAccount Account account, Model model) {
        if(!account.canSendConfirmEmail()) {
            model.addAttribute("error", "인증 이메일은 1시간에 한 번만 발송이 가능합니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }

        accountService.sendSignUpConfirmEmail(account);
        return "redirect:/";
    }

    @GetMapping("/profile/id/{loginId}")
    public String viewProfileByLoginId(@PathVariable String loginId, @CurrentAccount Account account) {
        if(!loginId.equals(account.getLoginId())) {
            throw new IllegalArgumentException("잘못된 접근입니다.");
        }

        return "redirect:/profile/" + account.getNickname();
    }

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, @CurrentAccount Account account, Model model) {
        Account byNickname = accountRepository.findByNickname(nickname);
        if(byNickname == null) {
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 존재하지 않습니다.");
        }

        model.addAttribute(byNickname);
        model.addAttribute("isOwner", byNickname.equals(account));
        return "account/profile";
    }

    @GetMapping("/find-password")
    public String findPassword(Model model) {
        model.addAttribute(new FindPasswordForm());
        return "account/find-password";
    }

    @PostMapping("/find-password")
    public String findPasswordSubmit(@Valid FindPasswordForm findPasswordForm, Errors errors, RedirectAttributes attributes) {
        if(errors.hasErrors()) {
            return "account/find-password";
        }

        accountService.issueTemporaryPassword(findPasswordForm);

        attributes.addFlashAttribute("alertType", "alert-warning");
        attributes.addFlashAttribute("message", "비밀번호 찾기를 위한 메일이 발송되었습니다. 메일을 확인해 주세요.");
        return "redirect:/";
    }
}
