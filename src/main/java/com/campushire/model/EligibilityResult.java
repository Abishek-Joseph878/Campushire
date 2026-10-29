package com.campushire.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Carries the outcome of evaluating one student against one recruitment drive.
 * Separates the eligibility decision from the ranking score.
 */
public class EligibilityResult {

    private final Student student;
    private final RecruitmentDrive drive;
    private final boolean eligible;
    private final List<String> failureReasons;   // human-readable explanations
    private final List<String> matchedSkills;
    private final List<String> missingSkills;
    private double totalScore;                    // 0–100, only meaningful when eligible
    private double academicScore;
    private double skillScore;
    private double projectScore;
    private double experienceScore;

    private EligibilityResult(Builder b) {
        this.student        = b.student;
        this.drive          = b.drive;
        this.eligible       = b.eligible;
        this.failureReasons = List.copyOf(b.failureReasons);
        this.matchedSkills  = List.copyOf(b.matchedSkills);
        this.missingSkills  = List.copyOf(b.missingSkills);
        this.totalScore     = b.totalScore;
        this.academicScore  = b.academicScore;
        this.skillScore     = b.skillScore;
        this.projectScore   = b.projectScore;
        this.experienceScore = b.experienceScore;
    }

    // --- Accessors ---
    public Student getStudent()              { return student; }
    public RecruitmentDrive getDrive()       { return drive; }
    public boolean isEligible()              { return eligible; }
    public List<String> getFailureReasons()  { return failureReasons; }
    public List<String> getMatchedSkills()   { return matchedSkills; }
    public List<String> getMissingSkills()   { return missingSkills; }
    public double getTotalScore()            { return totalScore; }
    public double getAcademicScore()         { return academicScore; }
    public double getSkillScore()            { return skillScore; }
    public double getProjectScore()          { return projectScore; }
    public double getExperienceScore()       { return experienceScore; }

    public void setTotalScore(double totalScore)         { this.totalScore = totalScore; }
    public void setAcademicScore(double academicScore)   { this.academicScore = academicScore; }
    public void setSkillScore(double skillScore)         { this.skillScore = skillScore; }
    public void setProjectScore(double projectScore)     { this.projectScore = projectScore; }
    public void setExperienceScore(double v)             { this.experienceScore = v; }

    /** Formatted score breakdown for display. */
    public String getScoreBreakdown() {
        return String.format("Academic: %.0f | Skills: %.0f | Projects: %.0f | Experience: %.0f",
                academicScore, skillScore, projectScore, experienceScore);
    }

    // --- Builder ---
    public static class Builder {
        private Student student;
        private RecruitmentDrive drive;
        private boolean eligible;
        private List<String> failureReasons = new ArrayList<>();
        private List<String> matchedSkills  = new ArrayList<>();
        private List<String> missingSkills  = new ArrayList<>();
        private double totalScore;
        private double academicScore;
        private double skillScore;
        private double projectScore;
        private double experienceScore;

        public Builder(Student student, RecruitmentDrive drive, boolean eligible) {
            this.student  = student;
            this.drive    = drive;
            this.eligible = eligible;
        }

        public Builder failureReason(String reason) {
            if (reason != null && !reason.isBlank()) this.failureReasons.add(reason);
            return this;
        }

        public Builder matchedSkills(List<String> skills) {
            this.matchedSkills = skills;
            return this;
        }

        public Builder missingSkills(List<String> skills) {
            this.missingSkills = skills;
            return this;
        }

        public Builder scores(double total, double academic, double skill,
                              double project, double experience) {
            this.totalScore      = total;
            this.academicScore   = academic;
            this.skillScore      = skill;
            this.projectScore    = project;
            this.experienceScore = experience;
            return this;
        }

        public EligibilityResult build() {
            return new EligibilityResult(this);
        }
    }
}
