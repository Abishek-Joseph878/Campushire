package com.campushire.controller;

import com.campushire.model.*;
import com.campushire.repository.DataRepository;
import com.campushire.service.ShortlistManager;
import com.campushire.util.CsvExporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

/**
 * Shortlisting pipeline screen: manage stages, filter by drive/stage, export CSV.
 */
public class ShortlistController {

    private final DataRepository   repository;
    private final ShortlistManager shortlistManager;
    private final VBox             view;

    private TableView<ShortlistEntry>        table;
    private ObservableList<ShortlistEntry>   masterList;
    private FilteredList<ShortlistEntry>     filteredList;
    private ComboBox<String>                 stageFilter;
    private ComboBox<RecruitmentDrive>       driveFilter;

    public ShortlistController(DataRepository repository, ShortlistManager shortlistManager) {
        this.repository       = repository;
        this.shortlistManager = shortlistManager;
        this.view             = buildView();
    }

    public Node getView() { return view; }

    private VBox buildView() {
        VBox page = new VBox(20);
        page.getStyleClass().add("page");
        page.setPadding(new Insets(32));

        Label title = new Label("Shortlisting Pipeline");
        title.getStyleClass().add("page-title");
        Label sub   = new Label("Track candidates through selection stages");
        sub.getStyleClass().add("page-subtitle");

        HBox toolbar = buildToolbar();
        table        = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        page.getChildren().addAll(new VBox(4, title, sub), toolbar, table);
        refreshData();
        return page;
    }

    private HBox buildToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        driveFilter = new ComboBox<>();
        driveFilter.setPromptText("All Drives");
        driveFilter.getStyleClass().add("filter-combo");
        driveFilter.setPrefWidth(220);

        stageFilter = new ComboBox<>(FXCollections.observableArrayList(
            "All Stages",
            ShortlistStage.SHORTLISTED.getDisplayName(),
            ShortlistStage.ASSESSMENT.getDisplayName(),
            ShortlistStage.INTERVIEW.getDisplayName(),
            ShortlistStage.SELECTED.getDisplayName(),
            ShortlistStage.REJECTED.getDisplayName()
        ));
        stageFilter.getSelectionModel().selectFirst();
        stageFilter.getStyleClass().add("filter-combo");
        stageFilter.setPrefWidth(140);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().addAll("btn", "btn-ghost");
        refreshBtn.setOnAction(e -> refreshData());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = new Button("Export CSV");
        exportBtn.getStyleClass().addAll("btn", "btn-secondary");
        exportBtn.setOnAction(e -> exportCsv());

        bar.getChildren().addAll(
            new Label("Drive:"), driveFilter,
            new Label("Stage:"), stageFilter,
            refreshBtn, spacer, exportBtn);

        driveFilter.setOnAction(e -> applyFilter());
        stageFilter.setOnAction(e -> applyFilter());

