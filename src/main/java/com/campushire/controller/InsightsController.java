package com.campushire.controller;

import com.campushire.model.*;
import com.campushire.repository.DataRepository;
import com.campushire.service.RankingEngine;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Student opportunity insights: select a student and see which drives they're
 * eligible for, with scores and skill gaps. Includes a simple what-if CGPA simulator.
 */
public class InsightsController {

    private final DataRepository  repository;
    private final RankingEngine   rankingEngine;
    private final VBox            view;

    private ComboBox<Student> studentPicker;
    private TableView<EligibilityResult> resultsTable;
    private Label studentSummary;

    // What-if controls
    private TextField whatIfCgpa;
    private TextField whatIfSkills;

    public InsightsController(DataRepository repository, RankingEngine rankingEngine) {
        this.repository   = repository;
        this.rankingEngine = rankingEngine;
        this.view         = buildView();
    }

    public Node getView() { return view; }

    private VBox buildView() {
        VBox page = new VBox(20);
        page.getStyleClass().add("page");
        page.setPadding(new Insets(32));

        Label title = new Label("Student Opportunity Insights");
        title.getStyleClass().add("page-title");
        Label sub   = new Label("Explore which drives a student is eligible for and their matching scores");
        sub.getStyleClass().add("page-subtitle");

        HBox controls = buildControls();

        studentSummary = new Label();
        studentSummary.getStyleClass().add("page-subtitle");

        // What-if section
        VBox whatIfBox = buildWhatIfBox();

        resultsTable = buildResultsTable();
        VBox.setVgrow(resultsTable, Priority.ALWAYS);

        page.getChildren().addAll(
            new VBox(4, title, sub), controls, studentSummary, whatIfBox, resultsTable);
        return page;
    }

    private HBox buildControls() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        studentPicker = new ComboBox<>(
            FXCollections.observableArrayList(repository.getAllStudents()));
        studentPicker.setPromptText("Select a student…");
        studentPicker.getStyleClass().add("form-field");
        studentPicker.setPrefWidth(280);

        Button analyseBtn = new Button("Analyse Opportunities");
        analyseBtn.getStyleClass().addAll("btn", "btn-primary");
        analyseBtn.setOnAction(e -> runAnalysis(false));

