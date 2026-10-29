package com.campushire.app;

import com.campushire.controller.*;
import com.campushire.repository.DataRepository;
import com.campushire.service.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

/**
 * Root layout: fixed dark sidebar on the left, content area on the right.
 * Navigation is handled by swapping the content pane.
 */
public class MainLayout {

    private final BorderPane root;
    private final StackPane  contentArea;

    // Services (shared across controllers)
    private final DataRepository    repository;
    private final AnalyticsService  analyticsService;
    private final RankingEngine     rankingEngine;
    private final ShortlistManager  shortlistManager;

    public MainLayout(DataRepository repository) {
        this.repository       = repository;
        this.analyticsService = new AnalyticsService(repository);
        this.rankingEngine    = new RankingEngine();
        this.shortlistManager = new ShortlistManager(repository);

        root        = new BorderPane();
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");

        VBox sidebar = buildSidebar();
        root.setLeft(sidebar);
        root.setCenter(contentArea);

        // Show dashboard on startup
        navigate("dashboard");
    }

    public BorderPane getRoot() {
        return root;
    }

    // -------------------------------------------------------------------------
    // Sidebar
    // -------------------------------------------------------------------------
    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        // Brand
        VBox brand = new VBox(4);
        brand.getStyleClass().add("sidebar-brand");
        brand.setPadding(new Insets(24, 20, 20, 20));
        Label appName = new Label("CampusHire");
        appName.getStyleClass().add("brand-name");
        Label tagline = new Label("Recruitment Platform");
        tagline.getStyleClass().add("brand-tagline");
        brand.getChildren().addAll(appName, tagline);

        // Navigation items
        VBox nav = new VBox(4);
        nav.getStyleClass().add("nav-group");
        nav.setPadding(new Insets(12, 0, 0, 0));

        NavItem[] items = {
            new NavItem("dashboard",   "⬡",  "Dashboard"),
            new NavItem("candidates",  "◉",  "Candidates"),
            new NavItem("companies",   "◈",  "Companies"),
            new NavItem("drives",      "◆",  "Drives"),
            new NavItem("matching",    "◎",  "Talent Matching"),
            new NavItem("shortlist",   "◐",  "Shortlisting"),
            new NavItem("insights",    "◑",  "Insights"),
        };

        for (NavItem item : items) {
            HBox btn = buildNavButton(item);
            nav.getChildren().add(btn);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label version = new Label("v1.0  ·  Demo Data");
        version.getStyleClass().add("sidebar-footer");
        version.setPadding(new Insets(0, 0, 16, 20));

        sidebar.getChildren().addAll(brand, nav, spacer, version);
        return sidebar;
    }

    private HBox buildNavButton(NavItem item) {
        HBox btn = new HBox(10);
        btn.getStyleClass().add("nav-item");
        btn.setPadding(new Insets(10, 20, 10, 20));

        Label icon  = new Label(item.icon);
        icon.getStyleClass().add("nav-icon");
        Label label = new Label(item.label);
        label.getStyleClass().add("nav-label");

        btn.getChildren().addAll(icon, label);
        btn.setOnMouseClicked(e -> navigate(item.id));
        return btn;
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------
    private void navigate(String section) {
        // Mark active nav item
        root.getLeft().lookupAll(".nav-item").forEach(n -> n.getStyleClass().remove("nav-item-active"));
        root.getLeft().lookupAll(".nav-item").stream()
            .filter(n -> n instanceof HBox)
            .filter(n -> {
                HBox hb = (HBox) n;
                return hb.getChildren().stream()
                    .filter(c -> c instanceof Label)
                    .map(c -> (Label) c)
                    .anyMatch(l -> l.getText().equalsIgnoreCase(labelFor(section)));
            })
            .findFirst()
            .ifPresent(n -> n.getStyleClass().add("nav-item-active"));

        Node view = switch (section) {
            case "dashboard"  -> new DashboardController(repository, analyticsService, this::navigate).getView();
            case "candidates" -> new CandidatesController(repository, this::navigate).getView();
            case "companies"  -> new CompaniesController(repository).getView();
            case "drives"     -> new DrivesController(repository).getView();
            case "matching"   -> new MatchingController(repository, rankingEngine, shortlistManager).getView();
            case "shortlist"  -> new ShortlistController(repository, shortlistManager).getView();
            case "insights"   -> new InsightsController(repository, rankingEngine).getView();
            default           -> new DashboardController(repository, analyticsService, this::navigate).getView();
        };

        contentArea.getChildren().setAll(view);
    }

    private String labelFor(String section) {
        return switch (section) {
            case "dashboard"  -> "Dashboard";
            case "candidates" -> "Candidates";
            case "companies"  -> "Companies";
            case "drives"     -> "Drives";
            case "matching"   -> "Talent Matching";
            case "shortlist"  -> "Shortlisting";
            case "insights"   -> "Insights";
            default -> "";
        };
    }

    // -------------------------------------------------------------------------
    private record NavItem(String id, String icon, String label) {}
}
