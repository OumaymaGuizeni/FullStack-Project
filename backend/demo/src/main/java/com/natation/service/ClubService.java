package com.natation.service;

import com.natation.entity.Club;
import com.natation.entity.User;
import com.natation.repository.ClubRepository;
import com.natation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class ClubService {

    @Autowired
    private ClubRepository repo;

    @Autowired
    private UserRepository userRepo;

    public List<Club> getAll() {
        return repo.findAll();
    }

    public Club save(Club club) {
        return repo.save(club);
    }

    public Club update(Long id, Club updatedClub) {
        return repo.findById(id).map(club -> {
            System.out.println("Updating club ID " + id + " with trophies: " + updatedClub.getTrophies());
            club.setName(updatedClub.getName());
            club.setCity(updatedClub.getCity());
            club.setRanking(updatedClub.getRanking());
            club.setTrophies(updatedClub.getTrophies());
            return repo.save(club);
        }).orElseThrow(() -> new RuntimeException("Club not found"));
    }

    public void delete(Long id) {
        repo.findById(id).ifPresent(club -> {
            List<User> users = userRepo.findAllByClub(club);
            for (User user : users) {
                user.setClub(null);
                userRepo.save(user);
            }
            repo.delete(club);
        });
    }

    public List<Club> getClubsByRanking() {
        return repo.findAllByOrderByRankingDesc();
    }
}