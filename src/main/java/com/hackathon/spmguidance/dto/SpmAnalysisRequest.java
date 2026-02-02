package com.hackathon.spmguidance.dto;

import java.util.List;

public class SpmAnalysisRequest {
    private List<SpmSubject> subjects;
    private String interests;
    private String location;
    private String familyIncome;

    public List<SpmSubject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<SpmSubject> subjects) {
        this.subjects = subjects;
    }

    public String getInterests() {
        return interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFamilyIncome() {
        return familyIncome;
    }

    public void setFamilyIncome(String familyIncome) {
        this.familyIncome = familyIncome;
    }
}