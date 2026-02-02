package com.hackathon.spmguidance.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.spmguidance.dto.AnalysisResponse;
import com.hackathon.spmguidance.dto.SpmAnalysisRequest;
import com.hackathon.spmguidance.service.RecommendationService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RecommendationController {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);

    @Autowired
    private RecommendationService recommendationService;

    @PostMapping("/analyze-spm-manual")
    public ResponseEntity<AnalysisResponse> analyzeManualInput(@RequestBody SpmAnalysisRequest request) {
        try {
            logger.info("Received manual SPM analysis request");
            
            if (request.getSubjects() == null || request.getSubjects().isEmpty()) {
                AnalysisResponse errorResponse = new AnalysisResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("Please provide at least one subject and grade");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            logger.info("Analyzing {} subjects", request.getSubjects().size());
            
            AnalysisResponse response = recommendationService.analyzeManualInput(request);
            
            if (response.isSuccess()) {
                logger.info("Manual analysis completed successfully");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Manual analysis failed: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing manual analysis request", e);
            AnalysisResponse errorResponse = new AnalysisResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("SPM Guidance Assistant API is running");
    }
}