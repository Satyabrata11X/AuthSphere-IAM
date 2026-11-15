
package com.authsphere.authsphere_backend.identity.systemhealth.service;

import com.authsphere.authsphere_backend.identity.systemhealth.dto.ComponentHealthResponse;
import com.authsphere.authsphere_backend.identity.systemhealth.dto.FileSystemResponse;
import com.authsphere.authsphere_backend.identity.systemhealth.dto.SystemHealthResponse;
import com.authsphere.authsphere_backend.identity.systemhealth.dto.SystemMetricsResponse;

import io.micrometer.core.instrument.MeterRegistry;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SystemHealthService {

    private final DataSource dataSource;
    private final HealthEndpoint healthEndpoint;
    private final MeterRegistry meterRegistry;

    // =====================================================
    // MAIN SYSTEM HEALTH
    // =====================================================

    public SystemHealthResponse getSystemHealth() {

        List<ComponentHealthResponse> components =
                new ArrayList<>();

        components.add(checkApplicationHealth());
        components.add(checkDatabaseHealth());

        String overallStatus =
                calculateOverallStatus(components);

        SystemMetricsResponse metrics =
                collectSystemMetrics();

        return new SystemHealthResponse(
                overallStatus,
                Instant.now(),
                components,
                metrics
        );
    }

    // =====================================================
    // APPLICATION HEALTH - ACTUATOR
    // =====================================================

    private ComponentHealthResponse checkApplicationHealth() {

        long startTime = System.nanoTime();

        try {

            HealthComponent health =
                    healthEndpoint.health();

            long responseTime =
                    TimeUnit.NANOSECONDS.toMillis(
                            System.nanoTime() - startTime
                    );

            Status status = health.getStatus();

            String statusValue = status.getCode();

            String message;

            if (Status.UP.equals(status)) {
                message = "Application health checks passed";
            } else if (Status.DOWN.equals(status)) {
                message = "Application health check failed";
            } else if (Status.OUT_OF_SERVICE.equals(status)) {
                message = "Application is out of service";
            } else {
                message = "Application health status: " + statusValue;
            }

            return new ComponentHealthResponse(
                    "Application",
                    statusValue,
                    message,
                    responseTime
            );

        } catch (Exception exception) {

            return new ComponentHealthResponse(
                    "Application",
                    "DOWN",
                    "Application health check failed",
                    TimeUnit.NANOSECONDS.toMillis(
                            System.nanoTime() - startTime
                    )
            );
        }
    }

    // =====================================================
    // DATABASE HEALTH
    // =====================================================

    private ComponentHealthResponse checkDatabaseHealth() {

        long startTime = System.nanoTime();

        try (Connection connection =
                     dataSource.getConnection()) {

            boolean connected = connection.isValid(3);

            long responseTime =
                    TimeUnit.NANOSECONDS.toMillis(
                            System.nanoTime() - startTime
                    );

            if (connected) {

                return new ComponentHealthResponse(
                        "PostgreSQL",
                        "UP",
                        "Database connection successful",
                        responseTime
                );
            }

            return new ComponentHealthResponse(
                    "PostgreSQL",
                    "DOWN",
                    "Database connection validation failed",
                    responseTime
            );

        } catch (SQLException exception) {

            long responseTime =
                    TimeUnit.NANOSECONDS.toMillis(
                            System.nanoTime() - startTime
                    );

            return new ComponentHealthResponse(
                    "PostgreSQL",
                    "DOWN",
                    "Database connection unavailable",
                    responseTime
            );
        }
    }

    // =====================================================
    // OVERALL HEALTH STATUS
    // =====================================================

    private String calculateOverallStatus(
            List<ComponentHealthResponse> components
    ) {

        boolean hasDown = components.stream()
                .anyMatch(component ->
                        "DOWN".equalsIgnoreCase(component.status())
                                || "OUT_OF_SERVICE".equalsIgnoreCase(
                                component.status()
                        )
                );

        boolean hasDegraded = components.stream()
                .anyMatch(component ->
                        !"UP".equalsIgnoreCase(component.status())
                                && !"DOWN".equalsIgnoreCase(
                                component.status()
                        )
                                && !"OUT_OF_SERVICE".equalsIgnoreCase(
                                component.status()
                        )
                );

        if (hasDown) {
            return "DOWN";
        }

        if (hasDegraded) {
            return "DEGRADED";
        }

        return "UP";
    }

    // =====================================================
    // SYSTEM METRICS
    // =====================================================

    private SystemMetricsResponse collectSystemMetrics() {

        // JVM MEMORY

        MemoryMXBean memoryBean =
                ManagementFactory.getMemoryMXBean();

        MemoryUsage heapMemory =
                memoryBean.getHeapMemoryUsage();

        MemoryUsage nonHeapMemory =
                memoryBean.getNonHeapMemoryUsage();

        long heapUsed = heapMemory.getUsed();

        long heapMax = heapMemory.getMax();

        long nonHeapUsed = nonHeapMemory.getUsed();

        // THREAD COUNT

        ThreadMXBean threadBean =
                ManagementFactory.getThreadMXBean();

        int threadCount =
                threadBean.getThreadCount();

        // APPLICATION UPTIME

        RuntimeMXBean runtimeBean =
                ManagementFactory.getRuntimeMXBean();

        long uptime = runtimeBean.getUptime();

        // CPU METRICS

        double systemCpuUsage =
                getSystemCpuUsage();

        double processCpuUsage =
                getProcessCpuUsage();

        // FILESYSTEM METRICS

        List<FileSystemResponse> filesystems =
                collectFileSystemMetrics();

        return new SystemMetricsResponse(
                systemCpuUsage,
                processCpuUsage,
                heapUsed,
                heapMax,
                nonHeapUsed,
                threadCount,
                uptime,
                filesystems
        );
    }

    // =====================================================
    // SYSTEM CPU USAGE
    // =====================================================

    private double getSystemCpuUsage() {

        var gauge = meterRegistry
                .find("system.cpu.usage")
                .gauge();

        if (gauge == null) {
            return -1.0;
        }

        return normalizeCpuValue(gauge.value());
    }

    // =====================================================
    // PROCESS CPU USAGE
    // =====================================================

    private double getProcessCpuUsage() {

        var gauge = meterRegistry
                .find("process.cpu.usage")
                .gauge();

        if (gauge == null) {
            return -1.0;
        }

        return normalizeCpuValue(gauge.value());
    }

    // =====================================================
    // NORMALIZE CPU VALUE
    // =====================================================

    private double normalizeCpuValue(double value) {

        if (value < 0
                || Double.isNaN(value)
                || Double.isInfinite(value)) {

            return -1.0;
        }

        return Math.round(value * 10000.0) / 100.0;
    }

    // =====================================================
    // FILESYSTEM METRICS
    // =====================================================

    private List<FileSystemResponse> collectFileSystemMetrics() {

        List<FileSystemResponse> filesystems =
                new ArrayList<>();

        File[] roots = File.listRoots();

        if (roots == null) {
            return filesystems;
        }

        for (File root : roots) {

            long totalSpace = root.getTotalSpace();

            long freeSpace = root.getFreeSpace();

            long usableSpace = root.getUsableSpace();

            double usagePercentage = -1.0;

            if (totalSpace > 0) {

                long usedSpace =
                        totalSpace - freeSpace;

                usagePercentage =
                        Math.round(
                                (usedSpace * 100.0) / totalSpace
                                        * 100.0
                        ) / 100.0;
            }

            filesystems.add(
                    new FileSystemResponse(
                            root.getAbsolutePath(),
                            totalSpace,
                            usableSpace,
                            freeSpace,
                            usagePercentage
                    )
            );
        }

        return filesystems;
    }
}
