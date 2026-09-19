package com.sahab.demo.service.ai;

import com.sahab.demo.entity.AIRequestAnalysis;
import com.sahab.demo.entity.Request;
import org.springframework.stereotype.Component;

@Component
public class AIAnalysisRules {

    public void apply(
            AIRequestAnalysis analysis,
            Request request
    ) {

        String text =
                normalize(
                        safe(request.getTitle())
                                + " "
                                + safe(request.getDescription())
                );

        applyFleetRule(
                analysis,
                text
        );

        applyLeaveRule(
                analysis,
                text
        );
    }

    // =====================================================
    // FLEET
    // =====================================================

    private void applyFleetRule(
            AIRequestAnalysis analysis,
            String text
    ) {

        boolean vehicle =
                containsAny(
                        text,
                        "سيار",
                        "مركب",
                        "شاحن",
                        "كفر",
                        "اطار",
                        "بنشر",
                        "بنزين",
                        "ديزل",
                        "وقود"
                );

        if (!vehicle) {
            return;
        }

        analysis.setCategory("FLEET");
        analysis.setSuggestedDepartment("FLEET");

        if (containsAny(
                text,
                "تعطلت",
                "خربت",
                "خربان",
                "عطلان",
                "ما تشتغل",
                "لا تعمل"
        )) {

            analysis.setSubCategory(
                    "Vehicle Breakdown"
            );

            fixGenericAction(
                    analysis,
                    "Contact the Fleet Department and arrange vehicle inspection or roadside assistance."
            );

            fixGenericReason(
                    analysis,
                    "The request concerns a company vehicle problem, so it belongs to Fleet."
            );

            return;
        }

        if (containsAny(
                text,
                "صيان"
        )) {

            analysis.setSubCategory(
                    "Vehicle Maintenance"
            );

            return;
        }

        if (containsAny(
                text,
                "تصليح",
                "اصلاح"
        )) {

            analysis.setSubCategory(
                    "Vehicle Repair"
            );

            return;
        }

        if (containsAny(
                text,
                "كفر",
                "اطار",
                "بنشر"
        )) {

            analysis.setSubCategory(
                    "Tires"
            );

            return;
        }

        if (containsAny(
                text,
                "بنزين",
                "ديزل",
                "وقود"
        )) {

            analysis.setSubCategory(
                    "Fuel"
            );

            return;
        }

        analysis.setSubCategory(
                "Transportation"
        );
    }

    // =====================================================
    // LEAVE
    // =====================================================

    private void applyLeaveRule(
            AIRequestAnalysis analysis,
            String text
    ) {

        boolean leave =
                containsAny(
                        text,
                        "اجاز",
                        "عطله",
                        "غياب",
                        "وقت راحه"
                );

        if (!leave) {
            return;
        }

        analysis.setCategory("LEAVE");
        analysis.setSuggestedDepartment("HR");

        if (containsAny(
                text,
                "مرض",
                "مرضي"
        )) {

            analysis.setSubCategory(
                    "Sick Leave"
            );

        } else if (
                containsAny(
                        text,
                        "سنوي"
                )
        ) {

            analysis.setSubCategory(
                    "Annual Leave"
            );

        } else if (
                containsAny(
                        text,
                        "اجاز",
                        "عطله"
                )
        ) {

            analysis.setSubCategory(
                    "Leave"
            );

        } else {

            analysis.setSubCategory(
                    "Leave"
            );
        }

        fixGenericSummary(
                analysis,
                "The employee submitted a leave request."
        );

        fixGenericAction(
                analysis,
                "Review the leave request and process it according to company leave policy."
        );

        fixGenericReason(
                analysis,
                "The request concerns employee leave and should be handled by HR."
        );
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private void fixGenericSummary(
            AIRequestAnalysis analysis,
            String value
    ) {

        if (isGeneric(analysis.getSummary())) {
            analysis.setSummary(value);
        }
    }

    private void fixGenericAction(
            AIRequestAnalysis analysis,
            String value
    ) {

        if (isGeneric(analysis.getSuggestedAction())) {
            analysis.setSuggestedAction(value);
        }
    }

    private void fixGenericReason(
            AIRequestAnalysis analysis,
            String value
    ) {

        if (isGeneric(analysis.getReason())) {
            analysis.setReason(value);
        }
    }

    private boolean isGeneric(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            return true;
        }

        String text =
                value.trim()
                        .toLowerCase();

        return text.equals(
                "recommended action"
        )
                || text.equals(
                "recommended action."
        )
                || text.equals(
                "reason for the analysis"
        )
                || text.equals(
                "reason for the analysis."
        )
                || text.equals(
                "summary"
        )
                || text.equals(
                "n/a"
        )
                || text.equals(
                "cannot be processed"
        );
    }

    private boolean containsAny(
            String text,
            String... values
    ) {

        for (String value : values) {

            if (text.contains(value)) {
                return true;
            }
        }

        return false;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String normalize(String text) {

        return text
                .toLowerCase()
                .trim()
                .replace("أ", "ا")
                .replace("إ", "ا")
                .replace("آ", "ا")
                .replace("ى", "ي")
                .replace("ؤ", "و")
                .replace("ئ", "ي");
    }
}