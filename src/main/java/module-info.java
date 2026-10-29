module com.campushire {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires com.google.gson;

    opens com.campushire.app to javafx.graphics, javafx.fxml;
    opens com.campushire.controller to javafx.fxml;
    opens com.campushire.model to com.google.gson;

    exports com.campushire.app;
    exports com.campushire.model;
    exports com.campushire.service;
    exports com.campushire.repository;
    exports com.campushire.controller;
    exports com.campushire.util;
}