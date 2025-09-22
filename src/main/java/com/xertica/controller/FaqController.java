package com.xertica.controller;

import com.xertica.dto.*;
import com.xertica.service.FaqService;
import jakarta.validation.Valid;
import com.xertica.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
public class FaqController {

    private final FaqService service;

    public FaqController(FaqService service) {
        this.service = service;
    }

    @PostMapping
    public FaqViewDTO create(@RequestBody @Valid FaqCreateDTO dto,
            @AuthenticationPrincipal User user) {
        return service.create(dto, user);
    }

    @GetMapping("/{id}")
    public FaqViewDTO get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/search")
    public List<FaqViewDTO> search(@RequestParam String q,
            @RequestParam(defaultValue = "true") boolean fts) {
        return service.search(q, fts);
    }

    @PutMapping("/{id}")
    public FaqViewDTO update(@PathVariable Long id, @RequestBody @Valid FaqCreateDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/me")
    public List<FaqViewDTO> myFaqs(@AuthenticationPrincipal User user) {
        return service.getByUser(user);
    }
}