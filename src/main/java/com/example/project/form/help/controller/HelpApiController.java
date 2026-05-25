package com.example.project.form.help.controller;

import com.example.project.form.exception.BadRequestException;
import com.example.project.form.exception.FormNotFoundException;
import com.example.project.form.help.entity.HelpEntity;
import com.example.project.form.help.repository.HelpRepository;
import com.example.project.form.help.request.CreateHelpRequest;
import com.example.project.form.help.request.EditHelpRequest;
import com.example.project.form.help.routes.HelpRoutes;
import com.example.project.users.entity.UserEntity;
import com.example.project.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Controller
@RequestMapping()
@RequiredArgsConstructor
public class HelpApiController {

    private final HelpRepository helpRepository;

    private final UserRepository userRepository;

    @PostMapping(HelpRoutes.CREATE)
    public String create(Authentication authentication,
                         @ModelAttribute CreateHelpRequest request)
            throws BadRequestException {
        String username = authentication.getName();
        UserEntity user = userRepository.findByEmail(username).orElseThrow();
        request.validate();
        HelpEntity help = request.entity();
        help.setUserId(user.getId());
        help.setStatus("Активно");
        helpRepository.save(help);
        return "redirect:" + HelpRoutes.SUCCESSFUL;
    }
    @GetMapping(HelpRoutes.SUCCESSFUL)
    public String successfulCreate() {
        return "/form/successfulCreate";
    }

