package com.xertica.service;

import com.xertica.dto.*;
import com.xertica.model.Faq;
import com.xertica.repository.FaqRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FaqService {
    private final FaqRepository repo;

    public FaqService(FaqRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public FaqViewDTO create(FaqCreateDTO dto) {
        Faq f = new Faq();
        f.setQuestion(dto.question());
        f.setAnswer(dto.answer());
        f.setTags(dto.tags());
        f = repo.save(f);
        return new FaqViewDTO(f.getId(), f.getQuestion(), f.getAnswer(), f.getTags());
    }

    @Transactional(readOnly = true)
    public FaqViewDTO get(Long id) {
        var f = repo.findById(id).orElseThrow();
        return new FaqViewDTO(f.getId(), f.getQuestion(), f.getAnswer(), f.getTags());
    }

    @Transactional(readOnly = true)
    public List<FaqViewDTO> search(String q, boolean fts) {
        if (fts) {
            return repo.searchFts(q).stream()
                .map(r -> new FaqViewDTO(((Number) r[0]).longValue(),
                                         (String) r[1], (String) r[2], (String) r[3]))
                .toList();
        }
        return repo.searchLike(q).stream()
            .map(f -> new FaqViewDTO(f.getId(), f.getQuestion(), f.getAnswer(), f.getTags()))
            .toList();
    }

    @Transactional
    public FaqViewDTO update(Long id, FaqCreateDTO dto) {
        var f = repo.findById(id).orElseThrow();
        f.setQuestion(dto.question());
        f.setAnswer(dto.answer());
        f.setTags(dto.tags());
        f = repo.save(f);
        return new FaqViewDTO(f.getId(), f.getQuestion(), f.getAnswer(), f.getTags());
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}