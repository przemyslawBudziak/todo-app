package com.pb.todoapp.logic;

import com.pb.todoapp.TaskConfigurationProperties;
import com.pb.todoapp.model.ProjectRepository;
import com.pb.todoapp.model.TaskGroupRepository;
import com.pb.todoapp.model.TaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class LogicConfiguration {
    @Bean
    ProjectService projectService(
            final ProjectRepository repository,
            final TaskGroupRepository taskGroupRepository,
            final TaskConfigurationProperties configurationProperties
            ) {
        return new ProjectService(repository, taskGroupRepository, configurationProperties);
    }

    @Bean
    TaskGroupService taskGroupService(final TaskGroupRepository repository, final TaskRepository taskRepository){
        return new TaskGroupService(repository, taskRepository);
    }
}
