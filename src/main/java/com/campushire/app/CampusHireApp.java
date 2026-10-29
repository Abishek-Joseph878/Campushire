package com.campushire.app;

import com.campushire.repository.DataRepository;
import com.campushire.repository.SqliteDataRepository;
import com.campushire.service.SampleDataSeeder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Application entry point for CampusHire.
 * Initialises the database, seeds demo data, and launches the main window.
 */
public class CampusHireApp extends Application {

    // Shared application-wide repository instance
    private static DataRepository repository;

    @Override
    public void start(Stage primaryStage) {
        try {
            // --- Bootstrap persistence ---
            repository = new SqliteDataRepository();
            repository.initialize();
            new SampleDataSeeder(repository).seedIfEmpty();

            // --- Build main scene ---
            MainLayout mainLayout = new MainLayout(repository);
            Scene scene = new Scene(mainLayout.getRoot(), 1280, 800);
            scene.getStylesheets().add(
                getClass().getResource("/com/campushire/css/theme.css").toExternalForm());

            primaryStage.setTitle("CampusHire — Campus Recruitment Platform");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(680);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Fatal startup error: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        if (repository != null) {
            repository.shutdown();
        }
    }

    public static DataRepository getRepository() {
        return repository;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
