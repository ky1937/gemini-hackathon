package com.hackathon.spmguidance.dto;

public class Recommendation {
    private String name;
    private String type;
    private String description;
    private String matchScore;
    private String reasoning;

    public Recommendation() {}

    public Recommendation(String name, String type, String description, String matchScore, String reasoning) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.matchScore = matchScore;
        this.reasoning = reasoning;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(String matchScore) {
        this.matchScore = matchScore;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }
}