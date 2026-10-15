package com.klu.raithubazaar.service;

import com.klu.raithubazaar.model.BackgroundJob;
import com.klu.raithubazaar.repository.BackgroundJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.List;

@Service
public class BackgroundJobService {
    private final BackgroundJobRepository jobs;
    private final JobNotificationService notifier;
    private final JobRunner runner;
    public BackgroundJobService(BackgroundJobRepository jobs, JobNotificationService notifier, JobRunner runner) {
        this.jobs = jobs;
        this.notifier = notifier;
        this.runner = runner;
    }

    @Transactional
    public void queueForOrder(Long orderId) {
        List<BackgroundJob> created = List.of("EMAIL", "INVOICE", "NOTIFICATION").stream().map(type -> {
            BackgroundJob job = new BackgroundJob();
            job.setOrderId(orderId);
            job.setJobType(type);
            job.setStatus("PENDING");
            job.setMessage("Queued");
            return jobs.save(job);
        }).toList();
        created.forEach(notifier::publish);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                created.forEach(job -> runner.execute(job.getJobId()));
            }
        });
    }

    public List<BackgroundJob> findAll() { return jobs.findAllByOrderByCreatedAtDesc(); }
}
