package com.campushire.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a candidate's entry in the shortlisting pipeline for a specific drive.
 * The compound key (studentId + driveId) must be unique.
 */
public class ShortlistEntry {

    private String id;
    private String studentId;
    private String studentName;   // denormalized for display
    private String driveId;
    private String driveName;     // denormalized: "Role @ Company"
    private String companyName;
    private ShortlistStage stage;
    private double matchScore;
    private LocalDate shortlistedDate;
    private String notes;

    public ShortlistEntry() {
        this.stage = ShortlistStage.SHORTLISTED;
        this.shortlistedDate = LocalDate.now();
    }

    public ShortlistEntry(String id, String studentId, String studentName,
                          String driveId, String driveName, String companyName,
                          double matchScore) {
        this();
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.driveId = driveId;
        this.driveName = driveName;
        this.companyName = companyName;
        this.matchScore = matchScore;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getDriveId() { return driveId; }
    public void setDriveId(String driveId) { this.driveId = driveId; }

    public String getDriveName() { return driveName; }
    public void setDriveName(String driveName) { this.driveName = driveName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public ShortlistStage getStage() { return stage; }
    public void setStage(ShortlistStage stage) { this.stage = stage; }

    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }

    public LocalDate getShortlistedDate() { return shortlistedDate; }
    public void setShortlistedDate(LocalDate shortlistedDate) { this.shortlistedDate = shortlistedDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    /** Compound key for duplicate detection. */
    public String getCompoundKey() {
        return studentId + "::" + driveId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShortlistEntry e)) return false;
        return Objects.equals(id, e.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
