package com.campushire.service;

import com.campushire.model.*;
import com.campushire.repository.DataRepository;
import com.campushire.util.IdGenerator;

import java.util.List;
import java.util.Optional;

/**
 * Manages the shortlisting pipeline.
 * Enforces the duplicate-entry constraint (one entry per student+drive pair).
 */
public class ShortlistManager {

    private final DataRepository repository;

    public ShortlistManager(DataRepository repository) {
        this.repository = repository;
    }

    /**
     * Shortlists a candidate for a drive.
     * Throws IllegalStateException if already shortlisted.
     */
    public ShortlistEntry shortlist(Student student, RecruitmentDrive drive, double matchScore) {
        // Duplicate check
        Optional<ShortlistEntry> existing =
            repository.getShortlistEntry(student.getId(), drive.getId());
        if (existing.isPresent()) {
            throw new IllegalStateException(
                student.getFullName() + " is already shortlisted for " + drive.getJobRole());
        }

        ShortlistEntry entry = new ShortlistEntry(
            IdGenerator.newShortlistId(),
            student.getId(),
            student.getFullName(),
            drive.getId(),
            drive.getJobRole() + " @ " + drive.getCompanyName(),
            drive.getCompanyName(),
            matchScore
        );
        repository.saveShortlistEntry(entry);
        return entry;
    }

    /**
     * Advances or changes the stage of an existing shortlist entry.
     */
    public void updateStage(String entryId, ShortlistStage newStage) {
        List<ShortlistEntry> all = repository.getAllShortlistEntries();
        all.stream()
           .filter(e -> e.getId().equals(entryId))
           .findFirst()
           .ifPresent(e -> {
               e.setStage(newStage);
               repository.saveShortlistEntry(e);
           });
    }

    public List<ShortlistEntry> getEntriesForDrive(String driveId) {
        return repository.getShortlistByDrive(driveId);
    }

    public List<ShortlistEntry> getAllEntries() {
        return repository.getAllShortlistEntries();
    }

    public void removeEntry(String entryId) {
        repository.deleteShortlistEntry(entryId);
    }
}
