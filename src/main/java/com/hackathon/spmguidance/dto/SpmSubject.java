package com.hackathon.spmguidance.dto;

public class SpmSubject {
    private String name;
    private String grade;

    public SpmSubject() {}

    public SpmSubject(String name, String grade) {
        this.name = name;
        this.grade = grade;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}