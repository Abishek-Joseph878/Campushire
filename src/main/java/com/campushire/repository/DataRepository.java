package com.campushire.repository;

import com.campushire.model.*;
import java.util.List;
import java.util.Optional;

/**
 * Abstraction over the persistence layer.
 * Swap implementations without touching business logic.
 */
public interface DataRepository {

    // --- Lifecycle ---
    void initialize() throws Exception;
    void shutdown();

    // --- Students ---
    List<Student>  getAllStudents();
    Optional<Student> getStudentById(String id);
    void saveStudent(Student student);
    void deleteStudent(String id);

    // --- Companies ---
    List<Company>  getAllCompanies();
    Optional<Company> getCompanyById(String id);
    void saveCompany(Company company);
    void deleteCompany(String id);

    // --- Recruitment Drives ---
    List<RecruitmentDrive> getAllDrives();
    List<RecruitmentDrive> getDrivesByCompany(String companyId);
    Optional<RecruitmentDrive> getDriveById(String id);
    void saveDrive(RecruitmentDrive drive);
    void deleteDrive(String id);

    // --- Shortlist ---
    List<ShortlistEntry> getAllShortlistEntries();
    List<ShortlistEntry> getShortlistByDrive(String driveId);
    List<ShortlistEntry> getShortlistByStudent(String studentId);
    Optional<ShortlistEntry> getShortlistEntry(String studentId, String driveId);
    void saveShortlistEntry(ShortlistEntry entry);
    void deleteShortlistEntry(String id);

    // --- Stats helpers ---
    long countStudents();
    long countCompanies();
    long countActiveDrives();
    long countShortlisted();
}
