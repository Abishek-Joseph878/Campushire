package com.campushire.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable validation helpers.
 */
public final class ValidationUtil {

    private ValidationUtil() {}

    public static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    public static boolean isValidCgpa(double cgpa) {
        return cgpa >= 0.0 && cgpa <= 10.0;
    }

    public static boolean isValidBacklogs(int backlogs) {
        return backlogs >= 0;
    }

    public static List<String> validateStudent(
            String firstName, String lastName, String department,
            String cgpaText, String backlogsText) {

        List<String> errors = new ArrayList<>();
        if (isBlank(firstName))    errors.add("First name is required.");
        if (isBlank(lastName))     errors.add("Last name is required.");
        if (isBlank(department))   errors.add("Department is required.");
        try {
            double cgpa = Double.parseDouble(cgpaText);
            if (!isValidCgpa(cgpa)) errors.add("CGPA must be between 0.0 and 10.0.");
        } catch (NumberFormatException e) {
            errors.add("CGPA must be a valid number.");
        }
        try {
            int bl = Integer.parseInt(backlogsText);
            if (!isValidBacklogs(bl)) errors.add("Active backlogs cannot be negative.");
        } catch (NumberFormatException e) {
            errors.add("Active backlogs must be a whole number.");
        }
        return errors;
    }

    public static List<String> validateDrive(
            String companyId, String jobRole, String minCgpaText,
            String maxBacklogsText) {

        List<String> errors = new ArrayList<>();
        if (isBlank(companyId)) errors.add("A company must be selected.");
        if (isBlank(jobRole))   errors.add("Job role is required.");
        try {
            double cgpa = Double.parseDouble(minCgpaText);
            if (!isValidCgpa(cgpa)) errors.add("Minimum CGPA must be between 0.0 and 10.0.");
        } catch (NumberFormatException e) {
            errors.add("Minimum CGPA must be a valid number.");
        }
        try {
            int bl = Integer.parseInt(maxBacklogsText);
            if (!isValidBacklogs(bl)) errors.add("Maximum backlogs cannot be negative.");
        } catch (NumberFormatException e) {
            errors.add("Maximum backlogs must be a whole number.");
        }
        return errors;
    }
}
