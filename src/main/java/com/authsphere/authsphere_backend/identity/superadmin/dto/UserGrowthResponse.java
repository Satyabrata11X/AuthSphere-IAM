
package com.authsphere.authsphere_backend.identity.superadmin.dto;

import java.time.LocalDate;

public record UserGrowthResponse(

        LocalDate date,

        long registrations

) {
}
