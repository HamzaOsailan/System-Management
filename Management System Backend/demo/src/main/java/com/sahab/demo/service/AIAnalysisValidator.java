package com.sahab.demo.service.ai;

import com.sahab.demo.entity.AIRequestAnalysis;
import com.sahab.demo.enums.RequestCategory;
import org.springframework.stereotype.Component;

@Component
public class AIAnalysisValidator {

    public void validate(
            AIRequestAnalysis analysis
    ) {

        String category =
                required(
                        analysis.getCategory(),
                        "category"
                )
                        .toUpperCase();

        try {

            RequestCategory.valueOf(
                    category
            );

        } catch (IllegalArgumentException e) {

            throw new RuntimeException(
                    "Invalid AI category: "
                            + category
            );
        }

        analysis.setCategory(category);


        String priority =
                required(
                        analysis.getPriority(),
                        "priority"
                )
                        .toUpperCase();

        if (!priority.equals("LOW")
                && !priority.equals("MEDIUM")
                && !priority.equals("HIGH")) {

            throw new RuntimeException(
                    "Invalid AI priority: "
                            + priority
            );
        }

        analysis.setPriority(priority);


        Integer urgency =
                analysis.getUrgency();

        if (urgency == null
                || urgency < 1
                || urgency > 5) {

            throw new RuntimeException(
                    "AI urgency must be between 1 and 5"
            );
        }


        required(
                analysis.getSubCategory(),
                "subCategory"
        );

        required(
                analysis.getSummary(),
                "summary"
        );

        required(
                analysis.getSuggestedAction(),
                "suggestedAction"
        );

        required(
                analysis.getReason(),
                "reason"
        );


        String department =
                required(
                        analysis.getSuggestedDepartment(),
                        "suggestedDepartment"
                )
                        .toUpperCase();

        validateDepartment(
                department
        );

        validateCategoryDepartment(
                category,
                department
        );

        analysis.setSuggestedDepartment(
                department
        );
    }

    private String required(
            String value,
            String field
    ) {

        if (value == null
                || value.isBlank()) {

            throw new RuntimeException(
                    "AI " + field + " is missing"
            );
        }

        return value.trim();
    }

    private void validateDepartment(
            String department
    ) {

        if (!department.equals("IT")
                && !department.equals("FLEET")
                && !department.equals("HR")
                && !department.equals("GENERAL")) {

            throw new RuntimeException(
                    "Invalid AI department: "
                            + department
            );
        }
    }

    private void validateCategoryDepartment(
            String category,
            String department
    ) {

        boolean valid =
                switch (category) {

                    case "IT" ->
                            department.equals("IT");

                    case "FLEET" ->
                            department.equals("FLEET");

                    case "HR" ->
                            department.equals("HR");

                    case "LEAVE" ->
                            department.equals("HR");

                    case "GENERAL" ->
                            department.equals("GENERAL");

                    default ->
                            false;
                };

        if (!valid) {

            throw new RuntimeException(
                    "Category and department do not match"
            );
        }
    }
}