        bar.getChildren().addAll(new Label("Student:"), studentPicker, analyseBtn);
        return bar;
    }

    private VBox buildWhatIfBox() {
        VBox box = new VBox(10);
        box.getStyleClass().add("chart-card");
        box.setPadding(new Insets(16));
        box.setMaxWidth(600);

        Label heading = new Label("What-If Simulator");
        heading.getStyleClass().add("card-title");

        Label hint = new Label("Override CGPA or add skills to see how eligibility changes.");
        hint.getStyleClass().add("page-subtitle");

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        whatIfCgpa = new TextField();
        whatIfCgpa.setPromptText("Override CGPA (e.g. 8.5)");
        whatIfCgpa.getStyleClass().add("form-field");
        whatIfCgpa.setPrefWidth(180);

        whatIfSkills = new TextField();
        whatIfSkills.setPromptText("Add skills (comma-separated)");
        whatIfSkills.getStyleClass().add("form-field");
        whatIfSkills.setPrefWidth(240);

        Button simBtn = new Button("Simulate");
        simBtn.getStyleClass().addAll("btn", "btn-secondary");
        simBtn.setOnAction(e -> runAnalysis(true));

        Button resetBtn = new Button("Reset");
        resetBtn.getStyleClass().addAll("btn", "btn-ghost");
        resetBtn.setOnAction(e -> { whatIfCgpa.clear(); whatIfSkills.clear(); runAnalysis(false); });

        row.getChildren().addAll(whatIfCgpa, whatIfSkills, simBtn, resetBtn);
        box.getChildren().addAll(heading, hint, row);
        return box;
    }

    private TableView<EligibilityResult> buildResultsTable() {
        TableView<EligibilityResult> t = new TableView<>();
        t.getStyleClass().add("data-table");
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<EligibilityResult, String> eligCol = new TableColumn<>("Eligible");
        eligCol.setPrefWidth(80);
        eligCol.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().isEligible() ? "YES" : "NO"));
        eligCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.getStyleClass().addAll("badge",
                    "YES".equals(item) ? "badge-active" : "badge-cancelled");
                setGraphic(badge);
            }
        });

        t.getColumns().addAll(
            eligCol,
            strCol("Drive",           200, r -> r.getDrive().getJobRole()),
            strCol("Company",         160, r -> r.getDrive().getCompanyName()),
            strCol("Min CGPA",         80, r -> String.format("%.1f", r.getDrive().getMinimumCgpa())),
            strCol("Score",            60, r -> r.isEligible() ? String.format("%.1f", r.getTotalScore()) : "—"),
            strCol("Matched Skills",  200, r -> String.join(", ", r.getMatchedSkills())),
            strCol("Missing Skills",  200, r -> String.join(", ", r.getMissingSkills())),
            strCol("Notes",           260, r -> r.isEligible()
                ? r.getScoreBreakdown()
                : String.join(" | ", r.getFailureReasons()))
        );
        t.setPlaceholder(new Label("Select a student to see their opportunities."));
        return t;
    }

    private TableColumn<EligibilityResult, String> strCol(String hdr, double w,
            java.util.function.Function<EligibilityResult, String> fn) {
        TableColumn<EligibilityResult, String> col = new TableColumn<>(hdr);
        col.setPrefWidth(w);
        col.setCellValueFactory(cd -> new SimpleStringProperty(fn.apply(cd.getValue())));
        return col;
    }

    private void runAnalysis(boolean whatIf) {
        Student selected = studentPicker.getValue();
        if (selected == null) return;

        // Optionally apply what-if overrides
        Student analysed = selected;
        if (whatIf) {
            // Clone student for simulation — don't mutate the real object
            analysed = cloneWithOverrides(selected);
        }

        List<RecruitmentDrive> drives = repository.getAllDrives();
        List<EligibilityResult> results = new java.util.ArrayList<>();
        for (RecruitmentDrive drive : drives) {
            List<EligibilityResult> eval = rankingEngine.evaluateAndRank(List.of(analysed), drive);
            if (!eval.isEmpty()) results.add(eval.get(0));
        }
        // Sort: eligible first, then by score
        results.sort(java.util.Comparator
            .comparing(EligibilityResult::isEligible).reversed()
            .thenComparingDouble(EligibilityResult::getTotalScore).reversed());

        resultsTable.setItems(FXCollections.observableArrayList(results));

        long eligible = results.stream().filter(EligibilityResult::isEligible).count();
        String prefix = whatIf ? "[SIMULATED] " : "";
        studentSummary.setText(prefix + analysed.getFullName()
            + "  |  CGPA: " + analysed.getCgpa()
            + "  |  Eligible for " + eligible + " / " + results.size() + " drives");
    }

    private Student cloneWithOverrides(Student original) {
        Student clone = new Student(
            original.getId() + "-sim",
            original.getFirstName(),
            original.getLastName(),
            original.getEmail(),
            original.getDepartment(),
            original.getGraduationYear(),
            original.getCgpa(),
            original.getActiveBacklogs()
        );
        clone.setSkills(original.getSkills());
        clone.setProjectCount(original.getProjectCount());
        clone.setInternshipCount(original.getInternshipCount());
        clone.setCertificationCount(original.getCertificationCount());

        // Apply CGPA override
        if (!whatIfCgpa.getText().isBlank()) {
            try {
                double overrideCgpa = Double.parseDouble(whatIfCgpa.getText().trim());
                if (overrideCgpa >= 0 && overrideCgpa <= 10) {
                    clone.setCgpa(overrideCgpa);
                }
            } catch (NumberFormatException ignored) {}
        }

        // Add extra skills
        if (!whatIfSkills.getText().isBlank()) {
            java.util.List<String> extra = java.util.Arrays.asList(
                whatIfSkills.getText().split(","));
            java.util.List<String> combined = new java.util.ArrayList<>(clone.getSkills());
            extra.stream().map(String::trim).filter(s -> !s.isEmpty())
                .forEach(combined::add);
            clone.setSkills(combined);
        }

        return clone;
    }
}
