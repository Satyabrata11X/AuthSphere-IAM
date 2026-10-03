package com.authsphere.authsphere_backend.identity.company;

import com.authsphere.authsphere_backend.core.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "companies",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_company_name",
                        columnNames = "name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Company extends BaseEntity {

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 255)
    private String website;

    @Column(length = 255)
    private String industry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CompanyStatus status = CompanyStatus.ACTIVE;
}