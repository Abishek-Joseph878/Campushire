package com.campushire.service;

import com.campushire.model.*;
import com.campushire.repository.DataRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Computes dashboard statistics and chart data from live repository state.
 */
public class AnalyticsService {

    private final DataRepository repository;
    private final EligibilityEngine eligibilityEngine;

    public AnalyticsService(DataRepository repository) {
        this.repository        = repository;
        this.eligibilityEngine = new EligibilityEngine();
    }

    public record DashboardStats(
        long totalStudents,
        long totalCompanies,
        long activeDrives,
        long totalShortlisted,
        long eligibleCandidates
    ) {}

    public DashboardStats getDashboardStats() {
        long students   = repository.countStudents();
        long companies  = repository.countCompanies();
        long drives     = repository.countActiveDrives();
        long shortlisted = repository.countShortlisted();
        long eligible   = countEligibleAcrossAllActiveDrives();
        return new DashboardStats(students, companies, drives, shortlisted, eligible);
    }

    /** Counts unique students eligible for at least one active drive. */
    private long countEligibleAcrossAllActiveDrives() {
        List<RecruitmentDrive> activeDrives = repository.getAllDrives().stream()
            .filter(RecruitmentDrive::isActive)
            .toList();
        if (activeDrives.isEmpty()) return 0;

        List<Student> students = repository.getAllStudents();
        Set<String> eligibleIds = new HashSet<>();
        for (RecruitmentDrive drive : activeDrives) {
            for (Student s : students) {
                if (eligibilityEngine.evaluate(s, drive).isEligible()) {
                    eligibleIds.add(s.getId());
                }
            }
        }
        return eligibleIds.size();
    }

    /** Returns department → student count for the bar chart. */
    public Map<String, Long> getStudentsByDepartment() {
        return repository.getAllStudents().stream()
            .filter(s -> s.getDepartment() != null)
            .collect(Collectors.groupingBy(Student::getDepartment, Collectors.counting()));
    }

    /** Returns stage → count for the pipeline funnel chart. */
    public Map<ShortlistStage, Long> getShortlistByStage() {
        return repository.getAllShortlistEntries().stream()
            .collect(Collectors.groupingBy(ShortlistEntry::getStage, Collectors.counting()));
    }

    /** Returns drive status → count. */
    public Map<DriveStatus, Long> getDrivesByStatus() {
        return repository.getAllDrives().stream()
            .collect(Collectors.groupingBy(RecruitmentDrive::getStatus, Collectors.counting()));
    }

    /** Returns CGPA distribution buckets for histogram. */
    public Map<String, Long> getCgpaDistribution() {
        Map<String, Long> dist = new LinkedHashMap<>();
        dist.put("< 6.0", 0L);
        dist.put("6.0–6.9", 0L);
        dist.put("7.0–7.9", 0L);
        dist.put("8.0–8.9", 0L);
        dist.put("9.0–10.0", 0L);
        for (Student s : repository.getAllStudents()) {
            double c = s.getCgpa();
            if      (c < 6.0)  dist.merge("< 6.0",     1L, Long::sum);
            else if (c < 7.0)  dist.merge("6.0–6.9",   1L, Long::sum);
            else if (c < 8.0)  dist.merge("7.0–7.9",   1L, Long::sum);
            else if (c < 9.0)  dist.merge("8.0–8.9",   1L, Long::sum);
            else               dist.merge("9.0–10.0",  1L, Long::sum);
        }
        return dist;
    }
}
