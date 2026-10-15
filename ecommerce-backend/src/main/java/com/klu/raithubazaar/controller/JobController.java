package com.klu.raithubazaar.controller;

import com.klu.raithubazaar.model.BackgroundJob;
import com.klu.raithubazaar.service.BackgroundJobService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final BackgroundJobService jobs;
    public JobController(BackgroundJobService jobs) { this.jobs = jobs; }
    @GetMapping
    public List<BackgroundJob> list() { return jobs.findAll(); }
}
