package com.sahab.demo.service.ai;

import com.sahab.demo.entity.Request;
import org.springframework.stereotype.Component;

@Component
public class AIRequestPromptBuilder {

    public String build(Request request) {

        return """
                Analyze the following employee request.

                TITLE:
                %s

                DESCRIPTION:
                %s

                ORIGINAL CATEGORY:
                %s


                CATEGORIES
                --------------

                IT:
                Computers, laptops, printers, software,
                applications, network, internet,
                accounts, passwords, servers and technical systems.

                FLEET:
                Cars, vehicles, trucks, drivers,
                vehicle breakdown, maintenance,
                repair, tires, fuel and transportation.

                LEAVE:
                Vacation, annual leave, sick leave,
                absence and time off.

                HR:
                Employees, salaries, attendance,
                recruitment, benefits and personnel.

                GENERAL:
                Anything that clearly does not belong
                to the previous categories.


                IMPORTANT CLASSIFICATION RULES
                --------------------------------

                The title and description are more important
                than the original category.

                Examples:

                "سيارتي تعطلت"
                => FLEET

                "السيارة تحتاج صيانة"
                => FLEET

                "كفر السيارة خربان"
                => FLEET

                "أريد إجازة"
                => LEAVE

                "اجازتي الأسبوع القادم"
                => LEAVE

                "إجازة مرضية"
                => LEAVE

                "اللابتوب لا يعمل"
                => IT

                "الطابعة لا تطبع"
                => IT

                "عندي مشكلة في الراتب"
                => HR


                SUB CATEGORY
                ------------

                IT:
                Hardware
                Software
                Printer
                Network
                Account
                Password

                FLEET:
                Vehicle Breakdown
                Vehicle Maintenance
                Vehicle Repair
                Tires
                Fuel
                Driver
                Transportation

                LEAVE:
                Annual Leave
                Sick Leave
                Vacation
                Leave

                HR:
                Attendance
                Salary
                Employee
                Recruitment
                Benefits


                PRIORITY
                --------

                LOW
                MEDIUM
                HIGH


                URGENCY
                --------

                1 = Very low
                2 = Low
                3 = Medium
                4 = High
                5 = Critical


                DEPARTMENT
                ----------

                IT -> IT
                FLEET -> FLEET
                HR -> HR
                LEAVE -> HR
                GENERAL -> GENERAL


                SUMMARY
                -------

                Write a short meaningful summary
                of the actual request.


                SUGGESTED ACTION
                ----------------

                Give one practical action that should be
                taken to handle the request.


                REASON
                ------

                Briefly explain why you selected
                the category, priority, urgency and department.


                OUTPUT
                ------

                Return ONLY this JSON structure:

                {
                  "category": "LEAVE",
                  "subCategory": "Leave",
                  "priority": "MEDIUM",
                  "urgency": 3,
                  "summary": "The employee submitted a leave request.",
                  "suggestedDepartment": "HR",
                  "suggestedAction": "Review the leave request and process it according to company leave policy.",
                  "reason": "The request concerns employee leave and should be handled by HR."
                }
                """.formatted(
                request.getTitle(),
                request.getDescription(),
                request.getCategory()
        );
    }
}