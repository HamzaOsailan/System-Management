package com.sahab.demo.modules.ai.service;

import com.sahab.demo.modules.ai.enums.ChatIntent;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatIntentDetector {

    public ChatIntent detect(String message) {

        if (message == null || message.isBlank()) {
            System.out.println("========== CHAT INTENT DEBUG ==========");
            System.out.println("Message is empty");
            System.out.println("Detected Intent: GENERAL");
            System.out.println("=======================================");

            return ChatIntent.GENERAL;
        }

        String text =
                message
                        .toLowerCase(Locale.ROOT)
                        .trim();

        ChatIntent intent;

        // =========================================
        // Request Details
        // =========================================

        if (containsAny(
                text,
                "request number",
                "request id",
                "request #",
                "طلب رقم",
                "رقم الطلب",
                "تفاصيل الطلب",
                "تفاصيل طلب",
                "detail of request",
                "details of request"
        )) {

            intent = ChatIntent.REQUEST_DETAILS;

            printDebug(message, intent);
            return intent;
        }

        // =========================================
        // AI Analysis
        // =========================================

        if (containsAny(
                text,
                "ai analysis",
                "ai تحليل",
                "تحليل ai",
                "تحليل الذكاء",
                "تحليل الطلب",
                "تحليل طلبي",
                "اشرح التحليل",
                "اشرح لي التحليل",
                "تحليل الطلب رقم",
                "analysis of request"
        )) {

            intent = ChatIntent.AI_ANALYSIS;

            printDebug(message, intent);
            return intent;
        }

        // =========================================
        // HR Attendance
        // =========================================

        if (isHrAttendanceQuestion(text)) {

            intent = ChatIntent.HR_ATTENDANCE;

            printDebug(message, intent);
            return intent;
        }

        // =========================================
        // Personal Attendance
        // =========================================

        if (containsAny(
                text,
                "attendance",
                "check in",
                "check-in",
                "check out",
                "check-out",
                "حضور",
                "حضوري",
                "دوامي",
                "سجل الحضور",
                "سجل حضوري",
                "حضوري اليوم",
                "دخلت اليوم",
                "خرجت اليوم",
                "متى دخلت",
                "متى خرجت",
                "وقت الدخول",
                "وقت الخروج"
        )) {

            intent = ChatIntent.ATTENDANCE;

            printDebug(message, intent);
            return intent;
        }

        // =========================================
        // Leave / Remote Work
        // =========================================

        if (isLeaveQuestion(text)) {

            intent = ChatIntent.LEAVE;

            printDebug(message, intent);
            return intent;
        }

        // =========================================
        // Requests
        // =========================================

        if (containsAny(
                text,
                "requests",
                "request",
                "my requests",
                "طلبات",
                "طلباتي",
                "طلب",
                "الطلبات",
                "طلباتي المعلقة",
                "الطلبات المعلقة",
                "pending requests",
                "approved requests",
                "rejected requests"
        )) {

            intent = ChatIntent.REQUESTS;

            printDebug(message, intent);
            return intent;
        }

        // =========================================
        // Profile
        // =========================================

        if (containsAny(
                text,
                "who am i",
                "my profile",
                "my information",
                "my account",
                "من أنا",
                "معلوماتي",
                "بياناتي",
                "حسابي",
                "وظيفتي",
                "دوري",
                "من انا"
        )) {

            intent = ChatIntent.PROFILE;

            printDebug(message, intent);
            return intent;
        }

        // =========================================
        // General
        // =========================================

        intent = ChatIntent.GENERAL;

        printDebug(message, intent);

        return intent;
    }

    // =====================================================
    // Leave Detection
    // =====================================================

    private boolean isLeaveQuestion(String text) {

        // -----------------------------------------
        // Annual Leave keywords
        // -----------------------------------------

        boolean annualLeave =
                containsAny(
                        text,

                        // English
                        "annual leave",
                        "annual leaves",
                        "leave balance",
                        "vacation balance",
                        "vacation",
                        "leave",
                        "leaves",

                        // Arabic
                        "إجازة",
                        "اجازة",
                        "الإجازة",
                        "الاجازة",
                        "إجازاتي",
                        "اجازاتي",
                        "إجازات",
                        "اجازات",
                        "الإجازات",
                        "الاجازات",
                        "رصيد الإجازات",
                        "رصيد الاجازات",
                        "رصيد الإجازة",
                        "رصيد الاجازة",
                        "رصيد إجازاتي",
                        "رصيد اجازاتي",
                        "الإجازات السنوية",
                        "الاجازات السنوية",
                        "إجازة سنوية",
                        "اجازة سنوية",
                        "إجازاتي السنوية",
                        "اجازاتي السنوية"
                );

        // -----------------------------------------
        // Remote Work keywords
        // -----------------------------------------

        boolean remoteWork =
                containsAny(
                        text,

                        // English
                        "remote work",
                        "remote working",
                        "work from home",
                        "working from home",
                        "wfh",
                        "remote balance",

                        // Arabic
                        "العمل عن بعد",
                        "عمل عن بعد",
                        "العمل من المنزل",
                        "عمل من المنزل",
                        "العمل من البيت",
                        "عمل من البيت",
                        "عن بعد",
                        "رصيد العمل عن بعد",
                        "رصيد عن بعد"
                );

        // -----------------------------------------
        // Balance keywords
        // -----------------------------------------

        boolean balanceQuestion =
                containsAny(
                        text,

                        // English
                        "balance",
                        "remaining",
                        "remain",
                        "left",
                        "used",
                        "available",
                        "how many days",
                        "how much leave",
                        "how many leave days",

                        // Arabic
                        "كم باقي",
                        "كم تبقى",
                        "كم بقي",
                        "كم باقي لي",
                        "كم يوم باقي",
                        "كم يوم متبقي",
                        "كم يوم تبقى",
                        "كم يوم بقي",
                        "المتبقي",
                        "المتبقية",
                        "المتبقي لي",
                        "الباقي",
                        "استخدمت",
                        "استخدم",
                        "المستخدم",
                        "المستخدمة",
                        "رصيدي",
                        "الرصيد",
                        "كم عندي",
                        "كم يوم عندي",
                        "كم يوم متبقي لي"
                );

        // -----------------------------------------
        // Direct annual leave question
        // -----------------------------------------

        if (annualLeave) {
            return true;
        }

        // -----------------------------------------
        // Direct remote work question
        // -----------------------------------------

        if (remoteWork) {
            return true;
        }

        // -----------------------------------------
        // Balance question + leave context
        // -----------------------------------------

        if (balanceQuestion
                && containsAny(
                text,
                "leave",
                "leaves",
                "vacation",
                "إجازة",
                "اجازة",
                "إجازاتي",
                "اجازاتي",
                "إجازات",
                "اجازات",
                "رصيد",
                "remote",
                "عن بعد"
        )) {

            return true;
        }

        return false;
    }

    // =====================================================
    // HR Attendance Detection
    // =====================================================

    private boolean isHrAttendanceQuestion(
            String text
    ) {

        boolean employeeWord =
                containsAny(
                        text,
                        "employee",
                        "employees",
                        "staff",
                        "الموظف",
                        "الموظفين",
                        "موظفين",
                        "موظف",
                        "الفريق",
                        "فريقي"
                );

        boolean attendanceWord =
                containsAny(
                        text,
                        "attendance",
                        "attend",
                        "check in",
                        "check-in",
                        "check out",
                        "check-out",
                        "حضور",
                        "حضر",
                        "حاضرين",
                        "حاضر",
                        "حاضرة",
                        "دوام",
                        "داوم",
                        "سجل الحضور"
                );

        boolean todayWord =
                containsAny(
                        text,
                        "today",
                        "اليوم",
                        "اليوم؟"
                );

        boolean countWord =
                containsAny(
                        text,
                        "how many",
                        "count",
                        "number",
                        "كم",
                        "عدد",
                        "كم موظف",
                        "كم موظفين"
                );

        boolean whoWord =
                containsAny(
                        text,
                        "who",
                        "مين",
                        "من",
                        "مين حضر",
                        "من حضر",
                        "مين داوم",
                        "من داوم"
                );

        // Employee + attendance

        if (employeeWord && attendanceWord) {
            return true;
        }

        // Today + count/who + attendance

        if (todayWord
                && (countWord || whoWord)
                && attendanceWord) {

            return true;
        }

        if (text.contains("مين حضر")) {
            return true;
        }

        if (text.contains("من حضر")) {
            return true;
        }

        if (text.contains("مين داوم")) {
            return true;
        }

        if (text.contains("من داوم")) {
            return true;
        }

        if (text.contains("كم موظف")
                && attendanceWord) {

            return true;
        }

        if (text.contains("كم عدد الموظفين")
                && attendanceWord) {

            return true;
        }

        return false;
    }

    // =====================================================
    // Request ID Extraction
    // =====================================================

    public Long extractRequestId(
            String message
    ) {

        if (message == null) {
            return null;
        }

        Pattern pattern =
                Pattern.compile(
                        "(?:request\\s*(?:number|id|#)?|طلب\\s*(?:رقم)?)[\\s:#-]*(\\d+)",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                pattern.matcher(message);

        if (matcher.find()) {

            return Long.parseLong(
                    matcher.group(1)
            );
        }

        return null;
    }

    // =====================================================
    // Common Text Matcher
    // =====================================================

    private boolean containsAny(
            String text,
            String... values
    ) {

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
    // Debug
    // =====================================================

    private void printDebug(
            String message,
            ChatIntent intent
    ) {

        System.out.println();
        System.out.println("========== CHAT INTENT DEBUG ==========");
        System.out.println("Original message: " + message);
        System.out.println("Detected Intent: " + intent);
        System.out.println("=======================================");
        System.out.println();
    }
}