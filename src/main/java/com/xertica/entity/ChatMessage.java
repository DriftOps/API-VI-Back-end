package com.xertica.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import com.xertica.entity.enums.UserFeedbackType;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ChatSession session;

    @Column(nullable = false, length = 10) 
    private String sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "image_data", columnDefinition = "LONGTEXT") 
    private String image;

    @Builder.Default
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();


    @Column(name = "nutritionist_comment", columnDefinition = "TEXT")
    private String nutritionistComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutritionist_id") 
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User nutritionist; 
    @Column(name = "comment_timestamp")
    private LocalDateTime commentTimestamp;

    @Column(name = "user_feedback", length = 10)
    private String userFeedback;
}