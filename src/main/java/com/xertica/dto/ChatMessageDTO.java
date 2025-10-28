package com.xertica.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class ChatMessageDTO {

    private Long id;
    private Long sessionId;
    private Long userId; 
    private String sender; 
    private String message;
    private LocalDateTime timestamp; 
    private String nutritionistComment; 

    
    private Long nutritionistId;
    private LocalDateTime commentTimestamp;

    private String userFeedback;
}