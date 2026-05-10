package com.natation.dto;

public class PredictionDTO {
    private String clubName;
    private int currentTrophies;
    private int predictedTrophies;
    private double growthProbability;
    private String insight;

    public PredictionDTO(String clubName, int currentTrophies, int predictedTrophies, double growthProbability, String insight) {
        this.clubName = clubName;
        this.currentTrophies = currentTrophies;
        this.predictedTrophies = predictedTrophies;
        this.growthProbability = growthProbability;
        this.insight = insight;
    }

    // Getters and Setters
    public String getClubName() { return clubName; }
    public int getCurrentTrophies() { return currentTrophies; }
    public int getPredictedTrophies() { return predictedTrophies; }
    public double getGrowthProbability() { return growthProbability; }
    public String getInsight() { return insight; }
}
