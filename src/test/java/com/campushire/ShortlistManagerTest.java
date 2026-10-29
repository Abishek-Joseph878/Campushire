package com.campushire;

import com.campushire.model.*;
import com.campushire.service.ShortlistManager;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for ShortlistManager.
 * Uses an in-memory SQLite database via a custom minimal repository,
 * avoiding file-locking contention between test instances.
 */
class ShortlistManagerTest {

    private InMemoryRepo    repo;
    private ShortlistManager manager;
    private Student          student;
    private RecruitmentDrive drive;

    @BeforeEach
    void setUp() throws Exception {
        repo    = new InMemoryRepo();
        manager = new ShortlistManager(repo);

        student = new Student("STU-T1", "Test", "Student", "t@t.com", "CSE", 2025, 8.0, 0);
        student.setSkills(List.of("Java", "SQL"));
        repo.saveStudent(student);

        drive = new RecruitmentDrive("DRV-T1", "CMP-T1", "TestCo", "Java Dev");
        drive.setMinimumCgpa(7.0);
        drive.setMaximumBacklogs(1);
        drive.setEligibleDepartments(List.of("CSE"));
        repo.saveDrive(drive);
    }

    @Test
    void shortlist_createsEntry() {
        ShortlistEntry entry = manager.shortlist(student, drive, 85.0);
        assertNotNull(entry.getId());
        assertEquals(ShortlistStage.SHORTLISTED, entry.getStage());
        assertEquals(85.0, entry.getMatchScore(), 0.001);
    }

    @Test
    void duplicateShortlist_throws() {
        manager.shortlist(student, drive, 85.0);
        assertThrows(IllegalStateException.class,
            () -> manager.shortlist(student, drive, 90.0),
            "Duplicate shortlist entry should throw");
    }

    @Test
    void updateStage_changesStage() {
        ShortlistEntry entry = manager.shortlist(student, drive, 75.0);
        manager.updateStage(entry.getId(), ShortlistStage.INTERVIEW);
        ShortlistEntry updated = repo.getShortlistEntry(student.getId(), drive.getId())
            .orElseThrow();
        assertEquals(ShortlistStage.INTERVIEW, updated.getStage());
    }

    @Test
    void removeEntry_deletesFromRepo() {
        ShortlistEntry entry = manager.shortlist(student, drive, 80.0);
        manager.removeEntry(entry.getId());
        assertTrue(repo.getShortlistEntry(student.getId(), drive.getId()).isEmpty());
    }

    // -----------------------------------------------------------------------
    // Minimal in-memory repository — avoids SQLite file locking in tests
    // -----------------------------------------------------------------------
    static class InMemoryRepo implements com.campushire.repository.DataRepository {
        private final java.util.Map<String, Student>          students  = new java.util.LinkedHashMap<>();
        private final java.util.Map<String, Company>          companies = new java.util.LinkedHashMap<>();
        private final java.util.Map<String, RecruitmentDrive> drives    = new java.util.LinkedHashMap<>();
        private final java.util.Map<String, ShortlistEntry>   shortlist = new java.util.LinkedHashMap<>();

        @Override public void initialize() {}
        @Override public void shutdown()    {}

        @Override public List<Student> getAllStudents() { return List.copyOf(students.values()); }
        @Override public Optional<Student> getStudentById(String id) { return Optional.ofNullable(students.get(id)); }
        @Override public void saveStudent(Student s)  { students.put(s.getId(), s); }
        @Override public void deleteStudent(String id){ students.remove(id); }

        @Override public List<Company> getAllCompanies() { return List.copyOf(companies.values()); }
        @Override public Optional<Company> getCompanyById(String id) { return Optional.ofNullable(companies.get(id)); }
        @Override public void saveCompany(Company c)  { companies.put(c.getId(), c); }
        @Override public void deleteCompany(String id){ companies.remove(id); }

        @Override public List<RecruitmentDrive> getAllDrives() { return List.copyOf(drives.values()); }
        @Override public List<RecruitmentDrive> getDrivesByCompany(String cid) {
            return drives.values().stream().filter(d -> d.getCompanyId().equals(cid)).toList();
        }
        @Override public Optional<RecruitmentDrive> getDriveById(String id) { return Optional.ofNullable(drives.get(id)); }
        @Override public void saveDrive(RecruitmentDrive d) { drives.put(d.getId(), d); }
        @Override public void deleteDrive(String id)        { drives.remove(id); }

        @Override public List<ShortlistEntry> getAllShortlistEntries() { return List.copyOf(shortlist.values()); }
        @Override public List<ShortlistEntry> getShortlistByDrive(String driveId) {
            return shortlist.values().stream().filter(e -> e.getDriveId().equals(driveId)).toList();
        }
        @Override public List<ShortlistEntry> getShortlistByStudent(String studentId) {
            return shortlist.values().stream().filter(e -> e.getStudentId().equals(studentId)).toList();
        }
        @Override public Optional<ShortlistEntry> getShortlistEntry(String studentId, String driveId) {
            return shortlist.values().stream()
                .filter(e -> e.getStudentId().equals(studentId) && e.getDriveId().equals(driveId))
                .findFirst();
        }
        @Override public void saveShortlistEntry(ShortlistEntry e) { shortlist.put(e.getId(), e); }
        @Override public void deleteShortlistEntry(String id)      { shortlist.remove(id); }

        @Override public long countStudents()   { return students.size(); }
        @Override public long countCompanies()  { return companies.size(); }
        @Override public long countActiveDrives() {
            return drives.values().stream().filter(RecruitmentDrive::isActive).count();
        }
        @Override public long countShortlisted(){ return shortlist.size(); }
    }
}
