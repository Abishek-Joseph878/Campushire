package com.campushire.service;

import com.campushire.model.*;
import com.campushire.repository.DataRepository;
import com.campushire.util.IdGenerator;

import java.time.LocalDate;
import java.util.List;

/**
 * Seeds the database with realistic demonstration data on first launch.
 * All data is clearly identified as demonstration data — it is not real student information.
 */
public class SampleDataSeeder {

    private final DataRepository repository;

    public SampleDataSeeder(DataRepository repository) {
        this.repository = repository;
    }

    /** Only seeds if the database is empty. */
    public void seedIfEmpty() {
        if (repository.countStudents() > 0) return;
        seedCompanies();
        seedStudents();
        seedDrives();
    }

    // =====================================================================
    // Companies
    // =====================================================================
    private void seedCompanies() {
        Company[] companies = {
            company("CMP-0001", "TechNova Solutions",   "Information Technology",  "hr@technova.example",    "Priya Sharma",   "Leading IT services and consulting firm."),
            company("CMP-0002", "FinEdge Analytics",    "Financial Technology",    "recruit@finedge.example", "Rahul Verma",   "Data-driven fintech company."),
            company("CMP-0003", "CloudBridge Systems",  "Cloud Infrastructure",    "talent@cloudbridge.example","Anita Rao",   "Cloud and DevOps solutions provider."),
            company("CMP-0004", "InnovateMed",          "Healthcare Technology",   "jobs@innovatemed.example","Dr. K. Mehta",  "AI-powered medical diagnostics."),
            company("CMP-0005", "GreenCore Energy",     "Clean Energy",            "hr@greencore.example",    "Suresh Nair",   "Renewable energy and smart grid tech.")
        };
        for (Company c : companies) repository.saveCompany(c);
    }

    private Company company(String id, String name, String industry,
                            String email, String contact, String desc) {
        Company c = new Company(id, name, industry, email);
        c.setContactPerson(contact);
        c.setDescription(desc);
        return c;
    }

