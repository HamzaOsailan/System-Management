package com.sahab.demo.modules.ai.controller;

import com.sahab.demo.modules.ai.dto.ChatRequestDTO;
import com.sahab.demo.modules.ai.dto.ChatResponseDTO;
import com.sahab.demo.modules.ai.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<ChatResponseDTO> chat(
            @Valid @RequestBody ChatRequestDTO request
    ) {
        return ResponseEntity.ok(
                chatService.chat(
                        request.getMessage()
                )
        );
    }
}