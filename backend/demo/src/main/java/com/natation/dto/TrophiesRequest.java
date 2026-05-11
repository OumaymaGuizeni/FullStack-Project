package com.natation.dto;

public class TrophiesRequest {
    private Integer trophies;

    public TrophiesRequest() {
    }

    public TrophiesRequest(Integer trophies) {
        this.trophies = trophies;
    }

    public Integer getTrophies() {
        return trophies;
    }

    public void setTrophies(Integer trophies) {
        this.trophies = trophies;
    }
}
