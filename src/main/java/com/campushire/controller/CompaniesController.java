package com.campushire.controller;

import com.campushire.model.Company;
import com.campushire.model.RecruitmentDrive;
import com.campushire.repository.DataRepository;
import com.campushire.util.IdGenerator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Company management screen: list companies, add/edit, view associated drives.
 */
public class CompaniesController {

    private final DataRepository repository;
    private final VBox           view;
    private TableView<Company>   table;

    public CompaniesController(DataRepository repository) {
        this.repository = repository;
        this.view       = buildView();
    }

    public Node getView() { return view; }

    private VBox buildView() {
        VBox page = new VBox(20);
        page.getStyleClass().add("page");
        page.setPadding(new Insets(32));

        Label title = new Label("Companies");
        title.getStyleClass().add("page-title");
        Label sub   = new Label("Registered recruiting companies");
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

        Button addBtn = new Button("+ Add Company");
        addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> showDialog(null));

        bar.getChildren().addAll(spacer, addBtn);
        return bar;
    }

    private TableView<Company> buildTable() {
        TableView<Company> t = new TableView<>();
        t.getStyleClass().add("data-table");
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        t.getColumns().addAll(
            strCol("ID",          120, c -> c.getId()),
            strCol("Name",        200, c -> c.getName()),
            strCol("Industry",    140, c -> c.getIndustry()),
            strCol("Contact",     160, c -> c.getContactPerson() != null ? c.getContactPerson() : "—"),
            strCol("Email",       200, c -> c.getContactEmail() != null ? c.getContactEmail() : "—"),
            actionsCol()
        );
        t.setPlaceholder(new Label("No companies registered."));
        return t;
    }

    private TableColumn<Company, String> strCol(String hdr, double w,
                                                 java.util.function.Function<Company, String> fn) {
        TableColumn<Company, String> col = new TableColumn<>(hdr);
        col.setPrefWidth(w);
        col.setCellValueFactory(cd -> new SimpleStringProperty(fn.apply(cd.getValue())));
        return col;
    }

    private TableColumn<Company, String> actionsCol() {
        TableColumn<Company, String> col = new TableColumn<>("Actions");
        col.setPrefWidth(160);
        col.setCellFactory(tc -> new TableCell<>() {
            private final Button edit    = new Button("Edit");
            private final Button drives  = new Button("Drives");
            private final Button delete  = new Button("Delete");
            private final HBox   box     = new HBox(6, edit, drives, delete);
            {
                edit.getStyleClass().addAll("btn-sm", "btn-secondary");
                drives.getStyleClass().addAll("btn-sm", "btn-ghost");
                delete.getStyleClass().addAll("btn-sm", "btn-danger");
                edit.setOnAction(e -> showDialog(getTableView().getItems().get(getIndex())));
                drives.setOnAction(e -> showDrives(getTableView().getItems().get(getIndex())));
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
        table.setItems(FXCollections.observableArrayList(repository.getAllCompanies()));
    }

    private void showDialog(Company existing) {
        Dialog<Company> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Company" : "Edit Company");
        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(10);
        form.setPadding(new Insets(16));

        TextField nameF     = field(existing != null ? existing.getName()          : "");
        TextField industryF = field(existing != null ? existing.getIndustry()       : "");
        TextField contactF  = field(existing != null ? existing.getContactPerson()  != null ? existing.getContactPerson() : "" : "");
        TextField emailF    = field(existing != null ? existing.getContactEmail()   != null ? existing.getContactEmail()  : "" : "");
        TextField websiteF  = field(existing != null ? existing.getWebsite()        != null ? existing.getWebsite()       : "" : "");
        TextArea  descF     = new TextArea(existing != null && existing.getDescription() != null ? existing.getDescription() : "");
        descF.setPrefRowCount(3);
        descF.getStyleClass().add("form-field");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);

        addRow(form, 0, "Company Name *", nameF);
        addRow(form, 1, "Industry",        industryF);
        addRow(form, 2, "Contact Person",  contactF);
        addRow(form, 3, "Contact Email",   emailF);
        addRow(form, 4, "Website",         websiteF);
        form.add(new Label("Description"), 0, 5);
        form.add(descF, 1, 5);
        form.add(errorLabel, 0, 6, 2, 1);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/com/campushire/css/theme.css").toExternalForm());

        Node saveBtn = dialog.getDialogPane().lookupButton(saveType);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            if (nameF.getText().isBlank()) {
                errorLabel.setText("Company name is required.");
                errorLabel.setVisible(true);
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn != saveType) return null;
            Company c = existing != null ? existing : new Company();
            if (c.getId() == null) c.setId(IdGenerator.newCompanyId());
            c.setName(nameF.getText().trim());
            c.setIndustry(industryF.getText().trim());
            c.setContactPerson(contactF.getText().trim());
            c.setContactEmail(emailF.getText().trim());
            c.setWebsite(websiteF.getText().trim());
            c.setDescription(descF.getText().trim());
            return c;
        });

        dialog.showAndWait().ifPresent(c -> { repository.saveCompany(c); refreshData(); });
    }

    private void showDrives(Company company) {
        java.util.List<RecruitmentDrive> drives = repository.getDrivesByCompany(company.getId());
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Drives for " + company.getName());
        info.setHeaderText(company.getName() + " — " + drives.size() + " drive(s)");
        StringBuilder sb = new StringBuilder();
        for (RecruitmentDrive d : drives) {
            sb.append("• ").append(d.getJobRole())
              .append(" [").append(d.getStatus().getDisplayName()).append("]")
              .append("\n");
        }
        info.setContentText(drives.isEmpty() ? "No drives associated." : sb.toString());
        info.getDialogPane().getStylesheets().add(
            getClass().getResource("/com/campushire/css/theme.css").toExternalForm());
        info.showAndWait();
    }

    private void confirmDelete(Company c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete " + c.getName() + "?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Delete");
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/com/campushire/css/theme.css").toExternalForm());
        alert.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            repository.deleteCompany(c.getId());
            refreshData();
        });
    }

    private TextField field(String value) {
        TextField tf = new TextField(value);
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
