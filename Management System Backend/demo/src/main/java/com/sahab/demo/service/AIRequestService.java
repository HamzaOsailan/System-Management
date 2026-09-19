package com.sahab.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahab.demo.dto.AIAnalysisDTO;
import com.sahab.demo.dto.AIUserAnalysisDTO;
import com.sahab.demo.entity.AIRequestAnalysis;
import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.RequestStatus;
import com.sahab.demo.repository.AIRequestAnalysisRepository;
import com.sahab.demo.repository.RequestRepository;
import com.sahab.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AIRequestService {

    private static final Logger logger =
            LoggerFactory.getLogger(AIRequestService.class);

    private final AIRequestAnalysisRepository analysisRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final com.sahab.demo.service.ai.OllamaService ollamaService;
    private final com.sahab.demo.service.ai.AIRequestPromptBuilder promptBuilder;
    private final com.sahab.demo.service.ai.AIAnalysisRules analysisRules;
    private final com.sahab.demo.service.ai.AIAnalysisValidator analysisValidator;

    public AIRequestService(
            AIRequestAnalysisRepository analysisRepository,
            RequestRepository requestRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper,
            com.sahab.demo.service.ai.OllamaService ollamaService,
            com.sahab.demo.service.ai.AIRequestPromptBuilder promptBuilder,
            com.sahab.demo.service.ai.AIAnalysisRules analysisRules,
            com.sahab.demo.service.ai.AIAnalysisValidator analysisValidator
    ) {
        this.analysisRepository = analysisRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.ollamaService = ollamaService;
        this.promptBuilder = promptBuilder;
        this.analysisRules = analysisRules;
        this.analysisValidator = analysisValidator;
    }

    // =====================================================
    // ANALYZE REQUEST
    // =====================================================

    public AIRequestAnalysis analyzeRequest(Request request) {

        if (request.getStatus() != RequestStatus.APPROVED) {
            throw new RuntimeException(
                    "AI analysis can only be generated for approved requests"
            );
        }

        var existing =
                analysisRepository.findByRequest(request);

        if (existing.isPresent()) {
            return existing.get();
        }

        logger.info(
                "Starting AI analysis for request {}",
                request.getId()
        );

        String prompt =
                promptBuilder.build(request);

        String aiResponse =
                ollamaService.analyze(prompt);

        AIRequestAnalysis analysis =
                parseResponse(
                        aiResponse,
                        request
                );

        analysisRules.apply(
                analysis,
                request
        );

        analysisValidator.validate(
                analysis
        );

        return analysisRepository.save(
                analysis
        );
    }

    // =====================================================
    // GENERATE FOR ADMIN
    // =====================================================

    public AIAnalysisDTO generateAnalysisForAdmin(
            Long requestId
    ) {

        Request request =
                getRequest(requestId);

        if (request.getStatus() != RequestStatus.APPROVED) {
            throw new RuntimeException(
                    "AI analysis can only be generated for approved requests"
            );
        }

        AIRequestAnalysis analysis =
                analysisRepository.findByRequest(request)
                        .orElseGet(() ->
                                analyzeRequest(request)
                        );

        return toAdminDTO(analysis);
    }

    // =====================================================
    // GET ADMIN ANALYSIS
    // =====================================================

    public AIAnalysisDTO getAdminAnalysis(
            Long requestId
    ) {

        Request request =
                getRequest(requestId);

        AIRequestAnalysis analysis =
                analysisRepository.findByRequest(request)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "AI analysis not found"
                                )
                        );

        return toAdminDTO(analysis);
    }

    // =====================================================
    // GET USER ANALYSIS
    // =====================================================

    public AIUserAnalysisDTO getUserAnalysis(
            Long requestId
    ) {

        User currentUser =
                getCurrentUser();

        Request request =
                getRequest(requestId);

        validateOwnership(
                request,
                currentUser
        );

        if (request.getStatus() != RequestStatus.APPROVED) {
            throw new RuntimeException(
                    "AI analysis is only available for approved requests"
            );
        }

        AIRequestAnalysis analysis =
                analysisRepository.findByRequest(request)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "AI analysis not found"
                                )
                        );

        return toUserDTO(analysis);
    }

    // =====================================================
    // DELETE
    // =====================================================

    public void deleteAnalysis(Long requestId) {

        Request request =
                requestRepository.findById(requestId)
                        .orElse(null);

        if (request != null) {
            analysisRepository.deleteByRequest(request);
        }
    }

    // =====================================================
    // PARSE AI RESPONSE
    // =====================================================

    private AIRequestAnalysis parseResponse(
            String aiResponse,
            Request request
    ) {

        try {

            String json =
                    aiResponse.trim();

            if (json.startsWith("```json")) {
                json = json.substring(7);
            } else if (json.startsWith("```")) {
                json = json.substring(3);
            }

            if (json.endsWith("```")) {
                json = json.substring(
                        0,
                        json.length() - 3
                );
            }

            JsonNode node =
                    objectMapper.readTree(
                            json.trim()
                    );

            AIRequestAnalysis analysis =
                    new AIRequestAnalysis();

            analysis.setRequest(request);

            analysis.setCategory(
                    getText(node, "category")
            );

            analysis.setSubCategory(
                    getText(node, "subCategory")
            );

            analysis.setPriority(
                    getText(node, "priority")
            );

            if (node.hasNonNull("urgency")) {
                analysis.setUrgency(
                        node.get("urgency").asInt()
                );
            }

            analysis.setSummary(
                    getText(node, "summary")
            );

            analysis.setSuggestedDepartment(
                    getText(
                            node,
                            "suggestedDepartment"
                    )
            );

            analysis.setSuggestedAction(
                    getText(
                            node,
                            "suggestedAction"
                    )
            );

            analysis.setReason(
                    getText(node, "reason")
            );

            analysis.setCreatedAt(
                    LocalDateTime.now()
            );

            return analysis;

        } catch (Exception e) {

            logger.error(
                    "Failed to parse AI response",
                    e
            );

            throw new RuntimeException(
                    "Invalid AI JSON response",
                    e
            );
        }
    }

    private String getText(
            JsonNode node,
            String field
    ) {

        JsonNode value =
                node.get(field);

        return value == null || value.isNull()
                ? null
                : value.asText();
    }

    // =====================================================
    // REQUEST
    // =====================================================

    private Request getRequest(Long requestId) {

        return requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Request not found"
                        )
                );
    }

    // =====================================================
    // CURRENT USER
    // =====================================================

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || authentication.getName() == null) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }

    // =====================================================
    // OWNERSHIP
    // =====================================================

    private void validateOwnership(
            Request request,
            User currentUser
    ) {

        if (request.getUser() == null
                || !request.getUser()
                .getId()
                .equals(currentUser.getId())) {

            throw new RuntimeException(
                    "You are not allowed to view this request"
            );
        }
    }

    // =====================================================
    // ADMIN DTO
    // =====================================================

    private AIAnalysisDTO toAdminDTO(
            AIRequestAnalysis analysis
    ) {

        AIAnalysisDTO dto =
                new AIAnalysisDTO();

        dto.setId(
                analysis.getId()
        );

        dto.setRequestId(
                analysis.getRequest().getId()
        );

        dto.setCategory(
                analysis.getCategory()
        );

        dto.setSubCategory(
                analysis.getSubCategory()
        );

        dto.setPriority(
                analysis.getPriority()
        );

        dto.setUrgency(
                analysis.getUrgency()
        );

        dto.setSummary(
                analysis.getSummary()
        );

        dto.setSuggestedDepartment(
                analysis.getSuggestedDepartment()
        );

        dto.setSuggestedAction(
                analysis.getSuggestedAction()
        );

        dto.setReason(
                analysis.getReason()
        );

        dto.setCreatedAt(
                analysis.getCreatedAt()
        );

        return dto;
    }

    // =====================================================
    // USER DTO
    // =====================================================

    private AIUserAnalysisDTO toUserDTO(
            AIRequestAnalysis analysis
    ) {

        Request request =
                analysis.getRequest();

        AIUserAnalysisDTO dto =
                new AIUserAnalysisDTO();

        dto.setRequestId(
                request.getId()
        );

        dto.setRequestTitle(
                request.getTitle()
        );

        dto.setRequestStatus(
                request.getStatus().name()
        );

        dto.setCategory(
                analysis.getCategory()
        );

        dto.setPriority(
                analysis.getPriority()
        );

        dto.setUrgency(
                analysis.getUrgency()
        );

        dto.setSummary(
                analysis.getSummary()
        );

        dto.setSuggestedDepartment(
                analysis.getSuggestedDepartment()
        );

        dto.setSuggestedAction(
                analysis.getSuggestedAction()
        );

        return dto;
    }
}