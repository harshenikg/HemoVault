package com.hemovault.controller;

import com.hemovault.dto.*;
import com.hemovault.model.BloodGroup;
import com.hemovault.model.User;
import com.hemovault.repository.UserRepository;
import com.hemovault.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

// ─────────────────────────────────────────────
// PredictionController
// ─────────────────────────────────────────────
@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
class PredictionController {

    private final PredictionService predictionService;

    /** GET /api/predictions — all group predictions */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PredictionResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(predictionService.getPredictions()));
    }

    /** GET /api/predictions/{bloodGroup} — prediction for one blood group */
    @GetMapping("/{bloodGroup}")
    public ResponseEntity<ApiResponse<PredictionResponse>> getForGroup(
            @PathVariable BloodGroup bloodGroup) {
        return ResponseEntity.ok(ApiResponse.ok(predictionService.getPredictionForGroup(bloodGroup)));
    }
}

// ─────────────────────────────────────────────
// PermissionController
// ─────────────────────────────────────────────
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
class PermissionController {

    private final PermissionRequestService permService;

    /** POST /api/permissions — submit admin access request */
    @PostMapping
    public ResponseEntity<ApiResponse<PermissionRequestResponse>> request(
            @Valid @RequestBody PermissionRequestDto dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok("Permission request submitted",
                permService.create(dto, user)));
    }

    /** GET /api/permissions/pending — admin only */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PermissionRequestResponse>>> getPending() {
        return ResponseEntity.ok(ApiResponse.ok(permService.getPending()));
    }

    /** PUT /api/permissions/{id}/grant — admin grants access */
    @PutMapping("/{id}/grant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PermissionRequestResponse>> grant(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.ok("Admin access granted", permService.grant(id, admin)));
    }

    /** PUT /api/permissions/{id}/deny */
    @PutMapping("/{id}/deny")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PermissionRequestResponse>> deny(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.ok("Request denied", permService.deny(id, admin)));
    }
}

// ─────────────────────────────────────────────
// AdminController
// ─────────────────────────────────────────────
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
class AdminController {

    private final UserRepository userRepo;
    private final AuthService authService;

    /** GET /api/admin/users — all users */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userRepo.findAll().stream()
                .map(authService::toUserResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    /** PUT /api/admin/users/{id}/toggle-admin */
    @PutMapping("/users/{id}/toggle-admin")
    public ResponseEntity<ApiResponse<UserResponse>> toggleAdmin(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentAdmin) {
        if (id.equals(currentAdmin.getId()))
            throw new IllegalArgumentException("Cannot toggle your own admin status");
        User target = userRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + id));
        target.setIsAdmin(!target.getIsAdmin());
        userRepo.save(target);
        return ResponseEntity.ok(ApiResponse.ok("Admin status toggled", authService.toUserResponse(target)));
    }

    /** DELETE /api/admin/users/{id} */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentAdmin) {
        if (id.equals(currentAdmin.getId()))
            throw new IllegalArgumentException("Cannot delete your own account");
        if (!userRepo.existsById(id))
            throw new NoSuchElementException("User not found: " + id);
        userRepo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
    }

    /** GET /api/admin/dashboard/stats */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        long total = userRepo.count();
        long admins = userRepo.findAll().stream().filter(u -> Boolean.TRUE.equals(u.getIsAdmin())).count();
        return ResponseEntity.ok(ApiResponse.ok(Map.of("totalUsers", total, "adminUsers", admins)));
    }

    /** PUT /api/admin/permissions/{userId}/revoke */
    @PutMapping("/permissions/{userId}/revoke")
    public ResponseEntity<ApiResponse<UserResponse>> revokeAdmin(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentAdmin) {
        if (userId.equals(currentAdmin.getId()))
            throw new IllegalArgumentException("Cannot revoke your own admin access");
        User target = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        target.setIsAdmin(false);
        userRepo.save(target);
        return ResponseEntity.ok(ApiResponse.ok("Admin access revoked", authService.toUserResponse(target)));
    }
}

// ─────────────────────────────────────────────
// ChatbotController
// ─────────────────────────────────────────────
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
class ChatbotController {

    private final ChatbotService chatbotService;

    /** POST /api/chatbot — send message to AI assistant */
    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Valid @RequestBody ChatRequest req,
            @AuthenticationPrincipal User user) {
        String sessionId = req.getSessionId() != null ? req.getSessionId() : UUID.randomUUID().toString();
        String response = chatbotService.chat(req.getMessage(), user);
        return ResponseEntity.ok(ApiResponse.ok(ChatResponse.builder()
                .response(response)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now())
                .build()));
    }
}
