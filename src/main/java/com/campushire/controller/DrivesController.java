package com.campushire.controller;

import com.campushire.model.*;
import com.campushire.repository.DataRepository;
import com.campushire.util.IdGenerator;
import com.campushire.util.ValidationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Recruitment drives screen: list, create, edit drives.
 */
public class DrivesController {

    private final DataRepository       repository;
    private final VBox                 view;
    private TableView<RecruitmentDrive> table;

    public DrivesController(DataRepository repository) {
        this.repository = repository;
        this.view       = buildView();
    }

    public Node getView() { return view; }

    private VBox buildView() {
        VBox page = new VBox(20);
        page.getStyleClass().add("page");
        page.setPadding(new Insets(32));

        Label title = new Label("Recruitment Drives");
        title.getStyleClass().add("page-title");
        Label sub   = new Label("Create and manage campus recruitment drives");
        sub.getStyleClass().add("page-subtitle");

        HBox toolbar = buildToolbar();
        table        = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        page.getChildren().addAll(new VBox(4, title, sub), toolbar, table);
        refreshData();
        return page;
    }

    private HBox buildToolbar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ New Drive");
        addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> showDialog(null));

        bar.getChildren().addAll(spacer, addBtn);
        return bar;
    }

    private TableView<RecruitmentDrive> buildTable() {
        TableView<RecruitmentDrive> t = new TableView<>();
        t.getStyleClass().add("data-table");
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<RecruitmentDrive, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(100);
        statusCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus().getDisplayName()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.getStyleClass().addAll("badge", "badge-" + item.toLowerCase());
                setGraphic(badge);
            }
        });

        t.getColumns().addAll(
            strCol("Drive ID",    120, d -> d.getId()),
            strCol("Company",     160, d -> d.getCompanyName()),
            strCol("Job Role",    180, d -> d.getJobRole()),
            strCol("Min CGPA",     80, d -> String.format("%.1f", d.getMinimumCgpa())),
            strCol("Max Backlogs", 90, d -> String.valueOf(d.getMaximumBacklogs())),
            strCol("Package",      90, d -> d.getPackageLpa() != null ? d.getPackageLpa() : "—"),
            strCol("Drive Date",  110, d -> d.getDriveDate() != null ? d.getDriveDate().toString() : "—"),
            statusCol,
            actionsCol()
        );
        t.setPlaceholder(new Label("No drives created yet."));
        return t;
    }

    private TableColumn<RecruitmentDrive, String> strCol(String hdr, double w,
            java.util.function.Function<RecruitmentDrive, String> fn) {
        TableColumn<RecruitmentDrive, String> col = new TableColumn<>(hdr);
        col.setPrefWidth(w);
        col.setCellValueFactory(cd -> new SimpleStringProperty(fn.apply(cd.getValue())));
        return col;
    }

    private TableColumn<RecruitmentDrive, String> actionsCol() {
        TableColumn<RecruitmentDrive, String> col = new TableColumn<>("Actions");
        col.setPrefWidth(120);
        col.setCellFactory(tc -> new TableCell<>() {
            private final Button edit   = new Button("Edit");
            private final Button delete = new Button("Delete");
            private final HBox   box    = new HBox(6, edit, delete);
            {
                edit.getStyleClass().addAll("btn-sm", "btn-secondary");
                delete.getStyleClass().addAll("btn-sm", "btn-danger");
                edit.setOnAction(e -> showDialog(getTableView().getItems().get(getIndex())));
                delete.setOnAction(e -> confirmDelete(getTableView().getItems().get(getIndex())));
                box.setAlignment(Pos.CENTER_LEFT);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
        return col;
    }

    private void refreshData() {
        table.setItems(FXCollections.observableArrayList(repository.getAllDrives()));
    }

    // -------------------------------------------------------------------------
    // Drive Form Dialog
    // -------------------------------------------------------------------------
    private void showDialog(RecruitmentDrive existing) {
        Dialog<RecruitmentDrive> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "New Recruitment Drive" : "Edit Drive");
        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        // Company picker
        List<Company> companies = repository.getAllCompanies();
        ComboBox<Company> companyPicker = new ComboBox<>(FXCollections.observableArrayList(companies));
        companyPicker.setPromptText("Select Company");
        companyPicker.getStyleClass().add("form-field");
        companyPicker.setPrefWidth(280);
        if (existing != null) {
            companies.stream()
                .filter(c -> c.getId().equals(existing.getCompanyId()))
                .findFirst().ifPresent(companyPicker::setValue);
        }

        TextField jobRoleF    = field(existing != null ? existing.getJobRole() : "");
        TextField minCgpaF    = field(existing != null ? String.valueOf(existing.getMinimumCgpa()) : "7.0");
        TextField maxBlogsF   = field(existing != null ? String.valueOf(existing.getMaximumBacklogs()) : "0");
        TextField deptsF      = field(existing != null ? existing.getDepartmentsDisplay() : "CSE,IT");
        TextField reqSkillsF  = field(existing != null ? existing.getRequiredSkillsDisplay() : "");
        TextField prefSkillsF = field(existing != null ? String.join(",", existing.getPreferredSkills()) : "");
        TextField positionsF  = field(existing != null ? String.valueOf(existing.getOpenPositions()) : "1");
        TextField packageF    = field(existing != null && existing.getPackageLpa() != null ? existing.getPackageLpa() : "");
        TextField locationF   = field(existing != null && existing.getWorkLocation() != null ? existing.getWorkLocation() : "");
        DatePicker driveDateF = new DatePicker(existing != null ? existing.getDriveDate() : LocalDate.now().plusDays(14));
        driveDateF.getStyleClass().add("form-field");

        ComboBox<DriveStatus> statusPicker = new ComboBox<>(
            FXCollections.observableArrayList(DriveStatus.values()));
        statusPicker.setValue(existing != null ? existing.getStatus() : DriveStatus.UPCOMING);
        statusPicker.getStyleClass().add("form-field");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(10);
        form.setPadding(new Insets(16));
        int r = 0;
        addRow(form, r++, "Company *",             companyPicker);
        addRow(form, r++, "Job Role *",            jobRoleF);
        addRow(form, r++, "Minimum CGPA *",        minCgpaF);
        addRow(form, r++, "Max Backlogs *",        maxBlogsF);
        addRow(form, r++, "Eligible Depts (CSV)",  deptsF);
        addRow(form, r++, "Required Skills (CSV)", reqSkillsF);
        addRow(form, r++, "Preferred Skills (CSV)",prefSkillsF);
        addRow(form, r++, "Open Positions",        positionsF);
        addRow(form, r++, "Package",               packageF);
        addRow(form, r++, "Location",              locationF);
        addRow(form, r++, "Drive Date",            driveDateF);
        addRow(form, r++, "Status",                statusPicker);
        form.add(errorLabel, 0, r, 2, 1);

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(480);
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/com/campushire/css/theme.css").toExternalForm());
        dialog.getDialogPane().setPrefWidth(520);

        Node saveBtn = dialog.getDialogPane().lookupButton(saveType);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String cid = companyPicker.getValue() != null ? companyPicker.getValue().getId() : "";
            List<String> errors = ValidationUtil.validateDrive(
                cid, jobRoleF.getText(), minCgpaF.getText(), maxBlogsF.getText());
            if (!errors.isEmpty()) {
                errorLabel.setText(String.join("\n", errors));
                errorLabel.setVisible(true);
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn != saveType) return null;
            Company selected = companyPicker.getValue();
            if (selected == null) return null;

            RecruitmentDrive d = existing != null ? existing : new RecruitmentDrive();
            if (d.getId() == null) d.setId(IdGenerator.newDriveId());
            d.setCompanyId(selected.getId());
            d.setCompanyName(selected.getName());
            d.setJobRole(jobRoleF.getText().trim());
            d.setMinimumCgpa(Double.parseDouble(minCgpaF.getText().trim()));
            d.setMaximumBacklogs(Integer.parseInt(maxBlogsF.getText().trim()));
            d.setEligibleDepartments(parseCsv(deptsF.getText()));
            d.setRequiredSkills(parseCsv(reqSkillsF.getText()));
            d.setPreferredSkills(parseCsv(prefSkillsF.getText()));
            try { d.setOpenPositions(Integer.parseInt(positionsF.getText().trim())); } catch (Exception ignored) {}
            d.setPackageLpa(packageF.getText().trim());
            d.setWorkLocation(locationF.getText().trim());
            d.setDriveDate(driveDateF.getValue());
            d.setStatus(statusPicker.getValue());
            return d;
        });

        dialog.showAndWait().ifPresent(d -> { repository.saveDrive(d); refreshData(); });
    }

    private List<String> parseCsv(String text) {
        if (text == null || text.isBlank()) return List.of();
        return Arrays.stream(text.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }

    private void confirmDelete(RecruitmentDrive d) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete drive: " + d.getJobRole() + " @ " + d.getCompanyName() + "?",
            ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Delete");
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/com/campushire/css/theme.css").toExternalForm());
        alert.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            repository.deleteDrive(d.getId());
            refreshData();
        });
    }

    private TextField field(String v) {
        TextField tf = new TextField(v);
        tf.getStyleClass().add("form-field");
        tf.setPrefWidth(280);
        return tf;
    }

    private void addRow(GridPane g, int row, String lbl, Node field) {
        Label l = new Label(lbl);
        l.getStyleClass().add("form-label");
        g.add(l, 0, row);
        g.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }
}
