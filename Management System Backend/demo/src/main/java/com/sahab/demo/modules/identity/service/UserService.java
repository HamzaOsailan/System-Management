
package com.sahab.demo.modules.identity.service;

import com.sahab.demo.modules.identity.dto.CreateUserDTO;
import com.sahab.demo.modules.identity.dto.EmployeeProfileDTO;
import com.sahab.demo.modules.requests.dto.EmployeeRequestDTO;
import com.sahab.demo.modules.identity.dto.UpdateUserDTO;
import com.sahab.demo.modules.identity.dto.UserResponseDTO;
import com.sahab.demo.entity.Department;
import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.Role;
import com.sahab.demo.exception.ResourceNotFoundException;
import com.sahab.demo.modules.departments.repository.DepartmentRepository;
import com.sahab.demo.modules.requests.repository.RequestAttachmentRepository;
import com.sahab.demo.modules.requests.repository.RequestRepository;
import com.sahab.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RequestRepository requestRepository;
    private final RequestAttachmentRepository requestAttachmentRepository;
    private final DepartmentRepository departmentRepository;

    // =========================================================
    // GET ALL USERS
    // =========================================================

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // =========================================================
    // GET USER BY ID
    // =========================================================

    public User getUserById(Long id) {

        if (id == null) {
            throw new RuntimeException(
                    "User ID is required"
            );
        }

        return userRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    // =========================================================
    // GET USER BY EMAIL
    // =========================================================

    public User getUserByEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new RuntimeException(
                    "Email is required"
            );
        }

        return userRepository
                .findByEmail(
                        email.trim().toLowerCase()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    // =========================================================
    // GET ALL MANAGERS
    // =========================================================

    public List<User> getAllManagers() {

        return userRepository.findByRole(
                Role.MANAGER
        );
    }

    // =========================================================
    // UPDATE USER ROLE
    // =========================================================

    @Transactional
    public User updateUserRole(
            Long id,
            Role newRole
    ) {

        User user = getUserById(id);

        if (newRole == null) {
            throw new RuntimeException(
                    "Role is required"
            );
        }

        // =====================================================
        // NO CHANGE
        // =====================================================

        if (user.getRole() == newRole) {
            return user;
        }

        // =====================================================
        // MANAGER RELATIONSHIP
        //
        // Only USER can have a manager.
        // If a USER becomes MANAGER/ADMIN/HR,
        // remove the old manager relationship.
        // =====================================================

        if (newRole != Role.USER) {

            user.setManager(null);
        }

        // =====================================================
        // CHANGE ROLE
        // =====================================================

        user.setRole(newRole);

        return userRepository.save(user);
    }

    // =========================================================
    // DELETE USER
    // =========================================================

    @Transactional
    public void deleteUser(Long id) {

        User user = getUserById(id);

        // =====================================================
        // DO NOT DELETE A MANAGER WITH ACTIVE TEAM MEMBERS
        // =====================================================

        if (user.getRole() == Role.MANAGER) {

            List<User> team =
                    userRepository.findByManagerId(
                            user.getId()
                    );

            if (team != null && !team.isEmpty()) {

                throw new RuntimeException(
                        "Cannot delete a Manager who has assigned employees. Reassign the employees first."
                );
            }
        }

        // =====================================================
        // REQUESTS OWNED BY USER
        //
        // We intentionally block deletion if the user has
        // requests because requests are business records.
        // =====================================================

        List<Request> requests =
                requestRepository.findByUserIdOrderByCreatedAtDesc(
                        user.getId()
                );

        if (requests != null && !requests.isEmpty()) {

            throw new RuntimeException(
                    "Cannot delete a user who has existing requests"
            );
        }

        userRepository.delete(user);
    }

    // =========================================================
    // CREATE USER
    // =========================================================

    @Transactional
    public User createUser(
            CreateUserDTO dto
    ) {

        if (dto == null) {
            throw new RuntimeException(
                    "User data is required"
            );
        }

        // =====================================================
        // NAME
        // =====================================================

        if (
                dto.getName() == null
                        ||
                        dto.getName().trim().isBlank()
        ) {

            throw new RuntimeException(
                    "Name is required"
            );
        }

        String name =
                dto.getName().trim();

        if (name.length() > 150) {
            throw new RuntimeException(
                    "Name must not exceed 150 characters"
            );
        }

        // =====================================================
        // EMAIL
        // =====================================================

        if (
                dto.getEmail() == null
                        ||
                        dto.getEmail().trim().isBlank()
        ) {

            throw new RuntimeException(
                    "Email is required"
            );
        }

        String email =
                dto.getEmail()
                        .trim()
                        .toLowerCase();

        if (email.length() > 255) {
            throw new RuntimeException(
                    "Email must not exceed 255 characters"
            );
        }

        if (userRepository
                .findByEmail(email)
                .isPresent()) {

            throw new RuntimeException(
                    "Email already exists"
            );
        }

        // =====================================================
        // PASSWORD
        // =====================================================

        if (
                dto.getPassword() == null
                        ||
                        dto.getPassword().isBlank()
        ) {

            throw new RuntimeException(
                    "Password is required"
            );
        }

        if (dto.getPassword().length() < 6) {
            throw new RuntimeException(
                    "Password must contain at least 6 characters"
            );
        }

        // =====================================================
        // ROLE
        // =====================================================

        if (dto.getRole() == null) {
            throw new RuntimeException(
                    "Role is required"
            );
        }

        // =====================================================
        // CREATE USER
        // =====================================================

        User user = new User();

        user.setName(name);
        user.setEmail(email);

        user.setPassword(
                passwordEncoder.encode(
                        dto.getPassword()
                )
        );

        user.setRole(
                dto.getRole()
        );

        // =====================================================
        // EMPLOYEE ID
        // =====================================================

        if (
                dto.getEmployeeId() != null
                        &&
                        !dto.getEmployeeId()
                                .trim()
                                .isBlank()
        ) {

            user.setEmployeeId(
                    dto.getEmployeeId().trim()
            );
        }

        // =====================================================
        // JOB TITLE
        // =====================================================

        if (
                dto.getJobTitle() != null
                        &&
                        !dto.getJobTitle()
                                .trim()
                                .isBlank()
        ) {

            user.setJobTitle(
                    dto.getJobTitle().trim()
            );
        }

        // =====================================================
        // DEPARTMENT
        // =====================================================

        if (
                dto.getDepartment() != null
                        &&
                        !dto.getDepartment()
                                .trim()
                                .isBlank()
        ) {

            String departmentName =
                    normalizeDepartmentName(
                            dto.getDepartment()
                    );

            Department department =
                    departmentRepository
                            .findByName(departmentName)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Department not found: "
                                                    + dto.getDepartment()
                                    )
                            );

            user.setDepartment(
                    department
            );
        }

        // =====================================================
        // JOINING DATE
        // =====================================================

        user.setJoiningDate(
                dto.getJoiningDate()
        );

        // =====================================================
        // EMPLOYMENT TYPE
        // =====================================================

        if (
                dto.getEmploymentType() != null
                        &&
                        !dto.getEmploymentType()
                                .trim()
                                .isBlank()
        ) {

            user.setEmploymentType(
                    dto.getEmploymentType().trim()
            );
        }

        // =====================================================
        // EMPLOYMENT STATUS
        // =====================================================

        if (
                dto.getEmploymentStatus() != null
                        &&
                        !dto.getEmploymentStatus()
                                .trim()
                                .isBlank()
        ) {

            user.setEmploymentStatus(
                    dto.getEmploymentStatus().trim()
            );
        }

        // =====================================================
        // WORK LOCATION
        // =====================================================

        if (
                dto.getWorkLocation() != null
                        &&
                        !dto.getWorkLocation()
                                .trim()
                                .isBlank()
        ) {

            user.setWorkLocation(
                    dto.getWorkLocation().trim()
            );
        }

        // =====================================================
        // WORKING HOURS
        // =====================================================

        if (
                dto.getWorkingHours() != null
                        &&
                        !dto.getWorkingHours()
                                .trim()
                                .isBlank()
        ) {

            user.setWorkingHours(
                    dto.getWorkingHours().trim()
            );
        }

        // =====================================================
        // MANAGER
        //
        // Only USER can have a Manager.
        // =====================================================

        if (dto.getManagerId() != null) {

            if (dto.getRole() != Role.USER) {

                throw new RuntimeException(
                        "Only Users can be assigned to a Manager"
                );
            }

            User manager =
                    getUserById(
                            dto.getManagerId()
                    );

            validateManagerAssignment(
                    user,
                    manager
            );

            user.setManager(
                    manager
            );
        }

        // =====================================================
        // SAVE
        // =====================================================

        return userRepository.save(user);
    }




    @Transactional
    public User updateUser(Long id, UpdateUserDTO dto) {

        if (dto == null) {
            throw new RuntimeException("User data is required");
        }

        User user = getUserById(id);

        // =========================================================
        // ROLE
        // =========================================================

        if (dto.getRole() != null) {

            user.setRole(dto.getRole());

            // Only USER can have a manager
            if (dto.getRole() != Role.USER) {
                user.setManager(null);
            }
        }

        // =========================================================
        // EMPLOYEE ID
        // =========================================================

        user.setEmployeeId(
                dto.getEmployeeId() != null &&
                        !dto.getEmployeeId().trim().isEmpty()
                        ? dto.getEmployeeId().trim()
                        : null
        );

        // =========================================================
        // JOB TITLE
        // =========================================================

        user.setJobTitle(
                dto.getJobTitle() != null &&
                        !dto.getJobTitle().trim().isEmpty()
                        ? dto.getJobTitle().trim()
                        : null
        );

        // =========================================================
        // DEPARTMENT
        // =========================================================

        if (dto.getDepartment() != null &&
                !dto.getDepartment().trim().isEmpty()) {

            String departmentName =
                    normalizeDepartmentName(
                            dto.getDepartment()
                    );

            Department department =
                    departmentRepository
                            .findByNameIgnoreCase(departmentName)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Department not found: "
                                                    + dto.getDepartment()
                                    )
                            );

            user.setDepartment(department);

        } else {

            user.setDepartment(null);
        }

        // =========================================================
        // MANAGER
        // =========================================================

        if (user.getRole() != Role.USER) {
            user.setManager(null);
        } else if (dto.getManagerId() != null) {
            User manager = getUserById(dto.getManagerId());

            validateManagerAssignment(user, manager);

            user.setManager(manager);
        } else {
            user.setManager(null);
        }

        // =========================================================
        // JOINING DATE
        // =========================================================

        user.setJoiningDate(
                dto.getJoiningDate()
        );

        // =========================================================
        // EMPLOYMENT TYPE
        // =========================================================

        user.setEmploymentType(
                dto.getEmploymentType() != null &&
                        !dto.getEmploymentType().trim().isEmpty()
                        ? dto.getEmploymentType().trim()
                        : null
        );

        // =========================================================
        // EMPLOYMENT STATUS
        // =========================================================

        user.setEmploymentStatus(
                dto.getEmploymentStatus() != null &&
                        !dto.getEmploymentStatus().trim().isEmpty()
                        ? dto.getEmploymentStatus().trim()
                        : null
        );

        // =========================================================
        // WORK LOCATION
        // =========================================================

        user.setWorkLocation(
                dto.getWorkLocation() != null &&
                        !dto.getWorkLocation().trim().isEmpty()
                        ? dto.getWorkLocation().trim()
                        : null
        );

        // =========================================================
        // WORKING HOURS
        // =========================================================

        user.setWorkingHours(
                dto.getWorkingHours() != null &&
                        !dto.getWorkingHours().trim().isEmpty()
                        ? dto.getWorkingHours().trim()
                        : null
        );

        // =========================================================
        // SAVE
        // =========================================================

        return userRepository.save(user);
    }









    // =========================================================
    // ASSIGN MANAGER
    // =========================================================

    @Transactional
    public User assignManager(
            Long userId,
            Long managerId
    ) {

        if (userId == null || managerId == null) {
            throw new RuntimeException(
                    "User ID and Manager ID are required"
            );
        }

        User user =
                getUserById(userId);

        User manager =
                getUserById(managerId);

        // =====================================================
        // ONLY USER CAN HAVE MANAGER
        // =====================================================

        if (user.getRole() != Role.USER) {

            throw new RuntimeException(
                    "Only Users can be assigned to a Manager"
            );
        }

        // =====================================================
        // VALIDATE MANAGER
        // =====================================================

        validateManagerAssignment(
                user,
                manager
        );

        user.setManager(
                manager
        );

        return userRepository.save(user);
    }

    // =========================================================
    // VALIDATE MANAGER ASSIGNMENT
    // =========================================================

    private void validateManagerAssignment(
            User user,
            User manager
    ) {

        if (user == null) {
            throw new RuntimeException(
                    "User is required"
            );
        }

        if (manager == null) {
            throw new RuntimeException(
                    "Manager is required"
            );
        }

        // =====================================================
        // TARGET MUST BE MANAGER
        // =====================================================

        if (manager.getRole() != Role.MANAGER) {

            throw new RuntimeException(
                    "Selected user is not a Manager"
            );
        }

        // =====================================================
        // USER CANNOT BE THEIR OWN MANAGER
        // =====================================================

        if (
                user.getId() != null
                        &&
                        manager.getId() != null
                        &&
                        user.getId().equals(
                                manager.getId()
                        )
        ) {

            throw new RuntimeException(
                    "A user cannot be their own manager"
            );
        }

        // =====================================================
        // MANAGER MUST NOT HAVE A MANAGER
        // =====================================================

        if (user.getRole() == Role.MANAGER) {

            throw new RuntimeException(
                    "A Manager cannot be assigned to another Manager"
            );
        }
    }

    // =========================================================
    // GET MY TEAM
    // =========================================================

    public List<User> getMyTeam(
            User manager
    ) {

        if (manager == null) {

            throw new ResourceNotFoundException(
                    "Manager not found"
            );
        }

        if (manager.getRole() != Role.MANAGER) {

            throw new RuntimeException(
                    "Only Managers can access their team"
            );
        }

        return userRepository.findByManagerId(
                manager.getId()
        );
    }

    // =========================================================
    // CONVERT USER TO RESPONSE DTO
    // =========================================================

    public UserResponseDTO toResponseDTO(
            User user
    ) {

        if (user == null) {
            throw new RuntimeException(
                    "User is required"
            );
        }

        UserResponseDTO dto =
                new UserResponseDTO();

        // =====================================================
        // BASIC
        // =====================================================

        dto.setId(
                user.getId()
        );

        dto.setName(
                user.getName()
        );

        dto.setEmail(
                user.getEmail()
        );

        dto.setRole(
                user.getRole()
        );

        // =====================================================
        // PERSONAL
        // =====================================================

        dto.setPhone(
                user.getPhone()
        );

        dto.setDateOfBirth(
                user.getDateOfBirth()
        );

        dto.setNationality(
                user.getNationality()
        );

        dto.setAddress(
                user.getAddress()
        );

        // =====================================================
        // EMPLOYMENT
        // =====================================================

        dto.setEmployeeId(
                user.getEmployeeId()
        );

        dto.setJobTitle(
                user.getJobTitle()
        );

        if (user.getDepartment() != null) {

            dto.setDepartment(
                    user.getDepartment()
                            .getName()
            );

        } else {

            dto.setDepartment(null);
        }

        dto.setJoiningDate(
                user.getJoiningDate()
        );

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

        // =====================================================
        // BANKING
        // =====================================================

        dto.setBankName(
                user.getBankName()
        );

        dto.setAccountHolderName(
                user.getAccountHolderName()
        );

        dto.setAccountType(
                user.getAccountType()
        );

        dto.setBankBranch(
                user.getBankBranch()
        );

        // =====================================================
        // MANAGER
        // =====================================================

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

        return dto;
    }

    // =========================================================
    // GET EMPLOYEE PROFILE FOR MANAGER
    // =========================================================

    public EmployeeProfileDTO getEmployeeProfileForManager(
            Long employeeId,
            User manager
    ) {

        if (manager == null) {

            throw new ResourceNotFoundException(
                    "Manager not found"
            );
        }

        if (manager.getRole() != Role.MANAGER) {

            throw new RuntimeException(
                    "Only Managers can access employee profiles"
            );
        }

        User employee =
                userRepository
                        .findByIdAndManagerId(
                                employeeId,
                                manager.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employee not found in your team"
                                )
                        );

        // =====================================================
        // EMPLOYEE REQUESTS
        // =====================================================

        List<Request> requests =
                requestRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                employee.getId()
                        );

        // =====================================================
        // BUILD PROFILE DTO
        // =====================================================

        EmployeeProfileDTO dto =
                new EmployeeProfileDTO();

        // =====================================================
        // BASIC
        // =====================================================

        dto.setId(
                employee.getId()
        );

        dto.setName(
                employee.getName()
        );

        dto.setEmail(
                employee.getEmail()
        );

        dto.setRole(
                employee.getRole() != null
                        ? employee.getRole().name()
                        : null
        );

        // =====================================================
        // PERSONAL
        // =====================================================

        dto.setPhone(
                employee.getPhone()
        );

        dto.setDateOfBirth(
                employee.getDateOfBirth()
        );

        dto.setNationality(
                employee.getNationality()
        );

        dto.setAddress(
                employee.getAddress()
        );

        // =====================================================
        // EMPLOYMENT
        // =====================================================

        dto.setEmployeeId(
                getEmployeeId(employee)
        );

        dto.setJobTitle(
                employee.getJobTitle()
        );

        if (employee.getDepartment() != null) {

            dto.setDepartment(
                    employee.getDepartment()
                            .getName()
            );

        } else {

            dto.setDepartment(null);
        }

        dto.setJoiningDate(
                employee.getJoiningDate()
        );

        dto.setEmploymentType(
                employee.getEmploymentType()
        );

        dto.setEmploymentStatus(
                employee.getEmploymentStatus()
        );

        dto.setWorkLocation(
                employee.getWorkLocation()
        );

        dto.setWorkingHours(
                employee.getWorkingHours()
        );

        // =====================================================
        // MANAGER
        // =====================================================

        if (employee.getManager() != null) {

            dto.setManagerId(
                    employee.getManager().getId()
            );

            dto.setManagerName(
                    employee.getManager().getName()
            );

        } else {

            dto.setManagerId(
                    manager.getId()
            );

            dto.setManagerName(
                    manager.getName()
            );
        }

        // =====================================================
        // BANKING
        //
        // NOTE:
        // Banking data is intentionally NOT exposed here
        // to the Manager.
        //
        // The Manager should not receive:
        // IBAN
        // Account Number
        // Bank Account details
        // =====================================================

        dto.setBankName(null);
        dto.setBankBranch(null);
        dto.setAccountHolderName(null);
        dto.setAccountType(null);
        dto.setIban(null);
        dto.setAccountNumber(null);

        // =====================================================
        // REQUESTS
        // =====================================================

        List<EmployeeRequestDTO> requestDTOs =
                requests.stream()
                        .map(this::toEmployeeRequestDTO)
                        .toList();

        dto.setRequests(
                requestDTOs
        );

        return dto;
    }

    // =========================================================
    // CONVERT REQUEST TO EMPLOYEE REQUEST DTO
    // =========================================================

    private EmployeeRequestDTO toEmployeeRequestDTO(
            Request request
    ) {

        EmployeeRequestDTO dto =
                new EmployeeRequestDTO();

        dto.setId(
                request.getId()
        );

        dto.setTitle(
                request.getTitle()
        );

        dto.setDescription(
                request.getDescription()
        );

        dto.setStatus(
                request.getStatus()
        );

        dto.setCategory(
                request.getCategory()
        );

        dto.setPriority(
                request.getPriority()
        );

        dto.setDurationDays(
                request.getDurationDays()
        );

        dto.setCreatedAt(
                request.getCreatedAt()
        );

        dto.setApprovedAt(
                request.getApprovedAt()
        );

        dto.setApprovedBy(
                request.getApprovedBy()
        );

        boolean hasAttachment =
                requestAttachmentRepository
                        .findByRequest(request)
                        .isPresent();

        dto.setHasAttachment(
                hasAttachment
        );

        return dto;
    }

    // =========================================================
    // EMPLOYEE ID
    // =========================================================

    private String getEmployeeId(
            User user
    ) {

        if (
                user.getEmployeeId() != null
                        &&
                        !user.getEmployeeId().isBlank()
        ) {

            return user.getEmployeeId();
        }

        return String.format(
                "EMP-%05d",
                user.getId()
        );
    }

    // =========================================================
    // DEPARTMENT NAME NORMALIZER
    // =========================================================

    private String normalizeDepartmentName(
            String department
    ) {

        if (department == null ||
                department.trim().isBlank()) {

            throw new RuntimeException(
                    "Department is required"
            );
        }

        return department
                .trim()
                .replaceAll("\\s+", " ")
                .toUpperCase();
    }

}
