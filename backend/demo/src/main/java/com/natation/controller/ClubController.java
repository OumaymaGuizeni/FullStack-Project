package com.natation.controller;

import com.natation.entity.Club;
import com.natation.dto.PredictionDTO;
import com.natation.service.AnalyticsService;
import com.natation.service.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {

    @Autowired
    private ClubService service;

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping
    public List<Club> all() {
        return service.getAll();
    }

    @GetMapping("/analytics/predictions")
    public List<PredictionDTO> getPredictions() {
        List<PredictionDTO> res = analyticsService.getPredictions();
        System.out.println("Sending " + res.size() + " predictions to frontend");
        return res;
    }

    @PostMapping
    public Club create(@RequestBody Club club) {
        return service.save(club);
    }

    @PutMapping("/{id}")
    public Club update(@PathVariable Long id, @RequestBody Club club) {
        return service.update(id, club);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        System.out.println("Received delete request for club ID: " + id);
        service.delete(id);
    }

    @GetMapping("/ranking")
    public List<Club> getRanking() {
        return service.getClubsByRanking();
    }
}