    @GetMapping(HelpRoutes.CREATE)
    public String createForm(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            UserEntity user = userRepository.findByEmail(username).orElseThrow();
            Optional<HelpEntity> helpForm = helpRepository.findByUserId(user.getId());
            if (helpForm.isPresent()) {
                return "form/formAlreadyExist";
            }

            return "form/helpCreateForm";
        }
        return "redirect:/not-secured/login";
    }
    @PostMapping(HelpRoutes.STATUS)
    public String status(Authentication authentication) throws FormNotFoundException {
        String username = authentication.getName();
        UserEntity user = userRepository.findByEmail(username).orElseThrow();
        HelpEntity help = helpRepository.findByUserId(user.getId()).orElseThrow(FormNotFoundException::new);

        if (Objects.equals(help.getStatus(), "Активно")) {
            help.setStatus("Неактивно");
        } else {
            help.setStatus("Активно");
        }

        helpRepository.save(help);

        return "redirect:/api/v1/user/account/forms";

    }
    @GetMapping(HelpRoutes.EDIT)
    public String editForm(Authentication authentication, Model model) throws FormNotFoundException {
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            UserEntity user = userRepository.findByEmail(username).orElseThrow();
            Optional<HelpEntity> helpForm = helpRepository.findByUserId(user.getId());
            if (helpForm.isEmpty()) {
                return "redirect:"+ HelpRoutes.CREATE;
            }
            HelpEntity help = helpRepository.findByUserId(user.getId()).orElseThrow(FormNotFoundException::new);//ищем по id заявку
            model.addAttribute("help", help);
            return "form/helpEditForm";
        }
        return "redirect:/not-secured/login";
    }

    @PostMapping(HelpRoutes.EDIT)
    public String edit(Authentication authentication,@ModelAttribute EditHelpRequest request) throws FormNotFoundException {
        String username = authentication.getName();
        UserEntity user = userRepository.findByEmail(username).orElseThrow();
        HelpEntity help = helpRepository.findByUserId(user.getId()).orElseThrow(FormNotFoundException::new);

        help.setFullName(request.getFullName());
        help.setAge(request.getAge());
        help.setContactPhone(request.getContactPhone());
        help.setOtherContact(request.getOtherContact());
        help.setCity(request.getCity());
        help.setAvailability(request.getAvailability());
        help.setHelpGroup(request.getHelpGroup());
        help.setPersonCondition(request.getPersonCondition());
        help.setHelpNeeded(request.getHelpNeeded());
        help.setAdditionalInformation(request.getAdditionalInformation());
        help.setStatus("Активно");


        helpRepository.save(help);
        return "redirect:" + HelpRoutes.SUCCESSFUL;
    }
    @GetMapping(HelpRoutes.BY_ID)
    public String findById(@PathVariable Long id, Model model)  {
        Optional<HelpEntity> help = helpRepository.findById(id);

        if (help.isEmpty()) {
            model.addAttribute("error", "Заявка не найдена");
            return "form/helpPersonForm";
        }

        model.addAttribute("help", help.get());
        return "form/helpPersonForm";
    }

    @PostMapping(HelpRoutes.DElETE)
    public String delete(Authentication authentication) throws FormNotFoundException {
        String username = authentication.getName();
        UserEntity user = userRepository.findByEmail(username).orElseThrow();
        HelpEntity help = helpRepository.findByUserId(user.getId()).orElseThrow(FormNotFoundException::new);//ищем по id заявку
        helpRepository.deleteById(help.getId());
        return "redirect:/api/v1/user/account/forms";


    }

    @GetMapping(HelpRoutes.SEARCH)
    public String search(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "") String city,
            @RequestParam(defaultValue = "") String personCondition,
            @RequestParam(defaultValue = "") String availability,
            @RequestParam(defaultValue = "") String helpGroup,
            @RequestParam(defaultValue = "") String helpNeeded,
            @RequestParam(defaultValue = "") String sort,
            //@RequestParam(defaultValue = "0") int page,
            //@RequestParam(defaultValue = "10") int size)
            Model model) {

        //Pageable pageable = PageRequest.of(page, size);
        ExampleMatcher exampleMatcher = ExampleMatcher.matchingAll()
                .withMatcher("city", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("personCondition", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("availability", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("helpGroup", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("helpNeeded", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

        HelpEntity helpEntity = HelpEntity.builder()
                .city(city.isEmpty() ? null : city) // если не передан город, не заполняем поле
                .personCondition(personCondition.isEmpty() ? null : personCondition)
                .availability(availability.isEmpty() ? null : availability)
                .helpGroup(helpGroup.isEmpty() ? null : helpGroup)
                .helpNeeded(helpNeeded.isEmpty() ? null : helpNeeded)
                .status("Активно")
                .build();

        Example<HelpEntity> example =
                Example.of(helpEntity, exampleMatcher);

        List<HelpEntity> helps;

        //  сортировка по выбору пользователя
        if ("created".equals(sort)) {

            helps = helpRepository.findAll(
                    example,
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );

        } else if ("updated".equals(sort)) {

            helps = helpRepository.findAll(
                    example,
                    Sort.by(Sort.Direction.DESC, "updatedAt")
            );

        } else {

            // если ничего не выбрано → без сортировки
            helps = helpRepository.findAll(example);
        }

        // 2) Если query заполнен — фильтруем дополнительно вручную
        if (!query.isEmpty()) {
            String q = query.toLowerCase();

            helps = helps.stream()
                    .filter(v ->
                            (v.getCity() != null && v.getCity().toLowerCase().contains(q)) ||
                                    (v.getPersonCondition() != null && v.getPersonCondition().toLowerCase().contains(q)) ||
                                    (v.getAvailability() != null && v.getAvailability().toLowerCase().contains(q)) ||
                                    (v.getHelpGroup() != null && v.getHelpGroup().toLowerCase().contains(q)) ||
                                    (v.getHelpNeeded() != null && v.getHelpNeeded().toLowerCase().contains(q))
                    )
                    .toList();
        }

        //  Передаём список в HTML
        model.addAttribute("helps", helps);

        //  Чтобы query и остальные оставались в поле поиска
        model.addAttribute("sort", sort);

        model.addAttribute("query", query);
        model.addAttribute("city", city);
        model.addAttribute("personCondition", personCondition);
        model.addAttribute("availability", availability);
        model.addAttribute("helpGroup", helpGroup);
        model.addAttribute("helpNeeded", helpNeeded);

        return "form/helpForms";


    }
    }

