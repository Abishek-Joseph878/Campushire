package com.campushire.controller;

import com.campushire.model.Student;
import com.campushire.repository.DataRepository;
import com.campushire.util.IdGenerator;
import com.campushire.util.ValidationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Candidate directory: searchable/filterable table with add/edit forms.
 */
public class CandidatesController {

    private final DataRepository   repository;
    private final Consumer<String> navigate;
    private final VBox             view;

    private ObservableList<Student> masterList;
    private FilteredList<Student>   filteredList;
    private TableView<Student>      table;
    private TextField               searchField;
    private ComboBox<String>        deptFilter;

    public CandidatesController(DataRepository repository, Consumer<String> navigate) {
        this.repository = repository;
        this.navigate   = navigate;
        this.view       = buildView();
    }

    public Node getView() { return view; }

    // -------------------------------------------------------------------------
    private VBox buildView() {
        VBox page = new VBox(20);
        page.getStyleClass().add("page");
        page.setPadding(new Insets(32));

        // Header
        Label title = new Label("Candidate Directory");
        title.getStyleClass().add("page-title");
        Label sub   = new Label("All registered students");
        sub.getStyleClass().add("page-subtitle");

        // Toolbar
        HBox toolbar = buildToolbar();

        // Table
        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        page.getChildren().addAll(new VBox(4, title, sub), toolbar, table);
        refreshData();
        return page;
    }

    private HBox buildToolbar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search by name or ID…");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(260);

        deptFilter = new ComboBox<>();
        deptFilter.setPromptText("Department");
        deptFilter.getStyleClass().add("filter-combo");
        deptFilter.setPrefWidth(140);

        Button clearBtn = new Button("Clear");
        clearBtn.getStyleClass().addAll("btn", "btn-ghost");

