package com.springbatch.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobLaunchController {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobLaunchController.class);

	@Autowired
	private JobLauncher jobLauncher;
	
	@Autowired
	@Qualifier("firstJob")
	private Job job;

	@GetMapping("/launchJob")
	public ResponseEntity<String> startJob() {

		JobParameters jobParameters = new JobParametersBuilder()
				.addLong("startAt", System.currentTimeMillis()).toJobParameters();

		try {
			LOGGER.info("Before Job run..");
			jobLauncher.run(job, jobParameters);
			LOGGER.info("Job launched successfully with parameters: {}", jobParameters);
			return ResponseEntity.ok("Job launched successfully");
		} catch (JobExecutionException e) {
			LOGGER.error("Error launching job: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error launching Job: " + e.getMessage());
		}
	}
}
