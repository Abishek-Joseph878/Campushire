package com.campushire.repository;

import com.campushire.model.*;
import com.campushire.util.IdGenerator;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * SQLite-backed implementation of DataRepository.
 * Uses raw JDBC with a single file-based database.
 */
public class SqliteDataRepository implements DataRepository {

    private static final String DB_FILENAME = "campushire.db";
    private Connection connection;

    /**
     * Resolves the database path to the project directory (or user.home as fallback).
     * Uses user.home so the path is absolute and consistent regardless of JVM cwd.
     */
    private static String resolveDbPath() {
        // Store the DB alongside the project jar in user.home for simplicity
        String home = System.getProperty("user.home", ".");
        return home + java.io.File.separator + DB_FILENAME;
    }

    @Override
    public void initialize() throws Exception {
        String dbPath = resolveDbPath();
        System.out.println("[CampusHire] DB path: " + dbPath);

        // SQLite URI with timeout — prevents lock errors on quick restarts
        connection = DriverManager.getConnection(
            "jdbc:sqlite:" + dbPath + "?busy_timeout=10000");
        try (Statement st = connection.createStatement()) {
            // DELETE journal avoids WAL sidecar files (.db-wal/.db-shm)
            st.execute("PRAGMA journal_mode=DELETE");
            st.execute("PRAGMA foreign_keys=ON");
            st.execute("PRAGMA busy_timeout=10000");
        }
        createTables();
    }

