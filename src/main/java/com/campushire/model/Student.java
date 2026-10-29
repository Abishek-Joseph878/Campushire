package com.campushire.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a student candidate in the CampusHire system.
 * Extends Person with academic and professional attributes.
 */
public class Student extends Person {

    private String department;
    private int graduationYear;
    private double cgpa;
    private int activeBacklogs;
    private List<String> skills;
    private int projectCount;
    private int internshipCount;
    private int certificationCount;
    private String resumePath;

    public Student() {
        this.skills = new ArrayList<>();
    }

    public Student(String id, String firstName, String lastName, String email,
                   String department, int graduationYear, double cgpa, int activeBacklogs) {
        super(id, firstName, lastName, email);
        this.department = department;
        this.graduationYear = graduationYear;
        this.cgpa = cgpa;
        this.activeBacklogs = activeBacklogs;
        this.skills = new ArrayList<>();
        this.projectCount = 0;
        this.internshipCount = 0;
        this.certificationCount = 0;
    }

    @Override
    public String getRole() {
        return "Student";
    }

    // --- Academic score calculation ---
    /**
     * Normalizes CGPA to a 0–100 academic score.
     * Assumes CGPA is on a 10-point scale.
     */
    public double getAcademicScore() {
        return Math.min(100.0, (cgpa / 10.0) * 100.0);
    }

    /**
     * Calculates an experience score based on internships and certifications.
     * Formula: (internships * 30 + certifications * 15) capped at 100.
     */
    public double getExperienceScore() {
        double score = (internshipCount * 30.0) + (certificationCount * 15.0);
        return Math.min(100.0, score);
    }

    /**
     * Calculates a project score based on project count.
     * Formula: (projects * 25) capped at 100.
     */
    public double getProjectScore() {
        return Math.min(100.0, projectCount * 25.0);
    }

    // --- Getters and Setters ---
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getGraduationYear() { return graduationYear; }
    public void setGraduationYear(int graduationYear) { this.graduationYear = graduationYear; }

    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }

    public int getActiveBacklogs() { return activeBacklogs; }
    public void setActiveBacklogs(int activeBacklogs) { this.activeBacklogs = activeBacklogs; }

    public List<String> getSkills() { return new ArrayList<>(skills); }
    public void setSkills(List<String> skills) {
        this.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
    }

    public void addSkill(String skill) {
        if (skill != null && !skill.isBlank() && !skills.contains(skill)) {
            skills.add(skill);
        }
    }

    public int getProjectCount() { return projectCount; }
    public void setProjectCount(int projectCount) { this.projectCount = Math.max(0, projectCount); }

    public int getInternshipCount() { return internshipCount; }
    public void setInternshipCount(int internshipCount) { this.internshipCount = Math.max(0, internshipCount); }

    public int getCertificationCount() { return certificationCount; }
    public void setCertificationCount(int certificationCount) { this.certificationCount = Math.max(0, certificationCount); }

    public String getResumePath() { return resumePath; }
    public void setResumePath(String resumePath) { this.resumePath = resumePath; }

    public String getSkillsDisplay() {
        return String.join(", ", skills);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student s)) return false;
        return Objects.equals(getId(), s.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
