package com.campushire.util;

import com.campushire.model.ShortlistEntry;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Exports shortlist data to CSV format.
 */
public final class CsvExporter {

    private CsvExporter() {}

    /**
     * Exports a list of shortlist entries to the specified file path.
     *
     * @param entries  list of entries to export
     * @param filePath target file path (e.g. "shortlist_export.csv")
     * @throws IOException if the file cannot be written
     */
    public static void exportShortlist(List<ShortlistEntry> entries, String filePath) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            // Header
            pw.println("Entry ID,Student ID,Student Name,Drive,Company,Stage,Match Score,Shortlisted Date,Notes");

            for (ShortlistEntry e : entries) {
                pw.printf("%s,%s,%s,%s,%s,%s,%.1f,%s,%s%n",
                        escape(e.getId()),
                        escape(e.getStudentId()),
                        escape(e.getStudentName()),
                        escape(e.getDriveName()),
                        escape(e.getCompanyName()),
                        escape(e.getStage().getDisplayName()),
                        e.getMatchScore(),
                        e.getShortlistedDate() != null ? e.getShortlistedDate().toString() : "",
                        escape(e.getNotes() != null ? e.getNotes() : ""));
            }
        }
    }

    /** Wraps a field in quotes if it contains commas or quotes. */
    private static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
