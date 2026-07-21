package com.javarush.jira.bugtracking.task;

import com.javarush.jira.common.error.IllegalRequestDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TaskRepository taskRepository;

    public Set<String> getTags(long taskId) {
        Task task = taskRepository.getExisted(taskId);
        return Set.copyOf(task.getTags());
    }

    @Transactional
    public void addTag(long taskId, String tag) {
        if (tag == null || tag.isBlank()) {
            throw new IllegalRequestDataException("Tag must not be empty");
        }
        if (tag.length() < 2 || tag.length() > 32) {
            throw new IllegalRequestDataException("Tag length must be between 2 and 32 characters");
        }
        Task task = taskRepository.getExisted(taskId);
        if (task.getTags().contains(tag)) {
            throw new IllegalRequestDataException("Tag '" + tag + "' already exists for task " + taskId);
        }
        task.getTags().add(tag);
        taskRepository.save(task);
    }

    @Transactional
    public void removeTag(long taskId, String tag) {
        Task task = taskRepository.getExisted(taskId);
        if (!task.getTags().remove(tag)) {
            throw new IllegalRequestDataException("Tag '" + tag + "' not found for task " + taskId);
        }
        taskRepository.save(task);
    }
}
