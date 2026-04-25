package org.example.learnhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.learnhub.types.SubscriptionStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne()
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

}