    @Override
    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing DB: " + e.getMessage());
        }
    }

    // =========================================================
    // DDL
    // =========================================================
    private void createTables() throws SQLException {
        String students = """
            CREATE TABLE IF NOT EXISTS students (
                id TEXT PRIMARY KEY,
                first_name TEXT NOT NULL,
                last_name TEXT NOT NULL,
                email TEXT,
                phone TEXT,
                department TEXT,
                graduation_year INTEGER,
                cgpa REAL,
                active_backlogs INTEGER DEFAULT 0,
                skills TEXT,
                project_count INTEGER DEFAULT 0,
                internship_count INTEGER DEFAULT 0,
                certification_count INTEGER DEFAULT 0,
                resume_path TEXT
            )""";

        String companies = """
            CREATE TABLE IF NOT EXISTS companies (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                industry TEXT,
                website TEXT,
                contact_email TEXT,
                contact_person TEXT,
                description TEXT
            )""";

        String drives = """
            CREATE TABLE IF NOT EXISTS drives (
                id TEXT PRIMARY KEY,
                company_id TEXT,
                company_name TEXT,
                job_role TEXT,
                job_description TEXT,
                minimum_cgpa REAL DEFAULT 0,
                maximum_backlogs INTEGER DEFAULT 0,
                eligible_departments TEXT,
                required_skills TEXT,
                preferred_skills TEXT,
                open_positions INTEGER DEFAULT 1,
                drive_date TEXT,
                application_deadline TEXT,
                status TEXT DEFAULT 'UPCOMING',
                package_lpa TEXT,
                work_location TEXT
            )""";

        String shortlist = """
            CREATE TABLE IF NOT EXISTS shortlist (
                id TEXT PRIMARY KEY,
                student_id TEXT NOT NULL,
                student_name TEXT,
                drive_id TEXT NOT NULL,
                drive_name TEXT,
                company_name TEXT,
                stage TEXT DEFAULT 'SHORTLISTED',
                match_score REAL DEFAULT 0,
                shortlisted_date TEXT,
                notes TEXT,
                UNIQUE(student_id, drive_id)
            )""";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(students);
            stmt.execute(companies);
            stmt.execute(drives);
            stmt.execute(shortlist);
        }
    }

    // =========================================================
    // Students
    // =========================================================
    @Override
    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY last_name, first_name";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapStudent(rs));
        } catch (SQLException e) {
            System.err.println("Error loading students: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<Student> getStudentById(String id) {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapStudent(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting student: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void saveStudent(Student s) {
        String sql = """
            INSERT INTO students VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            ON CONFLICT(id) DO UPDATE SET
              first_name=excluded.first_name, last_name=excluded.last_name,
              email=excluded.email, phone=excluded.phone,
              department=excluded.department, graduation_year=excluded.graduation_year,
              cgpa=excluded.cgpa, active_backlogs=excluded.active_backlogs,
              skills=excluded.skills, project_count=excluded.project_count,
              internship_count=excluded.internship_count,
              certification_count=excluded.certification_count,
              resume_path=excluded.resume_path
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, s.getId());
            ps.setString(2, s.getFirstName());
            ps.setString(3, s.getLastName());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getPhone());
            ps.setString(6, s.getDepartment());
            ps.setInt(7, s.getGraduationYear());
            ps.setDouble(8, s.getCgpa());
            ps.setInt(9, s.getActiveBacklogs());
            ps.setString(10, String.join(",", s.getSkills()));
            ps.setInt(11, s.getProjectCount());
            ps.setInt(12, s.getInternshipCount());
            ps.setInt(13, s.getCertificationCount());
            ps.setString(14, s.getResumePath());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving student: " + e.getMessage());
        }
    }

    @Override
    public void deleteStudent(String id) {
        executeDelete("students", id);
    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getString("id"));
        s.setFirstName(rs.getString("first_name"));
        s.setLastName(rs.getString("last_name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setDepartment(rs.getString("department"));
        s.setGraduationYear(rs.getInt("graduation_year"));
        s.setCgpa(rs.getDouble("cgpa"));
        s.setActiveBacklogs(rs.getInt("active_backlogs"));
        String skillsCsv = rs.getString("skills");
        if (skillsCsv != null && !skillsCsv.isBlank()) {
            s.setSkills(Arrays.asList(skillsCsv.split(",")));
        }
        s.setProjectCount(rs.getInt("project_count"));
        s.setInternshipCount(rs.getInt("internship_count"));
        s.setCertificationCount(rs.getInt("certification_count"));
        s.setResumePath(rs.getString("resume_path"));
        return s;
    }

    // =========================================================
    // Companies
    // =========================================================
    @Override
    public List<Company> getAllCompanies() {
        List<Company> list = new ArrayList<>();
        String sql = "SELECT * FROM companies ORDER BY name";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapCompany(rs));
        } catch (SQLException e) {
            System.err.println("Error loading companies: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<Company> getCompanyById(String id) {
        String sql = "SELECT * FROM companies WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapCompany(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting company: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void saveCompany(Company c) {
        String sql = """
            INSERT INTO companies VALUES (?,?,?,?,?,?,?)
            ON CONFLICT(id) DO UPDATE SET
              name=excluded.name, industry=excluded.industry,
              website=excluded.website, contact_email=excluded.contact_email,
              contact_person=excluded.contact_person, description=excluded.description
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, c.getId());
            ps.setString(2, c.getName());
            ps.setString(3, c.getIndustry());
            ps.setString(4, c.getWebsite());
            ps.setString(5, c.getContactEmail());
            ps.setString(6, c.getContactPerson());
            ps.setString(7, c.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving company: " + e.getMessage());
        }
    }

    @Override
    public void deleteCompany(String id) {
        executeDelete("companies", id);
    }

    private Company mapCompany(ResultSet rs) throws SQLException {
        Company c = new Company();
        c.setId(rs.getString("id"));
        c.setName(rs.getString("name"));
        c.setIndustry(rs.getString("industry"));
        c.setWebsite(rs.getString("website"));
        c.setContactEmail(rs.getString("contact_email"));
        c.setContactPerson(rs.getString("contact_person"));
        c.setDescription(rs.getString("description"));
        return c;
    }

    // =========================================================
    // Drives
    // =========================================================
    @Override
    public List<RecruitmentDrive> getAllDrives() {
        List<RecruitmentDrive> list = new ArrayList<>();
        String sql = "SELECT * FROM drives ORDER BY drive_date DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapDrive(rs));
        } catch (SQLException e) {
            System.err.println("Error loading drives: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<RecruitmentDrive> getDrivesByCompany(String companyId) {
        List<RecruitmentDrive> list = new ArrayList<>();
        String sql = "SELECT * FROM drives WHERE company_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapDrive(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting drives by company: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<RecruitmentDrive> getDriveById(String id) {
        String sql = "SELECT * FROM drives WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapDrive(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting drive: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void saveDrive(RecruitmentDrive d) {
        String sql = """
            INSERT INTO drives VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            ON CONFLICT(id) DO UPDATE SET
              company_id=excluded.company_id, company_name=excluded.company_name,
              job_role=excluded.job_role, job_description=excluded.job_description,
              minimum_cgpa=excluded.minimum_cgpa, maximum_backlogs=excluded.maximum_backlogs,
              eligible_departments=excluded.eligible_departments,
              required_skills=excluded.required_skills,
              preferred_skills=excluded.preferred_skills,
              open_positions=excluded.open_positions,
              drive_date=excluded.drive_date,
              application_deadline=excluded.application_deadline,
              status=excluded.status, package_lpa=excluded.package_lpa,
              work_location=excluded.work_location
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, d.getId());
            ps.setString(2, d.getCompanyId());
            ps.setString(3, d.getCompanyName());
            ps.setString(4, d.getJobRole());
            ps.setString(5, d.getJobDescription());
            ps.setDouble(6, d.getMinimumCgpa());
            ps.setInt(7, d.getMaximumBacklogs());
            ps.setString(8, String.join(",", d.getEligibleDepartments()));
            ps.setString(9, String.join(",", d.getRequiredSkills()));
            ps.setString(10, String.join(",", d.getPreferredSkills()));
            ps.setInt(11, d.getOpenPositions());
            ps.setString(12, d.getDriveDate() != null ? d.getDriveDate().toString() : null);
            ps.setString(13, d.getApplicationDeadline() != null ? d.getApplicationDeadline().toString() : null);
            ps.setString(14, d.getStatus().name());
            ps.setString(15, d.getPackageLpa());
            ps.setString(16, d.getWorkLocation());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving drive: " + e.getMessage());
        }
    }

    @Override
    public void deleteDrive(String id) {
        executeDelete("drives", id);
    }

    private RecruitmentDrive mapDrive(ResultSet rs) throws SQLException {
        RecruitmentDrive d = new RecruitmentDrive();
        d.setId(rs.getString("id"));
        d.setCompanyId(rs.getString("company_id"));
        d.setCompanyName(rs.getString("company_name"));
        d.setJobRole(rs.getString("job_role"));
        d.setJobDescription(rs.getString("job_description"));
        d.setMinimumCgpa(rs.getDouble("minimum_cgpa"));
        d.setMaximumBacklogs(rs.getInt("maximum_backlogs"));
        d.setEligibleDepartments(splitCsv(rs.getString("eligible_departments")));
        d.setRequiredSkills(splitCsv(rs.getString("required_skills")));
        d.setPreferredSkills(splitCsv(rs.getString("preferred_skills")));
        d.setOpenPositions(rs.getInt("open_positions"));
        String driveDate = rs.getString("drive_date");
        if (driveDate != null) d.setDriveDate(LocalDate.parse(driveDate));
        String deadline = rs.getString("application_deadline");
        if (deadline != null) d.setApplicationDeadline(LocalDate.parse(deadline));
        d.setStatus(DriveStatus.valueOf(rs.getString("status")));
        d.setPackageLpa(rs.getString("package_lpa"));
        d.setWorkLocation(rs.getString("work_location"));
        return d;
    }

    // =========================================================
    // Shortlist
    // =========================================================
    @Override
    public List<ShortlistEntry> getAllShortlistEntries() {
        List<ShortlistEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM shortlist ORDER BY shortlisted_date DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapShortlist(rs));
        } catch (SQLException e) {
            System.err.println("Error loading shortlist: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<ShortlistEntry> getShortlistByDrive(String driveId) {
        List<ShortlistEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM shortlist WHERE drive_id = ? ORDER BY match_score DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, driveId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapShortlist(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting shortlist by drive: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<ShortlistEntry> getShortlistByStudent(String studentId) {
        List<ShortlistEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM shortlist WHERE student_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapShortlist(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting shortlist by student: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<ShortlistEntry> getShortlistEntry(String studentId, String driveId) {
        String sql = "SELECT * FROM shortlist WHERE student_id = ? AND drive_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, driveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapShortlist(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting shortlist entry: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void saveShortlistEntry(ShortlistEntry e) {
        String sql = """
            INSERT INTO shortlist VALUES (?,?,?,?,?,?,?,?,?,?)
            ON CONFLICT(id) DO UPDATE SET
              stage=excluded.stage, match_score=excluded.match_score,
              notes=excluded.notes
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, e.getId());
            ps.setString(2, e.getStudentId());
            ps.setString(3, e.getStudentName());
            ps.setString(4, e.getDriveId());
            ps.setString(5, e.getDriveName());
            ps.setString(6, e.getCompanyName());
            ps.setString(7, e.getStage().name());
            ps.setDouble(8, e.getMatchScore());
            ps.setString(9, e.getShortlistedDate() != null ? e.getShortlistedDate().toString() : LocalDate.now().toString());
            ps.setString(10, e.getNotes());
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Error saving shortlist entry: " + ex.getMessage());
        }
    }

    @Override
    public void deleteShortlistEntry(String id) {
        executeDelete("shortlist", id);
    }

    private ShortlistEntry mapShortlist(ResultSet rs) throws SQLException {
        ShortlistEntry e = new ShortlistEntry();
        e.setId(rs.getString("id"));
        e.setStudentId(rs.getString("student_id"));
        e.setStudentName(rs.getString("student_name"));
        e.setDriveId(rs.getString("drive_id"));
        e.setDriveName(rs.getString("drive_name"));
        e.setCompanyName(rs.getString("company_name"));
        e.setStage(ShortlistStage.valueOf(rs.getString("stage")));
        e.setMatchScore(rs.getDouble("match_score"));
        String date = rs.getString("shortlisted_date");
        if (date != null) e.setShortlistedDate(LocalDate.parse(date));
        e.setNotes(rs.getString("notes"));
        return e;
    }

    // =========================================================
    // Stats
    // =========================================================
    @Override
    public long countStudents() { return countRows("students"); }

    @Override
    public long countCompanies() { return countRows("companies"); }

    @Override
    public long countActiveDrives() {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM drives WHERE status = 'ACTIVE'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override
    public long countShortlisted() { return countRows("shortlist"); }

    // =========================================================
    // Helpers
    // =========================================================
    private long countRows(String table) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM " + table);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    private void executeDelete(String table, String id) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM " + table + " WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting from " + table + ": " + e.getMessage());
        }
    }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(csv.split(",")));
    }
}
