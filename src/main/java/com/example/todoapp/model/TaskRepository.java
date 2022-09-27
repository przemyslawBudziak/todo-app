package com.example.todoapp.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
// for communication with DB
public interface TaskRepository extends JpaRepository<Task, Integer> {

}
