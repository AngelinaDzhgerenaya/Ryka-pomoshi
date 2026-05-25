package com.example.project.form.benefits.controller;

import com.example.project.form.benefits.entity.BenefitEntity;
import com.example.project.form.benefits.repository.BenefitRepository;
import com.example.project.form.benefits.routes.BenefitRoutes;
import com.example.project.form.exception.FormNotFoundException;
import com.example.project.form.help.entity.HelpEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping()
@RequiredArgsConstructor
public class BenefitApiController {

    @Autowired
    private final BenefitRepository benefitRepository;

    @GetMapping(BenefitRoutes.BY_ID)
    public String findById(@PathVariable Long id, Model model) throws FormNotFoundException {
        BenefitEntity benefit = benefitRepository.findById(id).orElseThrow(FormNotFoundException::new);
        model.addAttribute("benefit", benefit);
        return "benefit/benefit";
    }

    @GetMapping(BenefitRoutes.SEARCH)
    public String search(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "") String sort,
            //@RequestParam(defaultValue = "0") int page,
            //@RequestParam(defaultValue = "10") int size)
            Model model) {

        //Pageable pageable = PageRequest.of(page, size);
        ExampleMatcher exampleMatcher = ExampleMatcher.matchingAny()
                .withMatcher("title", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("description", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());


        Example<BenefitEntity> example =
                Example.of(BenefitEntity.builder().title(query).description(query).build(), exampleMatcher);

        List<BenefitEntity> benefits;

        // ✅ сортировка по выбору пользователя
        if ("created".equals(sort)) {

            benefits = benefitRepository.findAll(
                    example,
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );

        } else if ("updated".equals(sort)) {

            benefits = benefitRepository.findAll(
                    example,
                    Sort.by(Sort.Direction.DESC, "updatedAt")
            );

        } else {

            // если ничего не выбрано → без сортировки
            benefits = benefitRepository.findAll(example);
        }

        //  Передаём список в HTML
        model.addAttribute("benefits", benefits);

        //  Чтобы query и остальные оставались в поле поиска
        model.addAttribute("sort", sort);

        model.addAttribute("query", query);

        return "benefit/benefitList";


    }
}
