package com.pb.todoapp.logic;

import com.pb.todoapp.TaskConfigurationProperties;
import com.pb.todoapp.model.*;
import com.pb.todoapp.model.projection.GroupReadModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private ProjectRepository projectRepository;
    private TaskGroupRepository taskGroupRepository;
    private TaskConfigurationProperties config;

    public ProjectService(
            ProjectRepository projectRepository,
            TaskGroupRepository taskGroupRepository,
            TaskConfigurationProperties config) {
        this.projectRepository = projectRepository;
        this.taskGroupRepository = taskGroupRepository;
        this.config = config;
    }

    public List<Project> readAll() {
        return projectRepository.findAll();
    }

    public Project save(Project toSave) {
        return projectRepository.save(toSave);
    }

    public GroupReadModel crateGroup(LocalDateTime deadline, int projectId) {
        if (!config.getTemplate().isAllowMultipleTasks() && taskGroupRepository.existsByDoneIsFalseAndProject_Id(projectId)) {
            throw new IllegalStateException("Only one undone group from project allowed");
        }
        TaskGroup taskGroup = projectRepository.findById(projectId)
                .map(project -> {
                    var result = new TaskGroup();
                    result.setDescription(project.getDescription());
                    result.setTasks(project.getSteps().stream()
                            .map(projectStep -> new Task(
                                    projectStep.getDescription(),
                                    deadline.plusDays(projectStep.getDaysToDeadline()))
                            ).collect(Collectors.toSet())
                    );
                    return result;
                }).orElseThrow(() -> new IllegalArgumentException("Project with given id not found"));
        return new GroupReadModel(taskGroup);
    }
}