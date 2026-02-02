package com.hackathon.spmguidance.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hackathon.spmguidance.dto.AnalysisResponse;
import com.hackathon.spmguidance.service.ImageAnalysisService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ImageAnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(ImageAnalysisController.class);

    @Autowired
    private ImageAnalysisService imageAnalysisService;

    @PostMapping("/analyze-spm-image")
    public ResponseEntity<AnalysisResponse> analyzeSpmImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "interests", required = false) String interests,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "familyIncome", required = false) String familyIncome) {
        
        try {
            logger.info("Received SPM image analysis request");
            
            if (file.isEmpty()) {
                AnalysisResponse errorResponse = new AnalysisResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("Please upload an image file");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("image/"))) {
                AnalysisResponse errorResponse = new AnalysisResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("Only image files are allowed (JPEG, PNG, etc.)");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (file.getSize() > 5 * 1024 * 1024) {
                AnalysisResponse errorResponse = new AnalysisResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("File size must be less than 5MB");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            logger.info("Processing file: {} (size: {} bytes)", file.getOriginalFilename(), file.getSize());
            
            AnalysisResponse response = imageAnalysisService.analyzeImageAndRecommend(
                file, interests, location, familyIncome
            );
            
            if (response.isSuccess()) {
                logger.info("Image analysis completed successfully");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Image analysis failed: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing image analysis request", e);
            AnalysisResponse errorResponse = new AnalysisResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/test-upload")
    public ResponseEntity<String> testUpload() {
        return ResponseEntity.ok("File upload endpoint is working. Use POST /api/analyze-spm-image with multipart/form-data");
    }
}