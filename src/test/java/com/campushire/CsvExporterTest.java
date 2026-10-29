package com.campushire;

import com.campushire.model.ShortlistEntry;
import com.campushire.model.ShortlistStage;
import com.campushire.util.CsvExporter;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvExporterTest {

    private static final String EXPORT_PATH = System.getProperty("java.io.tmpdir") + "/test_export.csv";

    @AfterEach
    void cleanup() {
        new File(EXPORT_PATH).delete();
    }

    private ShortlistEntry makeEntry(String name, ShortlistStage stage, double score) {
        ShortlistEntry e = new ShortlistEntry(
            "SHL-001", "STU-001", name,
            "DRV-001", "Java Dev @ Acme Corp", "Acme Corp", score);
        e.setStage(stage);
        e.setShortlistedDate(LocalDate.of(2026, 11, 1));
        return e;
    }

    @Test
    void export_createsValidCsv() throws Exception {
        List<ShortlistEntry> entries = List.of(
            makeEntry("Alice Johnson", ShortlistStage.INTERVIEW, 87.5),
            makeEntry("Bob Smith",     ShortlistStage.SELECTED,  92.0)
        );
        CsvExporter.exportShortlist(entries, EXPORT_PATH);

        assertTrue(new File(EXPORT_PATH).exists(), "CSV file should be created");
        List<String> lines = Files.readAllLines(new File(EXPORT_PATH).toPath());
        assertEquals(3, lines.size(), "Header + 2 data rows");
        assertTrue(lines.get(0).startsWith("Entry ID,"), "First line should be header");
        assertTrue(lines.get(1).contains("Alice Johnson"));
        assertTrue(lines.get(1).contains("87.5"));
    }

    @Test
    void export_emptyCsvHasHeaderOnly() throws Exception {
        CsvExporter.exportShortlist(List.of(), EXPORT_PATH);
        List<String> lines = Files.readAllLines(new File(EXPORT_PATH).toPath());
        assertEquals(1, lines.size(), "Only header row for empty list");
    }

    @Test
    void export_escapesCommasInNames() throws Exception {
        ShortlistEntry e = makeEntry("Smith, John Jr.", ShortlistStage.SHORTLISTED, 75.0);
        CsvExporter.exportShortlist(List.of(e), EXPORT_PATH);
        String content = Files.readString(new File(EXPORT_PATH).toPath());
        assertTrue(content.contains("\"Smith, John Jr.\""), "Name with comma should be quoted");
    }
}
