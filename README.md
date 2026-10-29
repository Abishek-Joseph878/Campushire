# CampusHire
### Campus Recruitment & Talent Discovery Platform
> College OOP Microproject — Java 21 · JavaFX 21 · SQLite · Gradle

---

## Quick Start

Double-click or run from the project root:

```bat
run.bat          ← Launch the application
compile.bat      ← Compile only
test.bat         ← Run unit tests
```

**Prerequisites:**
- Java 21 is provided by Android Studio's bundled JBR
  (`C:\Program Files\Android\Android Studio\jbr`)
- Internet access is required on the first run to download Gradle dependencies (~25 MB)
- After first run, all dependencies are cached and offline use works

---

## Architecture

```
src/main/java/com/campushire/
├── app/          CampusHireApp (entry point), MainLayout (root window + sidebar)
├── model/        Person, Student, Company, RecruitmentDrive, ShortlistEntry, enums
├── service/      EligibilityEngine, SkillMatcher, RankingEngine, ShortlistManager,
│                 AnalyticsService, SampleDataSeeder
├── repository/   DataRepository (interface), SqliteDataRepository
├── controller/   DashboardController, CandidatesController, CompaniesController,
│                 DrivesController, MatchingController, ShortlistController, InsightsController
└── util/         IdGenerator, ValidationUtil, CsvExporter
```

## OOP Concepts Demonstrated
| Concept          | Where |
|-----------------|-------|
| Abstraction     | `Person` (abstract class), `DataRepository` (interface), `RankingStrategy` (interface) |
| Inheritance     | `Student extends Person` |
| Polymorphism    | `WeightedRankingStrategy implements RankingStrategy` — swappable ranking |
| Encapsulation   | All model classes with private fields and getters/setters |
| Collections     | Lists, Maps, Sets throughout services and controllers |
| Exception handling | Duplicate shortlist, input validation, DB error handling |
| Builder pattern | `EligibilityResult.Builder` |
| Record types    | `SkillMatcher.SkillMatchResult`, `AnalyticsService.DashboardStats` |

## Screens
- **Dashboard** — KPI cards + charts (CGPA distribution, pipeline funnel, dept breakdown)
- **Candidates** — Searchable/filterable table, add/edit/delete with validation
- **Companies** — CRUD + view associated drives
- **Drives** — Create/edit drives with full eligibility criteria configuration
- **Talent Matching** — Evaluate all students against a drive, ranked scores, shortlist action
- **Shortlisting** — Pipeline stage management, CSV export
- **Insights** — Per-student opportunity analysis with what-if CGPA/skill simulator

## Scoring Formula
| Component       | Weight |
|----------------|--------|
| Academic (CGPA) | 30%   |
| Skill match     | 40%   |
| Projects        | 15%   |
| Experience      | 15%   |

All scores normalised to 0–100. Final score guaranteed within [0, 100].

## Data
- `campushire.db` — SQLite database created automatically on first launch
- 20 demo students, 5 companies, 5 recruitment drives seeded on first run
- All demo data is clearly labelled as demonstration data

## Tests
28 unit + integration tests covering:
- CGPA boundary conditions (exact minimum, below, above)
- Backlog boundary conditions
- Department eligibility (match, mismatch, empty = all)
- Skill matching (perfect, partial, case-insensitive, empty)
- Ranking score boundaries (0–100)
- Ranking order (eligible before ineligible, higher score first)
- Duplicate shortlist prevention
- Stage update and entry removal
- CSV export (valid output, header, comma escaping)
