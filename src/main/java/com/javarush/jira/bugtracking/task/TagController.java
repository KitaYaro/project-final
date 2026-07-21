package com.javarush.jira.bugtracking.task;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Tag(name = "Tags", description = "Tag management for tasks")
@RestController
@RequestMapping(value = TagController.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class TagController {
    public static final String REST_URL = "/api/tasks";

    private final TagService tagService;

    @GetMapping("/{id}/tags")
    @Operation(summary = "Get all tags for a task")
    public Set<String> getTags(@PathVariable long id) {
        return tagService.getTags(id);
    }

    @PostMapping("/{id}/tags")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a tag to a task")
    public void addTag(@PathVariable long id, @RequestParam String tag) {
        tagService.addTag(id, tag);
    }

    @DeleteMapping("/{id}/tags/{tag}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a tag from a task")
    public void removeTag(@PathVariable long id, @PathVariable String tag) {
        tagService.removeTag(id, tag);
    }
}
