package com.sahab.demo.modules.ai.service;
import com.sahab.demo.modules.hr.service.AttendanceAdminService;
import com.sahab.demo.modules.hr.service.LeaveBalanceService;

import com.sahab.demo.modules.requests.service.AIRequestService;


import com.sahab.demo.modules.ai.dto.ChatResponseDTO;
import com.sahab.demo.modules.hr.dto.LeaveBalanceDTO;
import com.sahab.demo.entity.Attendance;
import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.User;
import com.sahab.demo.modules.ai.enums.ChatIntent;
import com.sahab.demo.enums.RequestStatus;
import com.sahab.demo.enums.Role;
import com.sahab.demo.modules.hr.repository.AttendanceRepository;
import com.sahab.demo.modules.requests.repository.RequestRepository;
import com.sahab.demo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ChatService {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final AttendanceRepository attendanceRepository;
    private final OllamaService ollamaService;
    private final ChatIntentDetector intentDetector;
    private final AttendanceAdminService attendanceAdminService;
    private final AIRequestService aiRequestService;
    private final LeaveBalanceService leaveBalanceService;

    public ChatService(
            UserRepository userRepository,
            RequestRepository requestRepository,
            AttendanceRepository attendanceRepository,
            OllamaService ollamaService,
            ChatIntentDetector intentDetector,
            AttendanceAdminService attendanceAdminService,
            AIRequestService aiRequestService,
            LeaveBalanceService leaveBalanceService
    ) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.attendanceRepository = attendanceRepository;
        this.ollamaService = ollamaService;
        this.intentDetector = intentDetector;
        this.attendanceAdminService = attendanceAdminService;
        this.aiRequestService = aiRequestService;
        this.leaveBalanceService = leaveBalanceService;
    }

    // =====================================================
    // MAIN CHAT
    // =====================================================

    public ChatResponseDTO chat(String message) {

        User currentUser = getCurrentUser();

        ChatIntent intent =
                intentDetector.detect(message);

        System.out.println();
        System.out.println("========== CHAT SERVICE ==========");
        System.out.println("User: " + currentUser.getEmail());
        System.out.println("Message: " + message);
        System.out.println("Intent: " + intent);
        System.out.println("==================================");

        /*
         * Requests are handled directly by the backend.
         *
         * This avoids sending all user requests to Ollama.
         */
        if (intent == ChatIntent.REQUESTS) {

            String answer =
                    answerRequestsQuestion(
                            message,
                            currentUser
                    );

            return new ChatResponseDTO(
                    cleanResponse(answer)
            );
        }

        /*
         * Request details are also handled directly.
         *
         * The backend already knows the exact request.
         */
        if (intent == ChatIntent.REQUEST_DETAILS) {

            String answer =
                    answerRequestDetails(
                            message,
                            currentUser
                    );

            return new ChatResponseDTO(
                    cleanResponse(answer)
            );
        }

        /*
         * Everything else keeps using Ollama.
         */

        String context =
                buildContext(
                        intent,
                        message,
                        currentUser
                );

        String systemPrompt =
                """
                You are Sahab Assistant.

                You are an AI assistant inside the Sahab
                company management system.

                The backend has already determined the intent
                of the user's question and provided the relevant
                system data.

                RULES:

                1. Answer using the provided data.
                2. Never invent company information.
                3. Never invent dates, statuses, attendance,
                   request information, leave balances,
                   remote work balances, or employee information.
                4. If the provided data does not contain the
                   answer, clearly say that the information
                   is not available.
                5. Reply in the same language used by the user.
                6. Arabic questions should receive Arabic answers.
                7. English questions should receive English answers.
                8. Be concise and professional.
                9. Do not expose database, JWT, repository,
                   SecurityContext, or internal implementation details.
                10. Do not claim that an action was performed
                    unless the backend actually performed it.
                11. Treat the supplied system data as factual
                    context and do not fabricate missing values.
                12. For leave balance questions, clearly distinguish
                    annual leave from remote work.
                13. If the user asks how many days remain, use
                    the corresponding "remaining" value.
                14. If the user asks how many days were used, use
                    the corresponding "used" value.
                15. Do not calculate or change the supplied balance
                    values unless necessary for explaining them.

                Current intent:
                """ + intent + """

                System data:
                """ + context;

        try {

            String answer =
                    ollamaService.chat(
                            systemPrompt,
                            message
                    );

            return new ChatResponseDTO(
                    cleanResponse(answer)
            );

        } catch (Exception e) {

            System.err.println(
                    "========== CHAT SERVICE ERROR =========="
            );

            e.printStackTrace();

            return new ChatResponseDTO(
                    "تعذر الاتصال بمساعد الذكاء الاصطناعي حاليًا. حاول مرة أخرى."
            );
        }
    }

    // =====================================================
    // REQUEST QUESTIONS
    // =====================================================

    private String answerRequestsQuestion(
            String message,
            User user
    ) {

        List<Request> requests =
                getCurrentUserRequests(user);

        if (requests.isEmpty()) {

            return """
                    لا توجد لديك أي طلبات حاليًا.
                    """;
        }

        String text =
                message == null
                        ? ""
                        : message.toLowerCase(Locale.ROOT);

        // -------------------------------------------------
        // Pending
        // -------------------------------------------------

        if (containsAny(
                text,
                "pending",
                "معلق",
                "معلقة",
                "المعلق",
                "المعلقة",
                "قيد الانتظار",
                "قيد المراجعة"
        )) {

            return buildRequestsByStatusResponse(
                    requests,
                    RequestStatus.PENDING,
                    "الطلبات المعلقة"
            );
        }

        // -------------------------------------------------
        // Approved
        // -------------------------------------------------

        if (containsAny(
                text,
                "approved",
                "approve",
                "مقبول",
                "مقبولة",
                "المقبول",
                "المقبولة",
                "تم قبول"
        )) {

            return buildRequestsByStatusResponse(
                    requests,
                    RequestStatus.APPROVED,
                    "الطلبات المقبولة"
            );
        }

        // -------------------------------------------------
        // Rejected
        // -------------------------------------------------

        if (containsAny(
                text,
                "rejected",
                "reject",
                "مرفوض",
                "مرفوضة",
                "المرفوض",
                "المرفوضة",
                "تم رفض"
        )) {

            return buildRequestsByStatusResponse(
                    requests,
                    RequestStatus.REJECTED,
                    "الطلبات المرفوضة"
            );
        }

        // -------------------------------------------------
        // Count questions
        // -------------------------------------------------

        if (containsAny(
                text,
                "how many",
                "how much",
                "count",
                "number of",
                "كم طلب",
                "كم عندي",
                "عدد الطلبات",
                "عدد طلباتي"
        )) {

            return buildRequestsSummaryResponse(
                    requests
            );
        }

        // -------------------------------------------------
        // Default:
        // "What's the status of my requests?"
        // -------------------------------------------------

        return buildRequestsSummaryResponse(
                requests
        );
    }

    // =====================================================
    // GET CURRENT USER REQUESTS
    // =====================================================

    private List<Request> getCurrentUserRequests(
            User user
    ) {

        /*
         * We still use findAll() because this method is already
         * available in the current repository.
         *
         * However, unlike the old implementation, we do not
         * build a huge context and send it to Ollama.
         *
         * Only the current user's requests are retained.
         */

        return requestRepository
                .findAll()
                .stream()
                .filter(request ->
                        request.getUser() != null
                                && request.getUser()
                                .getId()
                                .equals(user.getId())
                )
                .sorted(
                        Comparator.comparing(
                                Request::getCreatedAt,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                )
                .toList();
    }

    // =====================================================
    // REQUEST SUMMARY
    // =====================================================

    private String buildRequestsSummaryResponse(
            List<Request> requests
    ) {

        long pending =
                countByStatus(
                        requests,
                        RequestStatus.PENDING
                );

        long approved =
                countByStatus(
                        requests,
                        RequestStatus.APPROVED
                );

        long rejected =
                countByStatus(
                        requests,
                        RequestStatus.REJECTED
                );

        StringBuilder response =
                new StringBuilder();

        response.append(
                "لديك "
        ).append(
                requests.size()
        ).append(
                " طلب"
        );

        if (requests.size() != 1) {
            response.append("ات");
        }

        response.append(" حاليًا:\n\n");

        response.append(
                "• المعلقة: "
        ).append(
                pending
        ).append("\n");

        response.append(
                "• المقبولة: "
        ).append(
                approved
        ).append("\n");

        response.append(
                "• المرفوضة: "
        ).append(
                rejected
        );

        return response.toString();
    }

    // =====================================================
    // REQUESTS BY STATUS
    // =====================================================

    private String buildRequestsByStatusResponse(
            List<Request> requests,
            RequestStatus status,
            String title
    ) {

        List<Request> filtered =
                requests.stream()
                        .filter(request ->
                                request.getStatus() == status
                        )
                        .toList();

        if (filtered.isEmpty()) {

            return "لا توجد لديك طلبات " +
                    arabicStatus(status) +
                    " حاليًا.";
        }

        StringBuilder response =
                new StringBuilder();

        response.append(title)
                .append(": ")
                .append(filtered.size())
                .append("\n\n");

        for (Request request : filtered) {

            response.append("• ");

            response.append(
                    safe(request.getTitle())
            );

            response.append(
                    " — رقم الطلب: "
            );

            response.append(
                    request.getId()
            );

            if (request.getCategory() != null) {

                response.append(
                        " — الفئة: "
                );

                response.append(
                        request.getCategory()
                );
            }

            if (request.getPriority() != null) {

                response.append(
                        " — الأولوية: "
                );

                response.append(
                        request.getPriority()
                );
            }

            response.append("\n");
        }

        return response.toString().trim();
    }

    // =====================================================
    // REQUEST DETAILS
    // =====================================================

    private String answerRequestDetails(
            String message,
            User user
    ) {

        Long requestId =
                intentDetector.extractRequestId(
                        message
                );

        if (requestId == null) {

            return """
                    لم أتمكن من تحديد رقم الطلب.
                    اكتب مثلًا: وش حالة طلب رقم 10؟
                    """;
        }

        Request request =
                requestRepository
                        .findById(requestId)
                        .orElse(null);

        if (request == null) {

            return "لم يتم العثور على الطلب رقم "
                    + requestId
                    + ".";
        }

        if (request.getUser() == null
                || !request.getUser()
                .getId()
                .equals(user.getId())) {

            return "لا يمكنك عرض تفاصيل هذا الطلب.";
        }

        StringBuilder response =
                new StringBuilder();

        response.append(
                "تفاصيل الطلب رقم "
        ).append(
                request.getId()
        ).append(
                ":\n\n"
        );

        response.append(
                "• العنوان: "
        ).append(
                safe(request.getTitle())
        ).append("\n");

        response.append(
                "• الحالة: "
        ).append(
                arabicStatus(
                        request.getStatus()
                )
        ).append("\n");

        response.append(
                "• الفئة: "
        ).append(
                safe(request.getCategory())
        ).append("\n");

        response.append(
                "• الأولوية: "
        ).append(
                safe(request.getPriority())
        ).append("\n");

        response.append(
                "• المدة: "
        ).append(
                safe(request.getDurationDays())
        ).append(
                " يوم\n"
        );

        response.append(
                "• تاريخ الإنشاء: "
        ).append(
                safe(request.getCreatedAt())
        ).append("\n");

        response.append(
                "• الوصف: "
        ).append(
                safe(request.getDescription())
        );

        return response.toString();
    }

    // =====================================================
    // STATUS COUNT
    // =====================================================

    private long countByStatus(
            List<Request> requests,
            RequestStatus status
    ) {

        return requests.stream()
                .filter(request ->
                        request.getStatus() == status
                )
                .count();
    }

    // =====================================================
    // ARABIC STATUS
    // =====================================================

    private String arabicStatus(
            RequestStatus status
    ) {

        if (status == null) {
            return "غير معروفة";
        }

        return switch (status) {

            case PENDING ->
                    "المعلقة";

            case APPROVED ->
                    "المقبولة";

            case REJECTED ->
                    "المرفوضة";
        };
    }

    // =====================================================
    // BUILD CONTEXT
    // =====================================================

    private String buildContext(
            ChatIntent intent,
            String message,
            User user
    ) {

        return switch (intent) {

            case PROFILE ->
                    buildProfileContext(user);

            case ATTENDANCE ->
                    buildAttendanceContext(user);

            case REQUESTS ->
                    buildRequestsContext(user);

            case REQUEST_DETAILS ->
                    buildRequestDetailsContext(
                            message,
                            user
                    );

            case AI_ANALYSIS ->
                    buildAIAnalysisContext(
                            message,
                            user
                    );

            case HR_ATTENDANCE ->
                    buildHRAttendanceContext(user);

            case LEAVE ->
                    buildLeaveBalanceContext();

            case GENERAL ->
                    buildGeneralContext(user);
        };
    }

    // =====================================================
    // PROFILE
    // =====================================================

    private String buildProfileContext(
            User user
    ) {

        return """
                USER PROFILE
                --------------------
                ID: %s
                Name: %s
                Email: %s
                Role: %s
                """.formatted(
                user.getId(),
                safe(user.getName()),
                safe(user.getEmail()),
                safe(user.getRole())
        );
    }

    // =====================================================
    // PERSONAL ATTENDANCE
    // =====================================================

    private String buildAttendanceContext(
            User user
    ) {

        LocalDate today =
                LocalDate.now();

        StringBuilder context =
                new StringBuilder();

        context.append(
                "PERSONAL ATTENDANCE\n"
        );

        context.append(
                "--------------------\n"
        );

        context.append(
                "Date: "
        ).append(
                today
        ).append("\n");

        attendanceRepository
                .findByUserAndDate(
                        user,
                        today
                )
                .ifPresentOrElse(
                        attendance ->
                                appendAttendance(
                                        context,
                                        attendance
                                ),
                        () ->
                                context.append(
                                        "No attendance record exists for today.\n"
                                )
                );

        return context.toString();
    }

    // =====================================================
    // LEAVE BALANCE
    // =====================================================

    private String buildLeaveBalanceContext() {

        LeaveBalanceDTO balance;

        try {

            balance =
                    leaveBalanceService
                            .getCurrentUserBalance();

        } catch (Exception e) {

            return """
                    LEAVE BALANCE
                    --------------------
                    Leave balance information is not available.
                    """;
        }

        StringBuilder context =
                new StringBuilder();

        context.append(
                "CURRENT USER LEAVE BALANCE\n"
        );

        context.append(
                "--------------------\n"
        );

        context.append(
                "Year: "
        ).append(
                balance.getCurrentYear()
        ).append("\n");

        context.append(
                "Month: "
        ).append(
                balance.getCurrentMonth()
        ).append("\n\n");

        context.append(
                "ANNUAL LEAVE\n"
        );

        context.append(
                "Annual Leave Limit: "
        ).append(
                balance.getAnnualLeaveLimit()
        ).append(" days\n");

        context.append(
                "Annual Leave Used: "
        ).append(
                balance.getAnnualLeaveUsed()
        ).append(" days\n");

        context.append(
                "Annual Leave Remaining: "
        ).append(
                balance.getAnnualLeaveRemaining()
        ).append(" days\n");

        context.append(
                "Annual Leave Usage Percentage: "
        ).append(
                balance.getAnnualLeavePercentage()
        ).append("%\n\n");

        context.append(
                "REMOTE WORK\n"
        );

        context.append(
                "Remote Work Monthly Limit: "
        ).append(
                balance.getRemoteWorkMonthlyLimit()
        ).append(" days\n");

        context.append(
                "Remote Work Used This Month: "
        ).append(
                balance.getRemoteWorkUsed()
        ).append(" days\n");

        context.append(
                "Remote Work Remaining This Month: "
        ).append(
                balance.getRemoteWorkRemaining()
        ).append(" days\n");

        context.append(
                "Remote Work Usage Percentage: "
        ).append(
                balance.getRemoteWorkPercentage()
        ).append("%\n");

        return context.toString();
    }

    // =====================================================
    // REQUEST CONTEXT
    // =====================================================

    private String buildRequestsContext(
            User user
    ) {

        List<Request> requests =
                getCurrentUserRequests(user);

        StringBuilder context =
                new StringBuilder();

        context.append(
                "MY REQUESTS\n"
        );

        context.append(
                "--------------------\n"
        );

        context.append(
                "Total requests: "
        ).append(
                requests.size()
        ).append("\n");

        for (Request request : requests) {

            appendRequest(
                    context,
                    request
            );
        }

        return context.toString();
    }

    // =====================================================
    // REQUEST DETAILS CONTEXT
    // =====================================================

    private String buildRequestDetailsContext(
            String message,
            User user
    ) {

        Long requestId =
                intentDetector.extractRequestId(
                        message
                );

        if (requestId == null) {

            return """
                    REQUEST DETAILS
                    --------------------
                    No request ID was detected.
                    """;
        }

        Request request =
                requestRepository.findById(
                        requestId
                ).orElse(null);

        if (request == null) {

            return """
                    REQUEST DETAILS
                    --------------------
                    Request not found.
                    """;
        }

        if (request.getUser() == null
                || !request.getUser()
                .getId()
                .equals(user.getId())) {

            return """
                    REQUEST DETAILS
                    --------------------
                    This request does not belong
                    to the current user.
                    """;
        }

        StringBuilder context =
                new StringBuilder();

        context.append(
                "REQUEST DETAILS\n"
        );

        context.append(
                "--------------------\n"
        );

        appendRequest(
                context,
                request
        );

        return context.toString();
    }

    // =====================================================
    // AI ANALYSIS
    // =====================================================

    private String buildAIAnalysisContext(
            String message,
            User user
    ) {

        Long requestId =
                intentDetector.extractRequestId(
                        message
                );

        if (requestId == null) {

            return """
                    AI ANALYSIS
                    --------------------
                    No request ID was detected.
                    """;
        }

        Request request =
                requestRepository.findById(
                        requestId
                ).orElse(null);

        if (request == null) {

            return """
                    AI ANALYSIS
                    --------------------
                    Request not found.
                    """;
        }

        if (request.getUser() == null
                || !request.getUser()
                .getId()
                .equals(user.getId())) {

            return """
                    AI ANALYSIS
                    --------------------
                    This request does not belong
                    to the current user.
                    """;
        }

        if (request.getStatus() == null
                || !request.getStatus()
                .name()
                .equals("APPROVED")) {

            return """
                    AI ANALYSIS
                    --------------------
                    AI analysis is only available
                    for approved requests.
                    """;
        }

        try {

            var analysis =
                    aiRequestService.getUserAnalysis(
                            requestId
                    );

            StringBuilder context =
                    new StringBuilder();

            context.append(
                    "AI ANALYSIS FOR REQUEST "
            ).append(
                    requestId
            ).append("\n");

            context.append(
                    "--------------------\n"
            );

            context.append(
                    "Request title: "
            ).append(
                    safe(analysis.getRequestTitle())
            ).append("\n");

            context.append(
                    "Status: "
            ).append(
                    safe(analysis.getRequestStatus())
            ).append("\n");

            context.append(
                    "Category: "
            ).append(
                    safe(analysis.getCategory())
            ).append("\n");

            context.append(
                    "Priority: "
            ).append(
                    safe(analysis.getPriority())
            ).append("\n");

            context.append(
                    "Urgency: "
            ).append(
                    safe(analysis.getUrgency())
            ).append("\n");

            context.append(
                    "Summary: "
            ).append(
                    safe(analysis.getSummary())
            ).append("\n");

            context.append(
                    "Suggested Department: "
            ).append(
                    safe(analysis.getSuggestedDepartment())
            ).append("\n");

            context.append(
                    "Suggested Action: "
            ).append(
                    safe(analysis.getSuggestedAction())
            ).append("\n");

            return context.toString();

        } catch (Exception e) {

            return """
                    AI ANALYSIS
                    --------------------
                    AI analysis is not available
                    for this request.
                    """;
        }
    }

    // =====================================================
    // HR ATTENDANCE
    // =====================================================

    private String buildHRAttendanceContext(
            User user
    ) {

        if (!isAdminOrManager(user)) {

            return """
                    ACCESS
                    --------------------
                    The current user does not have permission
                    to view employee attendance information.
                    """;
        }

        var records =
                attendanceAdminService
                        .getTodayAttendance();

        StringBuilder context =
                new StringBuilder();

        context.append(
                "EMPLOYEE ATTENDANCE TODAY\n"
        );

        context.append(
                "--------------------\n"
        );

        context.append(
                "Date: "
        ).append(
                LocalDate.now()
        ).append("\n");

        context.append(
                "Total attendance records: "
        ).append(
                records.size()
        ).append("\n\n");

        for (var record : records) {

            context.append(
                    "Employee: "
            ).append(
                    safe(record.getEmployeeName())
            ).append("\n");

            context.append(
                    "Email: "
            ).append(
                    safe(record.getEmployeeEmail())
            ).append("\n");

            context.append(
                    "Status: "
            ).append(
                    safe(record.getStatus())
            ).append("\n");

            context.append(
                    "Check In: "
            ).append(
                    safe(record.getCheckIn())
            ).append("\n");

            context.append(
                    "Check Out: "
            ).append(
                    safe(record.getCheckOut())
            ).append("\n");

            context.append(
                    "--------------------\n"
            );
        }

        return context.toString();
    }

    // =====================================================
    // GENERAL
    // =====================================================

    private String buildGeneralContext(
            User user
    ) {

        return """
                CURRENT USER
                --------------------
                Name: %s
                Role: %s
                Email: %s
                """.formatted(
                safe(user.getName()),
                safe(user.getRole()),
                safe(user.getEmail())
        );
    }

    // =====================================================
    // REQUEST FORMAT
    // =====================================================

    private void appendRequest(
            StringBuilder context,
            Request request
    ) {

        context.append(
                "Request ID: "
        ).append(
                request.getId()
        ).append("\n");

        context.append(
                "Title: "
        ).append(
                safe(request.getTitle())
        ).append("\n");

        context.append(
                "Description: "
        ).append(
                safe(request.getDescription())
        ).append("\n");

        context.append(
                "Category: "
        ).append(
                safe(request.getCategory())
        ).append("\n");

        context.append(
                "Status: "
        ).append(
                safe(request.getStatus())
        ).append("\n");

        context.append(
                "Priority: "
        ).append(
                safe(request.getPriority())
        ).append("\n");

        context.append(
                "Duration Days: "
        ).append(
                safe(request.getDurationDays())
        ).append("\n");

        context.append(
                "Created At: "
        ).append(
                safe(request.getCreatedAt())
        ).append("\n");

        context.append(
                "--------------------\n"
        );
    }

    // =====================================================
    // ATTENDANCE FORMAT
    // =====================================================

    private void appendAttendance(
            StringBuilder context,
            Attendance attendance
    ) {

        context.append(
                "Date: "
        ).append(
                safe(attendance.getDate())
        ).append("\n");

        context.append(
                "Status: "
        ).append(
                safe(attendance.getStatus())
        ).append("\n");

        context.append(
                "Check In: "
        ).append(
                safe(attendance.getCheckIn())
        ).append("\n");

        context.append(
                "Check Out: "
        ).append(
                safe(attendance.getCheckOut())
        ).append("\n");
    }

    // =====================================================
    // ROLE
    // =====================================================

    private boolean isAdminOrManager(
            User user
    ) {

        return user.getRole() == Role.ADMIN
                || user.getRole() == Role.MANAGER;
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
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

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
    // CONTAINS ANY
    // =====================================================

    private boolean containsAny(
            String text,
            String... values
    ) {

        if (text == null) {
            return false;
        }

        for (String value : values) {

            if (value == null || value.isBlank()) {
                continue;
            }

            if (text.contains(
                    value.toLowerCase(Locale.ROOT)
            )) {

                return true;
            }
        }

        return false;
    }

    // =====================================================
    // SAFE
    // =====================================================

    private String safe(Object value) {

        return value == null
                ? "غير متوفر"
                : value.toString();
    }

    // =====================================================
    // CLEAN RESPONSE
    // =====================================================

    private String cleanResponse(
            String response
    ) {

        if (response == null
                || response.isBlank()) {

            return "لم أتمكن من إنشاء إجابة.";
        }

        return response
                .replaceAll(
                        "^```[a-zA-Z]*\\s*",
                        ""
                )
                .replaceAll(
                        "\\s*```$",
                        ""
                )
                .trim();
    }
}