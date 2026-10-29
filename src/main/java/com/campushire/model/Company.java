package com.campushire.model;

import java.util.Objects;

/**
 * Represents a recruiting company in the CampusHire system.
 */
public class Company {

    private String id;
    private String name;
    private String industry;
    private String website;
    private String contactEmail;
    private String contactPerson;
    private String description;
    private String logoPath;

    public Company() {}

    public Company(String id, String name, String industry, String contactEmail) {
        this.id = id;
        this.name = name;
        this.industry = industry;
        this.contactEmail = contactEmail;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Company c)) return false;
        return Objects.equals(id, c.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + industry + ")";
    }
}
