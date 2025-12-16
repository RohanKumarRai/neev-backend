package com.example.demo.repository;

import com.example.demo.model.WorkerProfile;
import com.example.demo.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, Long> {
    List<WorkerProfile> findBySkillCategoryIgnoreCase(String skillCategory);
    List<WorkerProfile> findByLocationIgnoreCase(String location);
    // inside interface WorkerProfileRepository
    List<WorkerProfile> findBySkillCategoryIgnoreCaseContainingAndLocationIgnoreCaseContaining(String skillCategory, String location);

    Optional<WorkerProfile> findByUser(AppUser user);
    Optional<WorkerProfile> findByUserId(Long userId);
    

}
