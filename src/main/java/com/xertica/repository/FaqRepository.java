package com.xertica.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.xertica.entity.Faq;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findByUserId(Long userId);
    
    @Query("""
        SELECT f FROM Faq f
        WHERE lower(f.question) LIKE lower(concat('%', :q, '%'))
           OR lower(f.answer)   LIKE lower(concat('%', :q, '%'))
           OR lower(coalesce(f.tags,'')) LIKE lower(concat('%', :q, '%'))
        ORDER BY f.id DESC
    """)
    List<Faq> searchLike(@Param("q") String q);

    
    @Query(value = """
        SELECT id, question, answer, tags
        FROM faqs
        WHERE tsv @@ plainto_tsquery('portuguese', :q)
        ORDER BY ts_rank(tsv, plainto_tsquery('portuguese', :q)) DESC
        LIMIT 50
    """, nativeQuery = true)
    List<Object[]> searchFts(@Param("q") String q);
}