    // =====================================================================
    // Students  (Demo Data — not real individuals)
    // =====================================================================
    private void seedStudents() {
        Object[][] data = {
            // id,  first,    last,        email,                       dept,      year, cgpa, bl, skills,                                    proj, intern, cert
            {"STU-0001","Aanya",   "Kapoor",   "aanya@demo.example",   "CSE",  2025, 8.7, 0, "Java,Python,SQL,Spring Boot,ReactJS",           3, 1, 2},
            {"STU-0002","Rohan",   "Mehta",    "rohan@demo.example",   "CSE",  2025, 7.9, 1, "Python,Machine Learning,TensorFlow,SQL",         2, 1, 1},
            {"STU-0003","Priya",   "Singh",    "priya@demo.example",   "ECE",  2025, 9.1, 0, "Embedded C,VHDL,Python,MATLAB",                  3, 2, 3},
            {"STU-0004","Dev",     "Patel",    "dev@demo.example",     "CSE",  2025, 6.5, 2, "Java,HTML,CSS,JavaScript",                       1, 0, 0},
            {"STU-0005","Shreya",  "Nair",     "shreya@demo.example",  "IT",   2025, 8.3, 0, "Java,Spring Boot,AWS,Docker,Kubernetes",          2, 1, 2},
            {"STU-0006","Arjun",   "Rao",      "arjun@demo.example",   "MECH", 2025, 7.4, 0, "AutoCAD,MATLAB,Python",                          2, 1, 1},
            {"STU-0007","Kavya",   "Reddy",    "kavya@demo.example",   "CSE",  2025, 9.4, 0, "Java,Python,ReactJS,SQL,Machine Learning,AWS",   4, 2, 3},
            {"STU-0008","Nikhil",  "Sharma",   "nikhil@demo.example",  "IT",   2025, 7.1, 3, "PHP,MySQL,JavaScript,HTML",                      1, 0, 0},
            {"STU-0009","Tanvi",   "Joshi",    "tanvi@demo.example",   "CSE",  2025, 8.0, 0, "Python,Django,PostgreSQL,Docker",                 2, 1, 1},
            {"STU-0010","Kiran",   "Kumar",    "kiran@demo.example",   "EEE",  2025, 7.8, 1, "Python,MATLAB,Embedded C,IoT",                   2, 1, 2},
            {"STU-0011","Ananya",  "Bose",     "ananya@demo.example",  "CSE",  2025, 6.2, 4, "Java,HTML,CSS",                                  1, 0, 0},
            {"STU-0012","Vikram",  "Gupta",    "vikram@demo.example",  "CSE",  2025, 8.9, 0, "Java,Microservices,Kafka,Spring Boot,SQL",        3, 2, 2},
            {"STU-0013","Sonal",   "Trivedi",  "sonal@demo.example",   "IT",   2025, 7.6, 0, "Python,Data Science,Pandas,NumPy,SQL",            2, 1, 2},
            {"STU-0014","Rahul",   "Pandey",   "rahul@demo.example",   "CSE",  2025, 5.8, 5, "Java,HTML",                                      0, 0, 0},
            {"STU-0015","Meera",   "Iyer",     "meera@demo.example",   "CIVIL",2025, 8.5, 0, "AutoCAD,Revit,Python",                           2, 1, 1},
            {"STU-0016","Aryan",   "Shah",     "aryan@demo.example",   "CSE",  2025, 9.0, 0, "Python,AI,Deep Learning,TensorFlow,PyTorch",     4, 2, 4},
            {"STU-0017","Pooja",   "Das",      "pooja@demo.example",   "IT",   2025, 7.3, 1, "Java,Android,Kotlin,Firebase",                   2, 1, 1},
            {"STU-0018","Siddharth","Malhotra","sid@demo.example",     "CSE",  2025, 8.4, 0, "Golang,Docker,Kubernetes,CI/CD,AWS",              3, 1, 2},
            {"STU-0019","Nisha",   "Pillai",   "nisha@demo.example",   "ECE",  2025, 6.9, 2, "Python,VHDL,Embedded C",                         1, 0, 1},
            {"STU-0020","Akash",   "Verma",    "akash@demo.example",   "CSE",  2025, 7.5, 0, "Java,ReactJS,NodeJS,MongoDB,SQL",                2, 1, 1}
        };

        for (Object[] row : data) {
            Student s = new Student(
                (String) row[0], (String) row[1], (String) row[2],
                (String) row[3], (String) row[4], (int) row[5],
                (double) row[6], (int) row[7]
            );
            s.setPhone("98" + (int)(Math.random() * 100000000 + 10000000));
            String[] skills = ((String) row[8]).split(",");
            s.setSkills(List.of(skills));
            s.setProjectCount((int) row[9]);
            s.setInternshipCount((int) row[10]);
            s.setCertificationCount((int) row[11]);
            repository.saveStudent(s);
        }
    }

