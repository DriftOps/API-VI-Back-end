package com.xertica.dto;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NewMessageRequest {
    private String message;
    private String image;
    // Novos campos
    private Double latitude;
    private Double longitude;
}