        Button addBtn = new Button("+ Add Student");
        addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> showAddEditDialog(null));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(searchField, deptFilter, clearBtn, spacer, addBtn);

        searchField.textProperty().addListener((obs, old, nw) -> applyFilter());
        deptFilter.setOnAction(e -> applyFilter());
        clearBtn.setOnAction(e -> { searchField.clear(); deptFilter.getSelectionModel().clearSelection(); });

        return bar;
    }

    private TableView<Student> buildTable() {
        TableView<Student> t = new TableView<>();
        t.getStyleClass().add("data-table");
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Student, String> idCol    = col("Student ID",  200, s -> s.getId());
        TableColumn<Student, String> nameCol  = col("Name",        200, s -> s.getFullName());
        TableColumn<Student, String> deptCol  = col("Department",  120, s -> s.getDepartment());
        TableColumn<Student, String> cgpaCol  = col("CGPA",         80, s -> String.format("%.2f", s.getCgpa()));
        TableColumn<Student, String> blCol    = col("Backlogs",     80, s -> String.valueOf(s.getActiveBacklogs()));
        TableColumn<Student, String> skillCol = col("Skills",       300, s -> s.getSkillsDisplay());

        // Actions column
        TableColumn<Student, String> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(140);
        actCol.setCellFactory(tc -> new TableCell<>() {
            private final Button edit   = new Button("Edit");
            private final Button delete = new Button("Delete");
            private final HBox   box    = new HBox(6, edit, delete);
            {
                edit.getStyleClass().addAll("btn-sm", "btn-secondary");
                delete.getStyleClass().addAll("btn-sm", "btn-danger");
                edit.setOnAction(e -> showAddEditDialog(getTableView().getItems().get(getIndex())));
                delete.setOnAction(e -> confirmDelete(getTableView().getItems().get(getIndex())));
                box.setAlignment(Pos.CENTER_LEFT);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        t.getColumns().addAll(idCol, nameCol, deptCol, cgpaCol, blCol, skillCol, actCol);
        t.setPlaceholder(new Label("No candidates found."));
        return t;
    }

    private <T> TableColumn<Student, String> col(String header, double width,
                                                  java.util.function.Function<Student, String> mapper) {
        TableColumn<Student, String> col = new TableColumn<>(header);
        col.setPrefWidth(width);
        col.setCellValueFactory(cd -> new SimpleStringProperty(mapper.apply(cd.getValue())));
        return col;
    }

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------
    private void refreshData() {
        masterList   = FXCollections.observableArrayList(repository.getAllStudents());
        filteredList = new FilteredList<>(masterList, s -> true);
        table.setItems(filteredList);

        // Populate dept filter
        List<String> depts = masterList.stream()
            .map(Student::getDepartment)
            .filter(d -> d != null && !d.isBlank())
            .distinct().sorted().collect(Collectors.toList());
        depts.add(0, "All Departments");
        deptFilter.setItems(FXCollections.observableArrayList(depts));
    }

    private void applyFilter() {
        String search = searchField.getText().trim().toLowerCase();
        String dept   = deptFilter.getValue();

        filteredList.setPredicate(s -> {
            boolean matchSearch = search.isEmpty()
                || s.getFullName().toLowerCase().contains(search)
                || s.getId().toLowerCase().contains(search);
            boolean matchDept = dept == null || dept.isBlank()
                || dept.equals("All Departments")
                || dept.equals(s.getDepartment());
            return matchSearch && matchDept;
        });
    }

    // -------------------------------------------------------------------------
    // Add / Edit Dialog
    // -------------------------------------------------------------------------
    private void showAddEditDialog(Student existing) {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Student" : "Edit Student");
        dialog.setHeaderText(null);

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane form = buildStudentForm(existing);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/com/campushire/css/theme.css").toExternalForm());

        // Retrieve fields from form
        TextField firstNameF  = (TextField) form.lookup("#firstName");
        TextField lastNameF   = (TextField) form.lookup("#lastName");
        TextField emailF      = (TextField) form.lookup("#email");
        TextField deptF       = (TextField) form.lookup("#dept");
        TextField cgpaF       = (TextField) form.lookup("#cgpa");
        TextField backlogsF   = (TextField) form.lookup("#backlogs");
        TextField skillsF     = (TextField) form.lookup("#skills");
        TextField projectsF   = (TextField) form.lookup("#projects");
        TextField internF     = (TextField) form.lookup("#interns");
        TextField certF       = (TextField) form.lookup("#certs");

        Label errorLabel = (Label) form.lookup("#errorLabel");

        Node saveButton = dialog.getDialogPane().lookupButton(saveType);
        dialog.setResultConverter(btn -> {
            if (btn != saveType) return null;

            List<String> errors = ValidationUtil.validateStudent(
                firstNameF.getText(), lastNameF.getText(),
                deptF.getText(), cgpaF.getText(), backlogsF.getText());

            if (!errors.isEmpty()) {
                errorLabel.setText(String.join("\n", errors));
                errorLabel.setVisible(true);
                return null;
            }

            Student s = existing != null ? existing : new Student();
            if (s.getId() == null) s.setId(IdGenerator.newStudentId());
            s.setFirstName(firstNameF.getText().trim());
            s.setLastName(lastNameF.getText().trim());
            s.setEmail(emailF.getText().trim());
            s.setDepartment(deptF.getText().trim().toUpperCase());
            s.setCgpa(Double.parseDouble(cgpaF.getText().trim()));
            s.setActiveBacklogs(Integer.parseInt(backlogsF.getText().trim()));
            String skillsCsv = skillsF.getText().trim();
            if (!skillsCsv.isBlank()) {
                s.setSkills(Arrays.asList(skillsCsv.split(",")));
            }
            try { s.setProjectCount(Integer.parseInt(projectsF.getText().trim())); } catch (Exception ignored) {}
            try { s.setInternshipCount(Integer.parseInt(internF.getText().trim())); } catch (Exception ignored) {}
            try { s.setCertificationCount(Integer.parseInt(certF.getText().trim())); } catch (Exception ignored) {}
            return s;
        });

        // Keep dialog open on validation error
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            List<String> errors = ValidationUtil.validateStudent(
                firstNameF.getText(), lastNameF.getText(),
                deptF.getText(), cgpaF.getText(), backlogsF.getText());
            if (!errors.isEmpty()) {
                errorLabel.setText(String.join("\n", errors));
                errorLabel.setVisible(true);
                event.consume();
            }
        });

        dialog.showAndWait().ifPresent(s -> {
            repository.saveStudent(s);
            refreshData();
        });
    }

    private GridPane buildStudentForm(Student s) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        addFormRow(grid, 0,  "First Name *",  textField("#firstName", s != null ? s.getFirstName() : ""));
        addFormRow(grid, 1,  "Last Name *",   textField("#lastName",  s != null ? s.getLastName()  : ""));
        addFormRow(grid, 2,  "Email",          textField("#email",     s != null ? s.getEmail()     : ""));
        addFormRow(grid, 3,  "Department *",   textField("#dept",      s != null ? s.getDepartment(): ""));
        addFormRow(grid, 4,  "CGPA * (0–10)", textField("#cgpa",      s != null ? String.valueOf(s.getCgpa()) : ""));
        addFormRow(grid, 5,  "Active Backlogs *", textField("#backlogs", s != null ? String.valueOf(s.getActiveBacklogs()) : "0"));
        addFormRow(grid, 6,  "Skills (comma-separated)", textField("#skills", s != null ? s.getSkillsDisplay() : ""));
        addFormRow(grid, 7,  "Projects",       textField("#projects",  s != null ? String.valueOf(s.getProjectCount()) : "0"));
        addFormRow(grid, 8,  "Internships",    textField("#interns",   s != null ? String.valueOf(s.getInternshipCount()) : "0"));
        addFormRow(grid, 9,  "Certifications", textField("#certs",     s != null ? String.valueOf(s.getCertificationCount()) : "0"));

        Label errorLabel = new Label();
        errorLabel.setId("errorLabel");
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        grid.add(errorLabel, 0, 10, 2, 1);

        return grid;
    }

    private void addFormRow(GridPane grid, int row, String labelText, TextField field) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("form-label");
        grid.add(lbl, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
        field.setPrefWidth(260);
    }

    private TextField textField(String id, String value) {
        TextField tf = new TextField(value);
        tf.setId(id.replace("#", ""));
        tf.getStyleClass().add("form-field");
        return tf;
    }

    private void confirmDelete(Student s) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete " + s.getFullName() + "? This cannot be undone.",
            ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Delete");
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/com/campushire/css/theme.css").toExternalForm());
        alert.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            repository.deleteStudent(s.getId());
            refreshData();
        });
    }
}
