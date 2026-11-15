
package com.authsphere.authsphere_backend.identity.systemhealth.service;

import com.authsphere.authsphere_backend.identity.systemhealth.dto.ComponentHealthResponse;
import com.authsphere.authsphere_backend.identity.systemhealth.dto.FileSystemResponse;
import com.authsphere.authsphere_backend.identity.systemhealth.dto.SystemHealthResponse;
import com.authsphere.authsphere_backend.identity.systemhealth.dto.SystemMetricsResponse;

import io.micrometer.core.instrument.MeterRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.ReadinessState;
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
@Slf4j
@RequiredArgsConstructor
public class SystemHealthService {

    private final DataSource dataSource;
    private final ApplicationAvailability applicationAvailability;
    private final MeterRegistry meterRegistry;

    // =====================================================
    // MAIN SYSTEM HEALTH
    // =====================================================

    public SystemHealthResponse getSystemHealth() {

        List<ComponentHealthResponse> components = new ArrayList<>();

        components.add(checkApplicationHealth());
        components.add(checkDatabaseHealth());

        String overallStatus = calculateOverallStatus(components);

        SystemMetricsResponse metrics = collectSystemMetrics();

        return new SystemHealthResponse(
                overallStatus,
                Instant.now(),
                components,
                metrics
        );
    }

    // =====================================================
    // APPLICATION READINESS
    // =====================================================

    private ComponentHealthResponse checkApplicationHealth() {

        long startTime = System.nanoTime();

        try {

            ReadinessState readinessState =
                    applicationAvailability.getReadinessState();

            long responseTime = TimeUnit.NANOSECONDS.toMillis(
                    System.nanoTime() - startTime
            );

            log.info(
                    "APPLICATION READINESS | State: {}",
                    readinessState
            );

            if (ReadinessState.ACCEPTING_TRAFFIC.equals(readinessState)) {

                return new ComponentHealthResponse(
                        "Application",
                        "UP",
                        "Application is accepting traffic",
                        responseTime
                );
            }

            return new ComponentHealthResponse(
                    "Application",
                    "OUT_OF_SERVICE",
                    "Application is not accepting traffic",
                    responseTime
            );

        } catch (Exception exception) {

            log.error("Application readiness check failed", exception);

            return new ComponentHealthResponse(
                    "Application",
                    "DOWN",
                    "Application readiness check failed",
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

        try (Connection connection = dataSource.getConnection()) {

            boolean connected = connection.isValid(3);

            long responseTime = TimeUnit.NANOSECONDS.toMillis(
                    System.nanoTime() - startTime
            );

            log.info(
                    "DATABASE HEALTH | Connected: {} | Response: {} ms",
                    connected,
                    responseTime
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

            long responseTime = TimeUnit.NANOSECONDS.toMillis(
                    System.nanoTime() - startTime
            );

            log.error("PostgreSQL health check failed", exception);

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

        int threadCount = threadBean.getThreadCount();

        // APPLICATION UPTIME

        RuntimeMXBean runtimeBean =
                ManagementFactory.getRuntimeMXBean();

        long uptime = runtimeBean.getUptime();

        // CPU METRICS

        double systemCpuUsage = getSystemCpuUsage();
        double processCpuUsage = getProcessCpuUsage();

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

        List<FileSystemResponse> filesystems = new ArrayList<>();

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

                long usedSpace = totalSpace - freeSpace;

                usagePercentage = Math.round(
                        (usedSpace * 100.0) / totalSpace * 100.0
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
