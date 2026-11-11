
package com.sahab.demo.modules.identity.controller;

import com.sahab.demo.modules.identity.dto.CreateUserDTO;
import com.sahab.demo.modules.identity.dto.EmployeeProfileDTO;
import com.sahab.demo.modules.identity.dto.UpdateUserDTO;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.Role;
import com.sahab.demo.exception.ResourceNotFoundException;
import com.sahab.demo.modules.identity.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {

    private final UserService userService;

    // =========================================================
    // GET ALL USERS
    // ADMIN ONLY
    // =========================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {

        try {

            List<User> users =
                    userService.getAllUsers();

            return ResponseEntity.ok(
                    users.stream()
                            .map(userService::toResponseDTO)
                            .toList()
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // GET ALL MANAGERS
    // ADMIN ONLY
    // =========================================================

    @GetMapping("/managers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllManagers() {

        try {

            List<User> managers =
                    userService.getAllManagers();

            List<Map<String, Object>> result =
                    managers.stream()
                            .map(manager ->
                                    Map.<String, Object>of(
                                            "id",
                                            manager.getId(),

                                            "name",
                                            manager.getName(),

                                            "email",
                                            manager.getEmail(),

                                            "role",
                                            manager.getRole()
                                    )
                            )
                            .toList();

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // MY TEAM
    // MANAGER ONLY
    // =========================================================

    @GetMapping("/my-team")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> getMyTeam() {

        try {

            User currentManager =
                    getCurrentUser();

            List<User> team =
                    userService.getMyTeam(
                            currentManager
                    );

            List<Map<String, Object>> result =
                    team.stream()
                            .map(user ->
                                    Map.<String, Object>of(

                                            "id",
                                            user.getId(),

                                            "name",
                                            user.getName(),

                                            "email",
                                            user.getEmail(),

                                            "role",
                                            user.getRole(),

                                            "department",
                                            user.getDepartment() != null
                                                    ? user.getDepartment()
                                                    .getName()
                                                    : "",

                                            "jobTitle",
                                            user.getJobTitle() != null
                                                    ? user.getJobTitle()
                                                    : "",

                                            "employeeId",
                                            user.getEmployeeId() != null
                                                    ? user.getEmployeeId()
                                                    : "",

                                            "employmentStatus",
                                            user.getEmploymentStatus() != null
                                                    ? user.getEmploymentStatus()
                                                    : ""
                                    )
                            )
                            .toList();

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // EMPLOYEE PROFILE
    // MANAGER ONLY
    //
    // GET:
    // /users/my-team/{employeeId}
    // =========================================================

    @GetMapping("/my-team/{employeeId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> getEmployeeProfile(
            @PathVariable Long employeeId
    ) {

        try {

            User currentManager =
                    getCurrentUser();

            EmployeeProfileDTO profile =
                    userService
                            .getEmployeeProfileForManager(
                                    employeeId,
                                    currentManager
                            );

            return ResponseEntity.ok(profile);

        } catch (ResourceNotFoundException e) {

            return ResponseEntity
                    .status(404)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // CURRENT AUTHENTICATED USER
    //
    // GET:
    // /users/me
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentAuthenticatedUser() {

        try {

            User currentUser =
                    getCurrentUser();

            return ResponseEntity.ok(
                    userService.toResponseDTO(
                            currentUser
                    )
            );

        } catch (ResourceNotFoundException e) {

            return ResponseEntity
                    .status(404)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // GET USER BY ID
    // ADMIN ONLY
    // =========================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(
            @PathVariable Long id
    ) {

        try {

            User user =
                    userService.getUserById(id);

            return ResponseEntity.ok(
                    userService.toResponseDTO(user)
            );

        } catch (ResourceNotFoundException e) {

            return ResponseEntity
                    .status(404)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // CREATE USER
    // ADMIN ONLY
    // =========================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(
            @RequestBody CreateUserDTO dto
    ) {

        try {

            User user =
                    userService.createUser(dto);

            return ResponseEntity
                    .status(201)
                    .body(
                            userService.toResponseDTO(user)
                    );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // UPDATE USER
    // ADMIN ONLY
    //
    // PUT:
    // /users/{id}
    //
    // BODY:
    // {
    //     "role": "MANAGER",
    //     "department": "IT",
    //     "managerId": null
    // }
    // =========================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserDTO dto
    ) {

        try {

            User currentAdmin =
                    getCurrentUser();

            User targetUser =
                    userService.getUserById(id);

            // =================================================
            // ADMIN CANNOT MODIFY THEMSELVES
            // =================================================

            if (currentAdmin.getId()
                    .equals(targetUser.getId())) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "You cannot modify your own user account"
                                )
                        );
            }

            // =================================================
            // EXISTING MANAGER PROTECTION
            // =================================================

            if (targetUser.getRole() == Role.MANAGER) {

                // Existing Manager cannot be downgraded

                if (dto.getRole() != null &&
                        dto.getRole() != Role.MANAGER) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    Map.of(
                                            "message",
                                            "Manager role cannot be changed"
                                    )
                            );
                }

                // Manager cannot be assigned to another Manager

                if (dto.getManagerId() != null) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    Map.of(
                                            "message",
                                            "A Manager cannot be assigned to another Manager"
                                    )
                            );
                }
            }

            // =================================================
            // UPDATE
            // =================================================

            User updatedUser =
                    userService.updateUser(
                            id,
                            dto
                    );

            return ResponseEntity.ok(
                    userService.toResponseDTO(
                            updatedUser
                    )
            );

        } catch (ResourceNotFoundException e) {

            return ResponseEntity
                    .status(404)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // UPDATE USER ROLE
    // ADMIN ONLY
    // =========================================================

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestParam Role role
    ) {

        try {

            User currentAdmin =
                    getCurrentUser();

            User targetUser =
                    userService.getUserById(id);

            // =================================================
            // ADMIN CANNOT CHANGE THEIR OWN ROLE
            // =================================================

            if (currentAdmin.getId()
                    .equals(targetUser.getId())) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "You cannot change your own role"
                                )
                        );
            }

            // =================================================
            // DO NOT CHANGE ANOTHER MANAGER'S ROLE
            // =================================================

            if (targetUser.getRole() == Role.MANAGER) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Manager role cannot be changed"
                                )
                        );
            }

            // =================================================
            // DO NOT CREATE MANAGER THROUGH THIS ENDPOINT
            // =================================================

            if (role == Role.MANAGER) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Manager role cannot be assigned here"
                                )
                        );
            }

            User updatedUser =
                    userService.updateUserRole(
                            id,
                            role
                    );

            return ResponseEntity.ok(
                    userService.toResponseDTO(
                            updatedUser
                    )
            );

        } catch (ResourceNotFoundException e) {

            return ResponseEntity
                    .status(404)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // ASSIGN MANAGER
    // ADMIN ONLY
    //
    // PUT:
    // /users/{userId}/manager/{managerId}
    // =========================================================

    @PutMapping("/{userId}/manager/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignManager(
            @PathVariable Long userId,
            @PathVariable Long managerId
    ) {

        try {

            User updatedUser =
                    userService.assignManager(
                            userId,
                            managerId
                    );

            return ResponseEntity.ok(
                    userService.toResponseDTO(
                            updatedUser
                    )
            );

        } catch (ResourceNotFoundException e) {

            return ResponseEntity
                    .status(404)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // DELETE USER
    // ADMIN ONLY
    // =========================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id
    ) {

        try {

            User currentAdmin =
                    getCurrentUser();

            User targetUser =
                    userService.getUserById(id);

            // =================================================
            // ADMIN CANNOT DELETE THEMSELVES
            // =================================================

            if (currentAdmin.getId()
                    .equals(targetUser.getId())) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "You cannot delete yourself"
                                )
                        );
            }

            userService.deleteUser(id);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "User deleted successfully"
                    )
            );

        } catch (ResourceNotFoundException e) {

            return ResponseEntity
                    .status(404)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // CURRENT USER HELPER
    // =========================================================

    private User getCurrentUser() {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        return userService.getUserByEmail(email);
    }
}
