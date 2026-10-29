package com.campushire.controller;

import com.campushire.model.DriveStatus;
import com.campushire.model.ShortlistStage;
import com.campushire.repository.DataRepository;
import com.campushire.service.AnalyticsService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Dashboard screen: KPI cards, CGPA distribution chart, pipeline chart, quick actions.
 */
public class DashboardController {

    private final DataRepository   repository;
    private final AnalyticsService analytics;
    private final Consumer<String> navigate;
    private final VBox             view;

    public DashboardController(DataRepository repository,
                               AnalyticsService analytics,
                               Consumer<String> navigate) {
        this.repository = repository;
        this.analytics  = analytics;
        this.navigate   = navigate;
        this.view       = buildView();
    }

    public Node getView() {
        return view;
    }

    private VBox buildView() {
        VBox page = new VBox(24);
        page.getStyleClass().add("page");
        page.setPadding(new Insets(32));

        // Header
        Label title    = new Label("Dashboard");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Campus recruitment overview");
        subtitle.getStyleClass().add("page-subtitle");
        VBox header = new VBox(4, title, subtitle);

        // KPI cards
        AnalyticsService.DashboardStats stats = analytics.getDashboardStats();
        HBox kpiRow = buildKpiRow(stats);

        // Charts row
        HBox chartsRow = buildChartsRow();
        chartsRow.setSpacing(20);

        // Quick actions
        HBox actions = buildQuickActions();

        page.getChildren().addAll(header, kpiRow, chartsRow, actions);
        return page;
    }

    // -------------------------------------------------------------------------
    // KPI Cards
    // -------------------------------------------------------------------------
    private HBox buildKpiRow(AnalyticsService.DashboardStats stats) {
        HBox row = new HBox(16);
        row.getChildren().addAll(
            kpiCard("Total Students",    String.valueOf(stats.totalStudents()),   "kpi-blue"),
            kpiCard("Companies",         String.valueOf(stats.totalCompanies()),  "kpi-indigo"),
            kpiCard("Active Drives",     String.valueOf(stats.activeDrives()),    "kpi-green"),
            kpiCard("Eligible",          String.valueOf(stats.eligibleCandidates()), "kpi-amber"),
            kpiCard("Shortlisted",       String.valueOf(stats.totalShortlisted()), "kpi-purple")
        );
        row.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        return row;
    }

    private VBox kpiCard(String label, String value, String colorClass) {
        VBox card = new VBox(6);
        card.getStyleClass().addAll("kpi-card", colorClass);
        card.setPadding(new Insets(20, 24, 20, 24));
        card.setMinWidth(140);

        Label val  = new Label(value);
        val.getStyleClass().add("kpi-value");
        Label lbl  = new Label(label);
        lbl.getStyleClass().add("kpi-label");

        card.getChildren().addAll(val, lbl);
        return card;
    }

    // -------------------------------------------------------------------------
    // Charts
    // -------------------------------------------------------------------------
    private HBox buildChartsRow() {
        HBox row = new HBox(20);

        // CGPA Distribution bar chart
        BarChart<String, Number> cgpaChart = buildCgpaChart();
        VBox cgpaCard = chartCard("CGPA Distribution", cgpaChart);
        HBox.setHgrow(cgpaCard, Priority.ALWAYS);

        // Pipeline funnel (horizontal bar)
        BarChart<Number, String> pipelineChart = buildPipelineChart();
        VBox pipelineCard = chartCard("Shortlist Pipeline", pipelineChart);
        HBox.setHgrow(pipelineCard, Priority.ALWAYS);

        // Department breakdown
        BarChart<String, Number> deptChart = buildDeptChart();
        VBox deptCard = chartCard("Students by Department", deptChart);
        HBox.setHgrow(deptCard, Priority.ALWAYS);

        row.getChildren().addAll(cgpaCard, pipelineCard, deptCard);
        return row;
    }

    private BarChart<String, Number> buildCgpaChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        xAxis.setLabel("CGPA Range");
        yAxis.setLabel("Students");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(220);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Long> dist = analytics.getCgpaDistribution();
        dist.forEach((range, count) -> series.getData().add(
            new XYChart.Data<>(range, count)));
        chart.getData().add(series);
        return chart;
    }

    private BarChart<Number, String> buildPipelineChart() {
        NumberAxis   xAxis = new NumberAxis();
        CategoryAxis yAxis = new CategoryAxis();
        xAxis.setLabel("Count");

        BarChart<Number, String> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(220);

        XYChart.Series<Number, String> series = new XYChart.Series<>();
        Map<ShortlistStage, Long> stageData = analytics.getShortlistByStage();
        for (ShortlistStage stage : ShortlistStage.values()) {
            long count = stageData.getOrDefault(stage, 0L);
            series.getData().add(new XYChart.Data<>(count, stage.getDisplayName()));
        }
        chart.getData().add(series);
        return chart;
    }

    private BarChart<String, Number> buildDeptChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        yAxis.setLabel("Students");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(220);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Long> deptData = analytics.getStudentsByDepartment();
        deptData.forEach((dept, count) -> series.getData().add(
            new XYChart.Data<>(dept, count)));
        chart.getData().add(series);
        return chart;
    }

    private VBox chartCard(String title, Node chart) {
        VBox card = new VBox(10);
        card.getStyleClass().add("chart-card");
        card.setPadding(new Insets(16, 16, 16, 16));

        Label lbl = new Label(title);
        lbl.getStyleClass().add("card-title");
        card.getChildren().addAll(lbl, chart);
        return card;
    }

    // -------------------------------------------------------------------------
    // Quick Actions
    // -------------------------------------------------------------------------
    private HBox buildQuickActions() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label("Quick Actions:");
        lbl.getStyleClass().add("section-label");

        Button newDrive = new Button("+ New Drive");
        newDrive.getStyleClass().addAll("btn", "btn-primary");
        newDrive.setOnAction(e -> navigate.accept("drives"));

        Button viewCandidates = new Button("View Candidates");
        viewCandidates.getStyleClass().addAll("btn", "btn-secondary");
        viewCandidates.setOnAction(e -> navigate.accept("candidates"));

        Button runMatching = new Button("Run Matching");
        runMatching.getStyleClass().addAll("btn", "btn-secondary");
        runMatching.setOnAction(e -> navigate.accept("matching"));

        row.getChildren().addAll(lbl, newDrive, viewCandidates, runMatching);
        return row;
    }
}
