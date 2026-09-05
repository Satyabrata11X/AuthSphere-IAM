package com.authsphere.authsphere_backend.identity.mfa;

import com.authsphere.authsphere_backend.core.common.BaseEntity;
import com.authsphere.authsphere_backend.identity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "mfa_configurations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MfaConfiguration extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    @Column(nullable = false)
    private String secret;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = false;
}