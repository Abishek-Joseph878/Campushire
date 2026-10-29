package com.campushire.model;

/**
 * Represents the pipeline stage of a shortlisted candidate.
 */
public enum ShortlistStage {
    SHORTLISTED("Shortlisted"),
    ASSESSMENT("Assessment"),
    INTERVIEW("Interview"),
    SELECTED("Selected"),
    REJECTED("Rejected");

    private final String displayName;

    ShortlistStage(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
