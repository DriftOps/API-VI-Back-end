package com.xertica.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_restrictions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRestriction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "restriction_id")
    private DietaryRestriction restriction;

    public UserRestriction(User user, DietaryRestriction restriction) {
        this.user = user;
        this.restriction = restriction;
    }
}