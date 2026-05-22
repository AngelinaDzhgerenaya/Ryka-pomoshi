package com.example.project.form.volunteer.controller;


import com.example.project.form.exception.BadRequestException;
import com.example.project.form.help.entity.HelpEntity;
import com.example.project.form.volunteer.entity.VolunteerEntity;
import com.example.project.form.exception.FormNotFoundException;
import com.example.project.form.volunteer.repository.VolunteerRepository;
import com.example.project.form.volunteer.request.CreateVolunteerRequest;
import com.example.project.form.volunteer.request.EditVolunteerRequest;
import com.example.project.form.volunteer.routes.VolunteerRoutes;
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
public class VolunteerApiController {

    @Autowired
    private final VolunteerRepository volunteerRepository;

    @Autowired
    private final UserRepository userRepository;

    @GetMapping(VolunteerRoutes.CREATE)
    public String createForm(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            UserEntity user = userRepository.findByEmail(username).orElseThrow();
            Optional<VolunteerEntity> volunteerForm = volunteerRepository.findByUserId(user.getId());
            if (volunteerForm.isPresent()) {
                return "/form/formAlreadyExist";
            }
            return "/form/volunteerCreateForm";
        }
        return "redirect:/not-secured/login";
    }

    @PostMapping(VolunteerRoutes.CREATE)
    public String create(Authentication authentication,@ModelAttribute CreateVolunteerRequest request) throws BadRequestException {
        String username = authentication.getName();
        UserEntity user = userRepository.findByEmail(username).orElseThrow();
        request.validate();
        VolunteerEntity volunteer = request.entity();
        volunteer.setUserId(user.getId());
        volunteer.setStatus("Активно");
        volunteerRepository.save(volunteer);
        return "redirect:" + VolunteerRoutes.SUCCESSFUL;
    }

    @GetMapping(VolunteerRoutes.SUCCESSFUL)
    public String successfulCreate(Authentication authentication) {
        return "/form/successfulCreate";
    }



    @PostMapping(VolunteerRoutes.STATUS)
    public String status(Authentication authentication) throws FormNotFoundException {
        String username = authentication.getName();
        UserEntity user = userRepository.findByEmail(username).orElseThrow();
        VolunteerEntity volunteer = volunteerRepository.findByUserId(user.getId()).orElseThrow(FormNotFoundException::new);

        if (Objects.equals(volunteer.getStatus(), "Активно")) {
            volunteer.setStatus("Неактивно");
        } else {
            volunteer.setStatus("Активно");
        }

        volunteerRepository.save(volunteer);

        return "redirect:/api/v1/user/account/forms";

    }

    @GetMapping(VolunteerRoutes.EDIT)
    public String editForm(Authentication authentication, Model model) throws FormNotFoundException {
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            UserEntity user = userRepository.findByEmail(username).orElseThrow();
            Optional<VolunteerEntity> volunteerForm = volunteerRepository.findByUserId(user.getId());
            if (volunteerForm.isEmpty()) {
                return "redirect:"+ VolunteerRoutes.CREATE;
            }
            VolunteerEntity volunteer = volunteerRepository.findByUserId(user.getId()).orElseThrow(FormNotFoundException::new);//ищем по id заявку
            model.addAttribute("volunteer", volunteer);
            return "/form/volunteerEditForm";
        }
        return "redirect:/not-secured/login";
    }

    @PostMapping(VolunteerRoutes.EDIT)
    public String edit( Authentication authentication, @ModelAttribute EditVolunteerRequest request) throws FormNotFoundException {
        String username = authentication.getName();
        UserEntity user = userRepository.findByEmail(username).orElseThrow();
        VolunteerEntity volunteer = volunteerRepository.findByUserId(user.getId()).orElseThrow(FormNotFoundException::new);

        volunteer.setFullName(request.getFullName());
        volunteer.setAge(request.getAge());
        volunteer.setContactPhone(request.getContactPhone());
        volunteer.setOtherContact(request.getOtherContact());
        volunteer.setCity(request.getCity());
        volunteer.setVolunteerExperience(request.getVolunteerExperience());
        volunteer.setAvailability(request.getAvailability());
        volunteer.setPreferredGroup(request.getPreferredGroup());
        volunteer.setAvailableHelp(request.getAvailableHelp());
        volunteer.setAdditionalInformation(request.getAdditionalInformation());
        volunteer.setStatus("Активно");

        volunteerRepository.save(volunteer);
        return "redirect:" + VolunteerRoutes.SUCCESSFUL;
    }

    @GetMapping(VolunteerRoutes.BY_ID)
    public String findById(@PathVariable Long id, Model model)  {
        Optional<VolunteerEntity> volunteer = volunteerRepository.findById(id);

        if (volunteer.isEmpty()) {
            model.addAttribute("error", "Заявка не найдена");
            return "/form/volunteerPersonForm";
        }

        model.addAttribute("volunteer", volunteer.get());
        return "/form/volunteerPersonForm";
    }

    @PostMapping(VolunteerRoutes.DElETE)
    public String delete(Authentication authentication) throws FormNotFoundException {
        String username = authentication.getName();
        UserEntity user = userRepository.findByEmail(username).orElseThrow();
        VolunteerEntity volunteer = volunteerRepository.findByUserId(user.getId()).orElseThrow(FormNotFoundException::new);//ищем по id заявку
        volunteerRepository.deleteById(volunteer.getId());
        return "redirect:/api/v1/user/account/forms";

    }


    @GetMapping(VolunteerRoutes.SEARCH)
    public String search(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "") String city,
            @RequestParam(defaultValue = "") String volunteerExperience,
            @RequestParam(defaultValue = "") String availability,
            @RequestParam(defaultValue = "") String preferredGroup,
            @RequestParam(defaultValue = "") String availableHelp,
            @RequestParam(defaultValue = "") String sort,
            //@RequestParam(defaultValue = "0") int page,
            //@RequestParam(defaultValue = "10") int size,
            Model model) {

        //Pageable pageable = PageRequest.of(page, size);

        ExampleMatcher filterMatcher = ExampleMatcher.matchingAll()
                .withIgnoreNullValues()
                .withMatcher("city", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("volunteerExperience", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("availability", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("preferredGroup", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("availableHelp", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

        VolunteerEntity filterEntity = VolunteerEntity.builder()
                .city(city.isEmpty() ? null : city)
                .volunteerExperience(volunteerExperience.isEmpty() ? null : volunteerExperience)
                .availability(availability.isEmpty() ? null : availability)
                .preferredGroup(preferredGroup.isEmpty() ? null : preferredGroup)
                .availableHelp(availableHelp.isEmpty() ? null : availableHelp)
                .status("Активно")
                .build();

        Example<VolunteerEntity> example =
                Example.of(filterEntity, filterMatcher);

        List<VolunteerEntity> volunteers;

        // ✅ сортировка по выбору пользователя
        if ("created".equals(sort)) {

            volunteers = volunteerRepository.findAll(
                    example,
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );

        } else if ("updated".equals(sort)) {

            volunteers = volunteerRepository.findAll(
                    example,
                    Sort.by(Sort.Direction.DESC, "updatedAt")
            );

        } else {

            // если ничего не выбрано → без сортировки
            volunteers = volunteerRepository.findAll(example);
        }

        // 2) Если query заполнен — фильтруем дополнительно вручную
        if (!query.isEmpty()) {
            String q = query.toLowerCase();

            volunteers = volunteers.stream()
                    .filter(v ->
                            (v.getCity() != null && v.getCity().toLowerCase().contains(q)) ||
                                    (v.getVolunteerExperience() != null && v.getVolunteerExperience().toLowerCase().contains(q)) ||
                                    (v.getAvailability() != null && v.getAvailability().toLowerCase().contains(q)) ||
                                    (v.getPreferredGroup() != null && v.getPreferredGroup().toLowerCase().contains(q)) ||
                                    (v.getAvailableHelp() != null && v.getAvailableHelp().toLowerCase().contains(q))
                    )
                    .toList();
        }


        //  Передаём список в HTML
        model.addAttribute("volunteers", volunteers);

        //  Чтобы query и остальные оставались в поле поиска
        model.addAttribute("sort", sort);

        model.addAttribute("query", query);
        model.addAttribute("city", city);
        model.addAttribute("volunteerExperience", volunteerExperience);
        model.addAttribute("availability", availability);
        model.addAttribute("preferredGroup", preferredGroup);
        model.addAttribute("availableHelp", availableHelp);

        return "/form/volunteerForms";

    }
}

