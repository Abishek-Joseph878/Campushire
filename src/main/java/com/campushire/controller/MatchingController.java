package com.campushire.controller;

import com.campushire.model.*;
import com.campushire.repository.DataRepository;
import com.campushire.service.RankingEngine;
import com.campushire.service.ShortlistManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Talent matching screen: pick a drive, evaluate all candidates, show ranked results
 * with scores and skill breakdown. Allows shortlisting from here.
 */
public class MatchingController {

    private final DataRepository  repository;
    private final RankingEngine   rankingEngine;
    private final ShortlistManager shortlistManager;
    private final VBox            view;

    private ComboBox<RecruitmentDrive>    drivePicker;
    private TableView<EligibilityResult>  resultsTable;
    private Label                         strategyLabel;
    private ObservableList<EligibilityResult> masterResults;
    private FilteredList<EligibilityResult>   filteredResults;
    private CheckBox showIneligibleCheck;

    public MatchingController(DataRepository repository, RankingEngine rankingEngine,
                              ShortlistManager shortlistManager) {
        this.repository       = repository;
        this.rankingEngine    = rankingEngine;
        this.shortlistManager = shortlistManager;
        this.view             = buildView();
    }

    public Node getView() { return view; }

    private VBox buildView() {
        VBox page = new VBox(20);
        page.getStyleClass().add("page");
        page.setPadding(new Insets(32));

        Label title = new Label("Talent Matching");
        title.getStyleClass().add("page-title");
        Label sub   = new Label("Evaluate candidates against a drive and rank eligible applicants");
        sub.getStyleClass().add("page-subtitle");

        HBox controls = buildControls();

        strategyLabel = new Label();
        strategyLabel.getStyleClass().add("strategy-label");
        updateStrategyLabel();

        resultsTable = buildResultsTable();
        VBox.setVgrow(resultsTable, Priority.ALWAYS);

        page.getChildren().addAll(new VBox(4, title, sub), controls, strategyLabel, resultsTable);
        return page;
    }

    private HBox buildControls() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        drivePicker = new ComboBox<>(
            FXCollections.observableArrayList(repository.getAllDrives()));
        drivePicker.setPromptText("Select a recruitment drive…");
        drivePicker.getStyleClass().add("form-field");
        drivePicker.setPrefWidth(340);

        Button evalBtn = new Button("Evaluate Candidates");
        evalBtn.getStyleClass().addAll("btn", "btn-primary");
        evalBtn.setOnAction(e -> runEvaluation());

        showIneligibleCheck = new CheckBox("Show Ineligible");
        showIneligibleCheck.setSelected(true);
        showIneligibleCheck.setOnAction(e -> applyFilter());

        bar.getChildren().addAll(
            new Label("Drive:"), drivePicker, evalBtn, showIneligibleCheck);
        return bar;
    }

    private TableView<EligibilityResult> buildResultsTable() {
        TableView<EligibilityResult> t = new TableView<>();
        t.getStyleClass().add("data-table");
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Rank column
        TableColumn<EligibilityResult, String> rankCol = new TableColumn<>("#");
        rankCol.setPrefWidth(40);
        rankCol.setCellValueFactory(cd -> {
            if (!cd.getValue().isEligible()) return new SimpleStringProperty("—");
            int idx = t.getItems().indexOf(cd.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(idx));
        });

        // Eligible badge column
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

        TableColumn<EligibilityResult, String> nameCol  = strCol("Name",       160, r -> r.getStudent().getFullName());
        TableColumn<EligibilityResult, String> deptCol  = strCol("Dept",        70, r -> r.getStudent().getDepartment());
        TableColumn<EligibilityResult, String> cgpaCol  = strCol("CGPA",        60, r -> String.format("%.2f", r.getStudent().getCgpa()));
        TableColumn<EligibilityResult, String> scoreCol = strCol("Score",        60, r -> r.isEligible() ? String.format("%.1f", r.getTotalScore()) : "—");
        TableColumn<EligibilityResult, String> matchedCol = strCol("Matched Skills", 180,
            r -> String.join(", ", r.getMatchedSkills()));
        TableColumn<EligibilityResult, String> missingCol = strCol("Missing Skills", 180,
            r -> String.join(", ", r.getMissingSkills()));
        TableColumn<EligibilityResult, String> reasonCol = strCol("Reason / Breakdown", 260,
            r -> r.isEligible() ? r.getScoreBreakdown() :
                 String.join(" | ", r.getFailureReasons()));

        // Shortlist action column
        TableColumn<EligibilityResult, String> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("Shortlist");
            {
                btn.getStyleClass().addAll("btn-sm", "btn-primary");
                btn.setOnAction(e -> shortlistCandidate(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                EligibilityResult r = getTableView().getItems().get(getIndex());
                setGraphic(r.isEligible() ? btn : null);
            }
        });

        t.getColumns().addAll(rankCol, eligCol, nameCol, deptCol, cgpaCol,
            scoreCol, matchedCol, missingCol, reasonCol, actionCol);
        t.setPlaceholder(new Label("Select a drive and click Evaluate Candidates."));
        return t;
    }

    private TableColumn<EligibilityResult, String> strCol(String hdr, double w,
            java.util.function.Function<EligibilityResult, String> fn) {
        TableColumn<EligibilityResult, String> col = new TableColumn<>(hdr);
        col.setPrefWidth(w);
        col.setCellValueFactory(cd -> new SimpleStringProperty(fn.apply(cd.getValue())));
        return col;
    }

    private void runEvaluation() {
        RecruitmentDrive drive = drivePicker.getValue();
        if (drive == null) {
            showAlert("Select a drive to evaluate.");
            return;
        }
        List<Student> students = repository.getAllStudents();
        List<EligibilityResult> results = rankingEngine.evaluateAndRank(students, drive);
        masterResults   = FXCollections.observableArrayList(results);
        filteredResults = new FilteredList<>(masterResults, r -> true);
        resultsTable.setItems(filteredResults);
        applyFilter();
        updateStrategyLabel();
    }

    private void applyFilter() {
        if (filteredResults == null) return;
        boolean showAll = showIneligibleCheck.isSelected();
        filteredResults.setPredicate(r -> showAll || r.isEligible());
    }

    private void shortlistCandidate(EligibilityResult result) {
        if (!result.isEligible()) return;
        RecruitmentDrive drive = drivePicker.getValue();
        if (drive == null) return;
        try {
            shortlistManager.shortlist(result.getStudent(), drive, result.getTotalScore());
            showInfo(result.getStudent().getFullName() + " has been shortlisted.");
        } catch (IllegalStateException e) {
            showAlert(e.getMessage());
        }
    }

    private void updateStrategyLabel() {
        strategyLabel.setText("Ranking: " + rankingEngine.getStrategy().getDescription());
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.getDialogPane().getStylesheets().add(
            getClass().getResource("/com/campushire/css/theme.css").toExternalForm());
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.getDialogPane().getStylesheets().add(
            getClass().getResource("/com/campushire/css/theme.css").toExternalForm());
        a.showAndWait();
    }
}
