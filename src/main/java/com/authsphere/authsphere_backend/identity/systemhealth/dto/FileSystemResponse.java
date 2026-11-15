
package com.authsphere.authsphere_backend.identity.systemhealth.dto;

public record FileSystemResponse(

        String path,

        long totalSpace,

        long usableSpace,

        long freeSpace,

        double usagePercentage

) {}
