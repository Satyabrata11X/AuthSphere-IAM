
package com.authsphere.authsphere_backend.identity.systemhealth.dto;

import java.util.List;

public record SystemMetricsResponse(

        double systemCpuUsage,

        double processCpuUsage,

        long heapUsed,

        long heapMax,

        long nonHeapUsed,

        int threadCount,

        long uptime,

        List<FileSystemResponse> filesystems

) {}