    // =====================================================================
    // Recruitment Drives
    // =====================================================================
    private void seedDrives() {
        // Drive 1 – TechNova: Java Backend Engineer
        RecruitmentDrive d1 = new RecruitmentDrive("DRV-0001", "CMP-0001", "TechNova Solutions", "Java Backend Engineer");
        d1.setMinimumCgpa(7.5);
        d1.setMaximumBacklogs(1);
        d1.setEligibleDepartments(List.of("CSE", "IT"));
        d1.setRequiredSkills(List.of("Java", "Spring Boot", "SQL"));
        d1.setPreferredSkills(List.of("Microservices", "Docker", "AWS"));
        d1.setOpenPositions(5);
        d1.setDriveDate(LocalDate.now().plusDays(10));
        d1.setApplicationDeadline(LocalDate.now().plusDays(5));
        d1.setStatus(DriveStatus.ACTIVE);
        d1.setPackageLpa("12 LPA");
        d1.setWorkLocation("Bengaluru");
        repository.saveDrive(d1);

        // Drive 2 – FinEdge: Data Analyst
        RecruitmentDrive d2 = new RecruitmentDrive("DRV-0002", "CMP-0002", "FinEdge Analytics", "Data Analyst");
        d2.setMinimumCgpa(7.0);
        d2.setMaximumBacklogs(0);
        d2.setEligibleDepartments(List.of("CSE", "IT", "ECE"));
        d2.setRequiredSkills(List.of("Python", "SQL", "Data Science"));
        d2.setPreferredSkills(List.of("Machine Learning", "Pandas", "Tableau"));
        d2.setOpenPositions(3);
        d2.setDriveDate(LocalDate.now().plusDays(15));
        d2.setApplicationDeadline(LocalDate.now().plusDays(8));
        d2.setStatus(DriveStatus.ACTIVE);
        d2.setPackageLpa("10 LPA");
        d2.setWorkLocation("Hyderabad");
        repository.saveDrive(d2);

        // Drive 3 – CloudBridge: DevOps Engineer
        RecruitmentDrive d3 = new RecruitmentDrive("DRV-0003", "CMP-0003", "CloudBridge Systems", "DevOps Engineer");
        d3.setMinimumCgpa(7.5);
        d3.setMaximumBacklogs(0);
        d3.setEligibleDepartments(List.of("CSE", "IT"));
        d3.setRequiredSkills(List.of("Docker", "Kubernetes", "AWS"));
        d3.setPreferredSkills(List.of("CI/CD", "Terraform", "Python"));
        d3.setOpenPositions(4);
        d3.setDriveDate(LocalDate.now().plusDays(20));
        d3.setApplicationDeadline(LocalDate.now().plusDays(12));
        d3.setStatus(DriveStatus.ACTIVE);
        d3.setPackageLpa("14 LPA");
        d3.setWorkLocation("Pune");
        repository.saveDrive(d3);

        // Drive 4 – InnovateMed: ML Engineer (upcoming)
        RecruitmentDrive d4 = new RecruitmentDrive("DRV-0004", "CMP-0004", "InnovateMed", "ML Engineer");
        d4.setMinimumCgpa(8.0);
        d4.setMaximumBacklogs(0);
        d4.setEligibleDepartments(List.of("CSE", "IT", "ECE"));
        d4.setRequiredSkills(List.of("Python", "Machine Learning", "TensorFlow"));
        d4.setPreferredSkills(List.of("PyTorch", "Deep Learning", "SQL"));
        d4.setOpenPositions(2);
        d4.setDriveDate(LocalDate.now().plusDays(30));
        d4.setApplicationDeadline(LocalDate.now().plusDays(20));
        d4.setStatus(DriveStatus.UPCOMING);
        d4.setPackageLpa("18 LPA");
        d4.setWorkLocation("Mumbai");
        repository.saveDrive(d4);

        // Drive 5 – GreenCore: completed drive (historical data)
        RecruitmentDrive d5 = new RecruitmentDrive("DRV-0005", "CMP-0005", "GreenCore Energy", "Embedded Systems Engineer");
        d5.setMinimumCgpa(6.5);
        d5.setMaximumBacklogs(2);
        d5.setEligibleDepartments(List.of("ECE", "EEE", "CSE"));
        d5.setRequiredSkills(List.of("Embedded C", "Python", "IoT"));
        d5.setOpenPositions(3);
        d5.setDriveDate(LocalDate.now().minusDays(30));
        d5.setApplicationDeadline(LocalDate.now().minusDays(35));
        d5.setStatus(DriveStatus.COMPLETED);
        d5.setPackageLpa("9 LPA");
        d5.setWorkLocation("Chennai");
        repository.saveDrive(d5);
    }
}
