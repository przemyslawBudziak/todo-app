package com.pb.todoapp.logic;

import com.pb.todoapp.model.TaskGroup;
import com.pb.todoapp.model.TaskGroupRepository;
import com.pb.todoapp.model.TaskRepository;
import com.pb.todoapp.model.projection.GroupReadModel;
import com.pb.todoapp.model.projection.GroupWriteModel;


import java.util.List;
import java.util.stream.Collectors;

public class TaskGroupService {
    private TaskGroupRepository repository;
    private TaskRepository taskRepository;

    TaskGroupService(final TaskGroupRepository repository, final TaskRepository taskRepository) {
        this.repository = repository;
        this.taskRepository = taskRepository;
    }

    public GroupReadModel createGroup(GroupWriteModel source) {
        TaskGroup result = repository.save(source.toGroup());
        return new GroupReadModel(result);
    }

    public List<GroupReadModel> readAll() {
        return repository.findAll().stream()
                .map(GroupReadModel::new)
                .collect(Collectors.toList());
    }

    public void toggleGroup(int groupId) {
        if (taskRepository.existsByDoneIsFalseAndGroup_Id(groupId)) {
            throw new IllegalStateException("Group has undone tasks");
        }
        TaskGroup result = repository.findById(groupId).orElseThrow(() -> new IllegalArgumentException("TaskGroup not found"));
        result.setDone(!result.isDone());
        repository.save(result);
    }
}
