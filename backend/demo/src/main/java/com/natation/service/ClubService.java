package com.natation.service;

import com.natation.entity.Club;
import com.natation.entity.User;
import com.natation.repository.ClubRepository;
import com.natation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class ClubService {

    @Autowired
    private ClubRepository repo;

    @Autowired
    private UserRepository userRepo;

    public List<Club> getAll() {
        return repo.findAll();
    }

    /**
     * Retrieve a page of clubs with optional sorting.
     *
     * @param page zero‑based page index
     * @param size number of elements per page
     * @param sortBy property to sort by (e.g., "name", "ranking"); if null defaults to id
     * @param direction "ASC" or "DESC"; defaults to ASC
     * @return a Page containing the requested clubs
     */
    public Page<Club> getClubsPage(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction != null ? direction : "ASC"),
                sortBy != null ? sortBy : "id");
        Pageable pageable = PageRequest.of(page, size, sort);
        return repo.findAll(pageable);
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

    public List<Club> getClubsSortedByName() {
        return repo.findAllByOrderByNameAsc();
    }

    public List<User> getAllUsersSortedByUsername() {
        return userRepo.findAllByOrderByUsernameAsc();
    }

    public List<Club> getClubsByRanking() {
        return repo.findAllByOrderByRankingDesc();
    }
}