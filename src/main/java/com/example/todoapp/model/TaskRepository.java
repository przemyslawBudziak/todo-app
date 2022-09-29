package com.example.todoapp.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

// for communication with DB
public interface TaskRepository extends JpaRepository<Task, Integer> {

    // turning off delete method
    @Override
    @RestResource(exported = false)
    void deleteById(Integer integer);

    @Override
    @RestResource(exported = false)
    void delete(Task entity);

    @RestResource(path = "done", rel = "done")
    List<Task> findByDone(@Param("state") boolean done);

}
