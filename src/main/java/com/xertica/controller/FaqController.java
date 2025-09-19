package com.xertica.controller;

import com.xertica.dto.*;
import com.xertica.service.FaqService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
public class FaqController {

    private final FaqService service;

    public FaqController(FaqService service) {
        this.service = service;
    }

    @PostMapping
    public FaqViewDTO create(@RequestBody @Valid FaqCreateDTO dto) {
        return service.create(dto);
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
}