package com.klu.raithubazaar.repository;

import com.klu.raithubazaar.model.BackgroundJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BackgroundJobRepository extends JpaRepository<BackgroundJob, Long> {
    List<BackgroundJob> findAllByOrderByCreatedAtDesc();
}