        return bar;
    }

    private TableView<ShortlistEntry> buildTable() {
        TableView<ShortlistEntry> t = new TableView<>();
        t.getStyleClass().add("data-table");
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Stage badge column
        TableColumn<ShortlistEntry, String> stageCol = new TableColumn<>("Stage");
        stageCol.setPrefWidth(110);
        stageCol.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getStage().getDisplayName()));
        stageCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.getStyleClass().addAll("badge", stageBadgeClass(item));
                setGraphic(badge);
            }
        });

        // Stage update column
        TableColumn<ShortlistEntry, String> updateCol = new TableColumn<>("Update Stage");
        updateCol.setPrefWidth(140);
        updateCol.setCellFactory(tc -> new TableCell<>() {
            private final ComboBox<ShortlistStage> combo = new ComboBox<>(
                FXCollections.observableArrayList(ShortlistStage.values()));
            private final Button applyBtn = new Button("Apply");
            private final HBox   box      = new HBox(6, combo, applyBtn);
            {
                combo.getStyleClass().add("filter-combo");
                applyBtn.getStyleClass().addAll("btn-sm", "btn-primary");
                applyBtn.setOnAction(e -> {
                    ShortlistEntry entry = getTableView().getItems().get(getIndex());
                    ShortlistStage newStage = combo.getValue();
                    if (newStage != null) {
                        shortlistManager.updateStage(entry.getId(), newStage);
                        refreshData();
                    }
                });
                box.setAlignment(Pos.CENTER_LEFT);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                ShortlistEntry entry = getTableView().getItems().get(getIndex());
                combo.setValue(entry.getStage());
                setGraphic(box);
            }
        });

        // Remove column
        TableColumn<ShortlistEntry, String> removeCol = new TableColumn<>("");
        removeCol.setPrefWidth(80);
        removeCol.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("Remove");
            {
                btn.getStyleClass().addAll("btn-sm", "btn-danger");
                btn.setOnAction(e -> {
                    ShortlistEntry entry = getTableView().getItems().get(getIndex());
                    shortlistManager.removeEntry(entry.getId());
                    refreshData();
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        t.getColumns().addAll(
            strCol("Student",     160, e -> e.getStudentName()),
            strCol("Drive",       200, e -> e.getDriveName()),
            strCol("Company",     140, e -> e.getCompanyName()),
            strCol("Score",        60, e -> String.format("%.1f", e.getMatchScore())),
            strCol("Date",         100, e -> e.getShortlistedDate() != null ? e.getShortlistedDate().toString() : ""),
            stageCol, updateCol, removeCol
        );
        t.setPlaceholder(new Label("No shortlisted candidates. Go to Talent Matching to shortlist."));
        return t;
    }

    private TableColumn<ShortlistEntry, String> strCol(String hdr, double w,
            java.util.function.Function<ShortlistEntry, String> fn) {
        TableColumn<ShortlistEntry, String> col = new TableColumn<>(hdr);
        col.setPrefWidth(w);
        col.setCellValueFactory(cd -> new SimpleStringProperty(fn.apply(cd.getValue())));
        return col;
    }

    private void refreshData() {
        masterList   = FXCollections.observableArrayList(shortlistManager.getAllEntries());
        filteredList = new FilteredList<>(masterList, e -> true);
        table.setItems(filteredList);

        // Populate drive filter
        List<RecruitmentDrive> drives = repository.getAllDrives();
        ObservableList<RecruitmentDrive> driveOptions = FXCollections.observableArrayList(drives);
        driveFilter.setItems(driveOptions);
        driveFilter.getSelectionModel().clearSelection();

        applyFilter();
    }

    private void applyFilter() {
        if (filteredList == null) return;
        RecruitmentDrive selectedDrive = driveFilter.getValue();
        String selectedStage = stageFilter.getValue();

        filteredList.setPredicate(e -> {
            boolean matchDrive = selectedDrive == null
                || e.getDriveId().equals(selectedDrive.getId());
            boolean matchStage = selectedStage == null
                || selectedStage.equals("All Stages")
                || e.getStage().getDisplayName().equals(selectedStage);
            return matchDrive && matchStage;
        });
    }

    private void exportCsv() {
        List<ShortlistEntry> toExport = filteredList != null
            ? filteredList.stream().toList()
            : shortlistManager.getAllEntries();

        if (toExport.isEmpty()) {
            showAlert("Nothing to export.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Shortlist CSV");
        chooser.setInitialFileName("campushire_shortlist.csv");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        File file = chooser.showSaveDialog(view.getScene().getWindow());
        if (file == null) return;

        try {
            CsvExporter.exportShortlist(toExport, file.getAbsolutePath());
            showInfo("Exported " + toExport.size() + " entries to\n" + file.getAbsolutePath());
        } catch (Exception e) {
            showAlert("Export failed: " + e.getMessage());
        }
    }

    private String stageBadgeClass(String stage) {
        return switch (stage) {
            case "Selected"    -> "badge-active";
            case "Rejected"    -> "badge-cancelled";
            case "Interview"   -> "badge-interview";
            case "Assessment"  -> "badge-assessment";
            default            -> "badge-upcoming";
        };
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
