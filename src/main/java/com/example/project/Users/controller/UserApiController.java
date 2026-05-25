package com.example.project.users.controller;

import com.example.project.form.exception.FormNotFoundException;
import com.example.project.form.help.entity.HelpEntity;
import com.example.project.form.help.repository.HelpRepository;
import com.example.project.form.volunteer.entity.VolunteerEntity;
import com.example.project.form.volunteer.repository.VolunteerRepository;
import com.example.project.users.entity.UserEntity;
import com.example.project.users.exception.BadRequestException;
import com.example.project.users.exception.UserAlreadyExistException;
import com.example.project.users.repository.UserRepository;
import com.example.project.users.request.EditUserRequest;
import com.example.project.users.request.ForgotPasswordRequest;
import com.example.project.users.request.RegistrationRequest;
import com.example.project.users.routes.UserRoutes;
import com.example.project.users.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;


@Controller
@RequestMapping()
@RequiredArgsConstructor
public class UserApiController {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final VolunteerRepository volunteerRepository;

    @Autowired
    private final HelpRepository helpRepository;

    @Autowired
    private final ForgotPasswordRequest forgotPasswordRequest;

    @Autowired
    private final EmailService emailService;



    @GetMapping("/not-secured/registration")
    public String registration(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            return "redirect:"+UserRoutes.ACCOUNT;
        }
        return "registration";  // Имя файла index.html, без расширения .html
    }

    @PostMapping(UserRoutes.REGISTRATION)
    public String registrationPost(@ModelAttribute RegistrationRequest request, RedirectAttributes redirectAttributes)  {
        //Проверка данных
        String errorMessage = request.validate();

        if (errorMessage != null) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/not-secured/registration";
        }

        // ===== Проверка на существующих пользователей =====
        Optional<UserEntity> existsEmail = userRepository.findByEmail(request.getEmail());
        if (existsEmail.isPresent() ) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пользователь с таким email уже существует");
            return "redirect:/not-secured/registration";
        }
        Optional<UserEntity> existsPhone = userRepository.findByPhoneNumber(request.getPhoneNumber());
        if (existsPhone.isPresent() ) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пользователь с таким номером уже существует");
            return "redirect:/not-secured/registration";
        }

        UserEntity client = UserEntity.builder()
                .lastName(request.getLastName())
                .firstName(request.getFirstName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(client);
        return "redirect:/not-secured/login";
    }

    @GetMapping("/not-secured/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            return "redirect:"+UserRoutes.ACCOUNT;
        }
        return "login";  // Имя файла index.html, без расширения .html
    }

    @GetMapping(UserRoutes.ACCOUNT)
    public String mePage(Authentication authentication, Model model) {
        String username = authentication.getName();
        UserEntity user = userRepository.findByEmail(username).orElseThrow();
        model.addAttribute("user", user);
        return "/account/account";
    }


    @GetMapping(UserRoutes.NOTME)
    public String notmePage( ) {
        return "notme";
    }

    @GetMapping(UserRoutes.FORMS)
    public String forms(Authentication authentication, Model model ) throws FormNotFoundException {
        String username = authentication.getName();
        UserEntity user = userRepository.findByEmail(username).orElseThrow();
        Optional<VolunteerEntity> volunteerForm = volunteerRepository.findByUserId(user.getId());
        Optional<HelpEntity> helpForm = helpRepository.findByUserId(user.getId());

        volunteerForm.ifPresent(volunteer -> model.addAttribute("volunteerForm", volunteer));
        helpForm.ifPresent(help -> model.addAttribute("helpForm", help));

        return "/account/accountForms";
    }

    @GetMapping(UserRoutes.EDIT)
    public String accountEdit( Authentication authentication, Model model) {
        String username = authentication.getName();
        UserEntity user = userRepository.findByEmail(username).orElseThrow();

        model.addAttribute("user", user);
        return "/account/accountEdit";
    }

    @PostMapping(UserRoutes.EDIT)
    public String account(Authentication authentication
            , @ModelAttribute EditUserRequest request
            , RedirectAttributes redirectAttributes) {
        //Проверка данных
        String errorMessage = request.validate();
        if (errorMessage != null) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:"+UserRoutes.EDIT;
        }
        String username = authentication.getName();
        UserEntity user = userRepository.findByEmail(username).orElseThrow();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());

        userRepository.save(user);
        return "redirect:"+ UserRoutes.ACCOUNT;
    }

    @GetMapping(UserRoutes.EMAIL)
    public String emailEdit() {

        return "/account/emailEdit";
    }
    @PostMapping(UserRoutes.EMAIL)
    public String email(@RequestParam String oldEmail, @RequestParam String newEmail,Authentication authentication, RedirectAttributes redirectAttributes){
        String username = authentication.getName();
        UserEntity user = userRepository.findByEmail(username).orElseThrow();

        if (!user.getEmail().equals(oldEmail)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Старая почта указана неверно");
            return "redirect:"+ UserRoutes.EMAIL;
        }

        if (userRepository.findByEmail(newEmail).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Эта почта уже используется");
            return "redirect:"+ UserRoutes.EMAIL;
        }
        user.setEmail(newEmail);
        userRepository.save(user);
        return "redirect:/not-secured/logout";
    }


    @GetMapping(UserRoutes.PASSWORD)
    public String passwordEdit() {
        return "/account/passwordEdit";
    }
    @PostMapping(UserRoutes.PASSWORD)
    public String password(@RequestParam String oldPassword, @RequestParam String newPassword,Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        UserEntity user = userRepository.findByEmail(username).orElseThrow();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Старый пароль указан неверно");
            return "redirect:"+ UserRoutes.PASSWORD;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "redirect:/not-secured/logout";
    }




    // ========= страница ввода email =========
    @GetMapping(UserRoutes.FORGOT)
    public String forgotPassword(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            return "redirect:"+UserRoutes.PASSWORD;
        }
        return "forgotPassword";
    }

    // ========= отправить код =========
    @PostMapping(UserRoutes.FORGOT)
    public String sendForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {

        Optional<UserEntity> existsEmail = userRepository.findByEmail(email);
        if (existsEmail.isEmpty() ) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пользователь с таким email не найден");
            return "redirect:"+UserRoutes.FORGOT;
        }

        forgotPasswordRequest.sendCode(email);
        redirectAttributes.addFlashAttribute("email", email);
        return "redirect:"+ UserRoutes.VERIFY;
    }

    @GetMapping(UserRoutes.VERIFY)
    public String forgotPasswordVerify(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            return "redirect:"+UserRoutes.PASSWORD;
        }
        return "forgotPasswordCode";
    }


    // ========= проверить код =========
    @PostMapping(UserRoutes.VERIFY)
    public String forgotPasswordVerify(@RequestParam String email, @RequestParam String code, RedirectAttributes redirectAttributes) {

        if (!forgotPasswordRequest.verifyCode(email, code)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Неверный код");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:"+UserRoutes.VERIFY;
        }

        redirectAttributes.addFlashAttribute("email", email);
        return "redirect:"+UserRoutes.NEWP;
    }

    // ========= страница нового пароля =========
    @GetMapping(UserRoutes.NEWP)
    public String forgotPasswordNew(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            return "redirect:"+UserRoutes.PASSWORD;
        }
        return "forgotPasswordNew";
    }

    // ========= сохранить пароль =========
    @PostMapping(UserRoutes.NEWP)
    public String forgotPasswordSave(@RequestParam String email, @RequestParam String password, RedirectAttributes redirectAttributes) {
        //Проверка данных
        String errorMessage = forgotPasswordRequest.validate(password);

        if (errorMessage != null) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:"+UserRoutes.NEWP;
        }
        UserEntity user = userRepository.findByEmail(email).orElseThrow();

        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        forgotPasswordRequest.clear(email);

        redirectAttributes.addFlashAttribute("success", "Пароль изменён");

        return "redirect:/not-secured/login";
    }


    /*
    maria.petrova@gmail.com
    9087654321
    1239874560
    qwerty123
     */


}
