package com.codearena.controller;

import com.codearena.model.CodeSnippet;
import com.codearena.service.CodeSnippetService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/snippets")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5090", "http://localhost:5173", "http://localhost:5174"})
public class CodeSnippetController {

    private final CodeSnippetService snippetService;

    @PostMapping
    public ResponseEntity<?> createSnippet(@RequestBody SnippetRequest request) {
        CodeSnippet snippet = snippetService.createSnippet(
            request.getUserId(),
            request.getTitle(),
            request.getCode(),
            request.getLanguage(),
            request.getDescription(),
            request.getTags(),
            request.getIsPublic()
        );
        return ResponseEntity.ok(snippet);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserSnippets(@PathVariable Long userId) {
        return ResponseEntity.ok(snippetService.getUserSnippets(userId));
    }

    @GetMapping("/public")
    public ResponseEntity<?> getPublicSnippets() {
        return ResponseEntity.ok(snippetService.getPublicSnippets());
    }

    @GetMapping("/share/{token}")
    public ResponseEntity<?> getSnippetByToken(@PathVariable String token) {
        return snippetService.getSnippetByToken(token)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/upvote")
    public ResponseEntity<?> upvote(@PathVariable Long id) {
        return ResponseEntity.ok(snippetService.upvoteSnippet(id));
    }

    @PostMapping("/{id}/downvote")
    public ResponseEntity<?> downvote(@PathVariable Long id) {
        return ResponseEntity.ok(snippetService.downvoteSnippet(id));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q) {
        return ResponseEntity.ok(snippetService.searchSnippets(q));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam Long userId) {
        snippetService.deleteSnippet(id, userId);
        return ResponseEntity.ok().build();
    }
}

@Data
class SnippetRequest {
    private Long userId;
    private String title;
    private String code;
    private String language;
    private String description;
    private String tags;
    private Boolean isPublic;
}
