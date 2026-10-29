package com.campushire.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a campus recruitment drive with eligibility criteria and job details.
 */
public class RecruitmentDrive {

    private String id;
    private String companyId;
    private String companyName; // denormalized for display convenience
    private String jobRole;
    private String jobDescription;
    private double minimumCgpa;
    private int maximumBacklogs;
    private List<String> eligibleDepartments;
    private List<String> requiredSkills;
    private List<String> preferredSkills;
    private int openPositions;
    private LocalDate driveDate;
    private LocalDate applicationDeadline;
    private DriveStatus status;
    private String packageLpa; // e.g. "12 LPA"
    private String workLocation;

    public RecruitmentDrive() {
        this.eligibleDepartments = new ArrayList<>();
        this.requiredSkills = new ArrayList<>();
        this.preferredSkills = new ArrayList<>();
        this.status = DriveStatus.UPCOMING;
    }

    public RecruitmentDrive(String id, String companyId, String companyName, String jobRole) {
        this();
        this.id = id;
        this.companyId = companyId;
        this.companyName = companyName;
        this.jobRole = jobRole;
    }

    // --- Getters and Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getJobRole() { return jobRole; }
    public void setJobRole(String jobRole) { this.jobRole = jobRole; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public double getMinimumCgpa() { return minimumCgpa; }
    public void setMinimumCgpa(double minimumCgpa) { this.minimumCgpa = minimumCgpa; }

    public int getMaximumBacklogs() { return maximumBacklogs; }
    public void setMaximumBacklogs(int maximumBacklogs) { this.maximumBacklogs = maximumBacklogs; }

    public List<String> getEligibleDepartments() { return new ArrayList<>(eligibleDepartments); }
    public void setEligibleDepartments(List<String> departments) {
        this.eligibleDepartments = departments != null ? new ArrayList<>(departments) : new ArrayList<>();
    }

    public List<String> getRequiredSkills() { return new ArrayList<>(requiredSkills); }
    public void setRequiredSkills(List<String> skills) {
        this.requiredSkills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
    }

    public List<String> getPreferredSkills() { return new ArrayList<>(preferredSkills); }
    public void setPreferredSkills(List<String> skills) {
        this.preferredSkills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
    }

    public int getOpenPositions() { return openPositions; }
    public void setOpenPositions(int openPositions) { this.openPositions = openPositions; }

    public LocalDate getDriveDate() { return driveDate; }
    public void setDriveDate(LocalDate driveDate) { this.driveDate = driveDate; }

    public LocalDate getApplicationDeadline() { return applicationDeadline; }
    public void setApplicationDeadline(LocalDate applicationDeadline) { this.applicationDeadline = applicationDeadline; }

    public DriveStatus getStatus() { return status; }
    public void setStatus(DriveStatus status) { this.status = status; }

    public String getPackageLpa() { return packageLpa; }
    public void setPackageLpa(String packageLpa) { this.packageLpa = packageLpa; }

    public String getWorkLocation() { return workLocation; }
    public void setWorkLocation(String workLocation) { this.workLocation = workLocation; }

    public boolean isActive() {
        return status == DriveStatus.ACTIVE;
    }

    public String getDepartmentsDisplay() {
        return String.join(", ", eligibleDepartments);
    }

    public String getRequiredSkillsDisplay() {
        return String.join(", ", requiredSkills);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecruitmentDrive d)) return false;
        return Objects.equals(id, d.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return jobRole + " @ " + companyName + " [" + status + "]";
    }
}
