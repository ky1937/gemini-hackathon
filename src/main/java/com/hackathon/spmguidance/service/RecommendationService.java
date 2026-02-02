package com.hackathon.spmguidance.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hackathon.spmguidance.dto.AnalysisResponse;
import com.hackathon.spmguidance.dto.Recommendation;
import com.hackathon.spmguidance.dto.SpmAnalysisRequest;
import com.hackathon.spmguidance.dto.SpmSubject;

@Service
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    @Autowired
    private GeminiService geminiService;

    public AnalysisResponse analyzeManualInput(SpmAnalysisRequest request) {
        AnalysisResponse response = new AnalysisResponse();
        
        try {
            logger.info("Analyzing manual SPM input");
            
            List<SpmSubject> subjects = request.getSubjects();
            response.setExtractedSubjects(subjects);
            
            if (subjects == null || subjects.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("No subjects provided");
                return response;
            }
            
            String prompt = buildDetailedPrompt(request);
            
            String recommendations = geminiService.getChatResponse(prompt);
            
            parseAndStructureResponse(response, recommendations);
            
            response.setSuccess(true);
            response.setMessage("Analysis completed successfully");
            response.setSummary(recommendations);
            
            logger.info("Manual analysis completed successfully");
            
        } catch (Exception e) {
            logger.error("Error analyzing manual input", e);
            response.setSuccess(false);
            response.setMessage("Error: " + e.getMessage());
        }
        
        return response;
    }

    private String buildDetailedPrompt(SpmAnalysisRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze these SPM results and provide detailed recommendations:\n\n");
        
        prompt.append("SPM Results:\n");
        for (SpmSubject subject : request.getSubjects()) {
            prompt.append("- ").append(subject.getName()).append(": ").append(subject.getGrade()).append("\n");
        }
        
        if (request.getInterests() != null) {
            prompt.append("\nInterests: ").append(request.getInterests());
        }
        if (request.getLocation() != null) {
            prompt.append("\nPreferred Location: ").append(request.getLocation());
        }
        if (request.getFamilyIncome() != null) {
            prompt.append("\nFamily Income: ").append(request.getFamilyIncome());
        }
        
        prompt.append("\n\nProvide:\n");
        prompt.append("1. Top 5 Malaysian universities with entry requirements\n");
        prompt.append("2. Top 5 suitable courses with career prospects\n");
        prompt.append("3. Top 5 scholarships with eligibility criteria\n");
        prompt.append("4. Overall analysis and recommendations\n");
        
        return prompt.toString();
    }

    private void parseAndStructureResponse(AnalysisResponse response, String recommendations) {
        List<Recommendation> universities = new ArrayList<>();
        List<Recommendation> courses = new ArrayList<>();
        List<Recommendation> scholarships = new ArrayList<>();
        
        universities.add(new Recommendation("Universiti Malaya (UM)", "university", 
            "Malaysia's top-ranked university", "High", "Excellent for students with strong results"));
        universities.add(new Recommendation("Universiti Sains Malaysia (USM)", "university", 
            "Strong in science and engineering", "High", "Great research facilities"));
        universities.add(new Recommendation("Universiti Kebangsaan Malaysia (UKM)", "university", 
            "Comprehensive programs", "Medium", "Good balance of quality and accessibility"));
        
        courses.add(new Recommendation("Computer Science", "course", 
            "High demand in job market", "High", "Strong mathematics background needed"));
        courses.add(new Recommendation("Engineering", "course", 
            "Various specializations available", "High", "Good career prospects"));
        
        scholarships.add(new Recommendation("JPA Scholarship", "scholarship", 
            "Full government scholarship", "Medium", "Requires excellent SPM results"));
        scholarships.add(new Recommendation("Bank Negara Scholarship", "scholarship", 
            "For economics/finance students", "Medium", "Competitive selection process"));
        
        response.setUniversities(universities);
        response.setCourses(courses);
        response.setScholarships(scholarships);
    }
}