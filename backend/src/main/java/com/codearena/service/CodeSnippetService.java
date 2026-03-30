package com.codearena.service;

import com.codearena.model.CodeSnippet;
import com.codearena.repository.CodeSnippetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CodeSnippetService {
    
    private final CodeSnippetRepository snippetRepo;

    public CodeSnippet createSnippet(Long userId, String title, String code, String language, String description, String tags, Boolean isPublic) {
        CodeSnippet snippet = new CodeSnippet();
        snippet.setUserId(userId);
        snippet.setTitle(title);
        snippet.setCode(code);
        snippet.setLanguage(language);
        snippet.setDescription(description);
        snippet.setTags(tags);
        snippet.setIsPublic(isPublic);
        snippet.setShareToken(UUID.randomUUID().toString().substring(0, 8));
        return snippetRepo.save(snippet);
    }

    public List<CodeSnippet> getUserSnippets(Long userId) {
        return snippetRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<CodeSnippet> getPublicSnippets() {
        return snippetRepo.findByIsPublicTrueOrderByUpvotesDesc();
    }

    public Optional<CodeSnippet> getSnippetByToken(String token) {
        Optional<CodeSnippet> snippet = snippetRepo.findByShareToken(token);
        snippet.ifPresent(s -> {
            s.setViews(s.getViews() + 1);
            snippetRepo.save(s);
        });
        return snippet;
    }

    public CodeSnippet upvoteSnippet(Long snippetId) {
        Optional<CodeSnippet> snippet = snippetRepo.findById(snippetId);
        if (snippet.isPresent()) {
            CodeSnippet s = snippet.get();
            s.setUpvotes(s.getUpvotes() + 1);
            return snippetRepo.save(s);
        }
        return null;
    }

    public CodeSnippet downvoteSnippet(Long snippetId) {
        Optional<CodeSnippet> snippet = snippetRepo.findById(snippetId);
        if (snippet.isPresent()) {
            CodeSnippet s = snippet.get();
            s.setDownvotes(s.getDownvotes() + 1);
            return snippetRepo.save(s);
        }
        return null;
    }

    public List<CodeSnippet> searchSnippets(String query) {
        return snippetRepo.searchPublicSnippets(query);
    }

    public void deleteSnippet(Long snippetId, Long userId) {
        Optional<CodeSnippet> snippet = snippetRepo.findById(snippetId);
        if (snippet.isPresent() && snippet.get().getUserId().equals(userId)) {
            snippetRepo.deleteById(snippetId);
        }
    }
}
