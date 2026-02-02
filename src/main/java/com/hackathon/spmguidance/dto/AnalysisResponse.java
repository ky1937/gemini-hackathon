package com.hackathon.spmguidance.dto;

import java.util.List;

public class AnalysisResponse {
    private boolean success;
    private String message;
    private List<SpmSubject> extractedSubjects;
    private List<Recommendation> universities;
    private List<Recommendation> courses;
    private List<Recommendation> scholarships;
    private String summary;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<SpmSubject> getExtractedSubjects() {
        return extractedSubjects;
    }

    public void setExtractedSubjects(List<SpmSubject> extractedSubjects) {
        this.extractedSubjects = extractedSubjects;
    }

    public List<Recommendation> getUniversities() {
        return universities;
    }

    public void setUniversities(List<Recommendation> universities) {
        this.universities = universities;
    }

    public List<Recommendation> getCourses() {
        return courses;
    }

    public void setCourses(List<Recommendation> courses) {
        this.courses = courses;
    }

    public List<Recommendation> getScholarships() {
        return scholarships;
    }

    public void setScholarships(List<Recommendation> scholarships) {
        this.scholarships = scholarships;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}