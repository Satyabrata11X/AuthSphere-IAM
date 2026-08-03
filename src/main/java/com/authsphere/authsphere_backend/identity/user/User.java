package com.authsphere.authsphere_backend.identity.user;


import com.authsphere.authsphere_backend.core.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

public class User extends BaseEntity {

    @Column(nullable = false)
    private  String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false , unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false,unique = true)
    private  String employeeId;


    private String phoneNumber;

    private String profileImageUrl;

   @Builder.Default
    private boolean emailVerified = false;

   @Builder.Default
    private boolean mfaEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.PENDING_VERIFICATION;

    private LocalDateTime lastLoginAt;

}
