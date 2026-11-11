package com.sahab.demo.modules.identity.service;

import com.sahab.demo.modules.identity.dto.EmployeeProfileDTO;
import com.sahab.demo.modules.identity.dto.EmployeeProfileUpdateDTO;
import com.sahab.demo.entity.User;
import com.sahab.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeProfileService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get currently authenticated user from JWT email.
     *
     * Frontend does NOT send userId.
     */
    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {
            throw new RuntimeException(
                    "Authentication not found"
            );
        }

        String email = authentication.getName();

        if (email == null || email.isBlank()) {
            throw new RuntimeException(
                    "Authenticated user email not found"
            );
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Current user not found"
                        )
                );
    }

    /**
     * GET /employee-profile
     *
     * Returns the currently authenticated user's profile.
     */
    @Transactional(readOnly = true)
    public EmployeeProfileDTO getMyProfile() {

        User user = getCurrentUser();

        return convertToDTO(user);
    }

    /**
     * PUT /employee-profile
     *
     * Users can update only:
     *
     * Personal Information:
     * - name
     * - phone
     * - address
     * - dateOfBirth
     * - nationality
     *
     * Banking Information:
     * - bankName
     * - bankBranch
     * - accountNumber
     * - accountHolderName
     * - accountType
     * - iban
     *
     * Administrative / employment fields are intentionally NOT updated:
     * - employeeId
     * - jobTitle
     * - department
     * - manager
     * - employmentStatus
     * - joiningDate
     * - employmentType
     * - workLocation
     * - workingHours
     * - role
     * - email
     */
    @Transactional
    public EmployeeProfileDTO updateMyProfile(
            EmployeeProfileUpdateDTO dto
    ) {

        if (dto == null) {
            throw new RuntimeException(
                    "Profile data is required"
            );
        }

        User user = getCurrentUser();

        // =========================
        // Personal Information
        // =========================

        if (dto.getName() == null ||
                dto.getName().trim().isBlank()) {

            throw new RuntimeException(
                    "Name is required"
            );
        }

        user.setName(
                dto.getName().trim()
        );

        user.setPhone(
                dto.getPhone()
        );

        user.setAddress(
                dto.getAddress()
        );

        user.setDateOfBirth(
                dto.getDateOfBirth()
        );

        user.setNationality(
                dto.getNationality()
        );

        // =========================
        // Employment Information
        // =========================
        //
        // IMPORTANT:
        //
        // These fields are intentionally NOT updated here.
        //
        // They are administrative fields and must be managed
        // through the appropriate administrative user-management
        // functionality.
        //
        // NOT UPDATED:
        //
        // user.setJobTitle(...)
        // user.setEmploymentType(...)
        // user.setWorkLocation(...)
        // user.setWorkingHours(...)
        // department
        // manager
        // employmentStatus
        // employeeId
        // joiningDate
        // role
        // email

        // =========================
        // Banking Information
        // =========================

        user.setBankName(
                dto.getBankName()
        );

        user.setBankBranch(
                dto.getBankBranch()
        );

        user.setAccountNumber(
                dto.getAccountNumber()
        );

        user.setAccountHolderName(
                dto.getAccountHolderName()
        );

        user.setAccountType(
                dto.getAccountType()
        );

        user.setIban(
                dto.getIban()
        );

        User savedUser =
                userRepository.save(user);

        return convertToDTO(savedUser);
    }

    /**
     * PUT /employee-profile/change-password
     *
     * Allows the authenticated user to change their own password.
     */
    @Transactional
    public void changePassword(
            ChangePasswordRequest request
    ) {

        if (request == null) {
            throw new RuntimeException(
                    "Password request is required"
            );
        }

        if (request.getCurrentPassword() == null ||
                request.getCurrentPassword().isBlank()) {

            throw new RuntimeException(
                    "Current password is required"
            );
        }

        if (request.getNewPassword() == null ||
                request.getNewPassword().isBlank()) {

            throw new RuntimeException(
                    "New password is required"
            );
        }

        if (request.getNewPassword().length() < 6) {

            throw new RuntimeException(
                    "New password must contain at least 6 characters"
            );
        }

        User user = getCurrentUser();

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword()
        )) {

            throw new RuntimeException(
                    "Current password is incorrect"
            );
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword()
        )) {

            throw new RuntimeException(
                    "New password must be different from current password"
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);
    }

    /**
     * Convert User entity to EmployeeProfileDTO.
     */
    private EmployeeProfileDTO convertToDTO(
            User user
    ) {

        EmployeeProfileDTO dto =
                new EmployeeProfileDTO();

        // =========================
        // Basic Account Information
        // =========================

        dto.setId(
                user.getId()
        );

        dto.setName(
                user.getName()
        );

        dto.setEmail(
                user.getEmail()
        );

        if (user.getRole() != null) {

            dto.setRole(
                    user.getRole().name()
            );
        }

        // =========================
        // Personal Information
        // =========================

        dto.setPhone(
                user.getPhone()
        );

        dto.setAddress(
                user.getAddress()
        );

        dto.setDateOfBirth(
                user.getDateOfBirth()
        );

        dto.setNationality(
                user.getNationality()
        );

        // =========================
        // Employment Information
        // =========================

        dto.setEmployeeId(
                user.getEmployeeId()
        );

        dto.setJobTitle(
                user.getJobTitle()
        );

        // Department is a Department entity.
        // Department.name is String.
        // Therefore .name() must NOT be used.

        if (user.getDepartment() != null) {

            dto.setDepartment(
                    user.getDepartment()
                            .getName()
            );

        } else {

            dto.setDepartment(null);
        }

        dto.setEmploymentType(
                user.getEmploymentType()
        );

        dto.setEmploymentStatus(
                user.getEmploymentStatus()
        );

        dto.setWorkLocation(
                user.getWorkLocation()
        );

        dto.setWorkingHours(
                user.getWorkingHours()
        );

        dto.setJoiningDate(
                user.getJoiningDate()
        );

        // =========================
        // Manager
        // =========================
        //
        // Manager is stored as a User relationship:
        //
        // user.manager
        //
        // NOT user.managerName.
        //

        if (user.getManager() != null) {

            dto.setManagerId(
                    user.getManager().getId()
            );

            dto.setManagerName(
                    user.getManager().getName()
            );

        } else {

            dto.setManagerId(null);
            dto.setManagerName(null);
        }

        // =========================
        // Banking Information
        // =========================

        dto.setBankName(
                user.getBankName()
        );

        dto.setBankBranch(
                user.getBankBranch()
        );

        dto.setAccountHolderName(
                user.getAccountHolderName()
        );

        dto.setAccountType(
                user.getAccountType()
        );

        dto.setIban(
                user.getIban()
        );

        dto.setAccountNumber(
                user.getAccountNumber()
        );

        return dto;
    }

    /**
     * Change Password Request
     */
    public static class ChangePasswordRequest {

        private String currentPassword;

        private String newPassword;

        public ChangePasswordRequest() {
        }

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(
                String currentPassword
        ) {
            this.currentPassword =
                    currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(
                String newPassword
        ) {
            this.newPassword =
                    newPassword;
        }
    }


}
