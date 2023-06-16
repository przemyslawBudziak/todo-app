package com.pb.todoapp.controller;

import com.pb.todoapp.model.Task;
import com.pb.todoapp.model.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskControllerE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    TaskRepository repository;

    @Test
    void httpGet_returnsAllTasks() {
        int initial = repository.findAll().size();
        repository.save(new Task("aaa", LocalDateTime.now()));
        repository.save(new Task("bbb", LocalDateTime.now()));

        Task[] result = restTemplate.getForObject("http://localhost:" + port + "/tasks", Task[].class);

        assertThat(result).hasSize(initial + 2);
    }
}
