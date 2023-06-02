package com.pb.todoapp.logic;

import com.pb.todoapp.TaskConfigurationProperties;
import com.pb.todoapp.model.*;
import com.pb.todoapp.model.projection.GroupReadModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjectServiceTest {

    @Test
    @DisplayName("should throw IllegalStateException when configured to allow just 1 group and the other undone group exist")
    void crateGroup_noMultipleGroupsConfig_And_undoneGroupExists_throwsIllegalStateException() {
        TaskGroupRepository mockGroupRepository = groupRepositoryReturning(true);
        TaskConfigurationProperties mockTaskConfigurationProperties = configurationReturning(false);
        var toTest = new ProjectService(null, mockGroupRepository, mockTaskConfigurationProperties);

        var exception = catchThrowable(() -> toTest.crateGroup(LocalDateTime.now(), 0));

        assertThat(exception)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one undone group");

        assertThatIllegalStateException()
                .isThrownBy(() -> toTest.crateGroup(LocalDateTime.now(), 0));

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> toTest.crateGroup(LocalDateTime.now(), 0));

        assertThatThrownBy(() -> toTest.crateGroup(LocalDateTime.now(), 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when configuration ok and no projects for a given id")
    void crateGroup_configurationOk_And_noProjects_throwsIllegalArgumentException() {
        var mockRepository = mock(ProjectRepository.class);
        when(mockRepository.findById(anyInt())).thenReturn(Optional.empty());
        TaskConfigurationProperties mockTaskConfigurationProperties = configurationReturning(true);
        var toTest = new ProjectService(mockRepository, null, mockTaskConfigurationProperties);

        var exception = catchThrowable(() -> toTest.crateGroup(LocalDateTime.now(), 0));

        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id not found");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when configured to allow just 1 group no groups for a given id")
    void crateGroup_noMultipleGroupsConfiguration_and_noUndoneGroupExists_noProjects_throwsIllegalArgumentException() {
        var mockRepository = mock(ProjectRepository.class);
        when(mockRepository.findById(anyInt())).thenReturn(Optional.empty());
        TaskGroupRepository mockGroupRepository = groupRepositoryReturning(false);
        TaskConfigurationProperties mockTaskConfigurationProperties = configurationReturning(true);
        var toTest = new ProjectService(mockRepository, mockGroupRepository, mockTaskConfigurationProperties);

        var exception = catchThrowable(() -> toTest.crateGroup(LocalDateTime.now(), 0));

        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id not found");
    }

    @Test
    @DisplayName("should create a new group from project")
    void crateGroup_configurationOk_existingProject_createsAndSavesGroup() {
        var today = LocalDate.now().atStartOfDay();
        var project = projectWith("bar", Set.of(-1, -2));
        var mockRepository = mock(ProjectRepository.class);
        when(mockRepository.findById(anyInt()))
                .thenReturn(Optional.of(project));
        inMemoryGroupRepository inMemoryGroupRepository = inMemoryGroupRepository();
        int countBeforeCall = inMemoryGroupRepository.count();
        TaskConfigurationProperties mockTaskConfigurationProperties = configurationReturning(true);

        var toTest = new ProjectService(mockRepository, inMemoryGroupRepository, mockTaskConfigurationProperties);

        GroupReadModel result = toTest.crateGroup(today, 1);

        assertThat(result.getDescription()).isEqualTo("bar");
        assertThat(result.getDeadline()).isEqualTo(today.minusDays(1));
        assertThat(result.getTasks()).allMatch(task -> task.getDescription().equals("foo"));
        assertThat(countBeforeCall + 1).isEqualTo(inMemoryGroupRepository.count());

    }

    private Project projectWith(String projectDescription, Set<Integer> daysToDeadline) {
        Set<ProjectStep> steps = daysToDeadline.stream()
                .map(days -> {
                    var step = mock(ProjectStep.class);
                    when(step.getDescription()).thenReturn("foo");
                    when(step.getDaysToDeadline()).thenReturn(days);
                    return step;
                }).collect(Collectors.toSet());
        var result = mock(Project.class);
        when(result.getDescription()).thenReturn(projectDescription);
        when(result.getSteps()).thenReturn(steps);

        return result;
    }

    private static TaskGroupRepository groupRepositoryReturning(boolean result) {
        var mockGroupRepository = mock(TaskGroupRepository.class);
        when(mockGroupRepository.existsByDoneIsFalseAndProject_Id(anyInt())).thenReturn(result);
        return mockGroupRepository;
    }

    private static TaskConfigurationProperties configurationReturning(final boolean result) {
        var mockTemplate = mock(TaskConfigurationProperties.Template.class);
        when(mockTemplate.isAllowMultipleTasks()).thenReturn(result);
        var mockTaskConfigurationProperties = mock(TaskConfigurationProperties.class);
        when(mockTaskConfigurationProperties.getTemplate()).thenReturn(mockTemplate);
        return mockTaskConfigurationProperties;
    }

    private inMemoryGroupRepository inMemoryGroupRepository() {
        return new inMemoryGroupRepository();
    }

    private static class inMemoryGroupRepository implements TaskGroupRepository {
        private int index = 0;
        private Map<Integer, TaskGroup> map = new HashMap<>();

        public int count() {
            return map.values().size();
        }

        @Override
        public List<TaskGroup> findAll() {
            return new ArrayList<>(map.values());
        }

        @Override
        public Optional<TaskGroup> findById(Integer id) {
            return Optional.ofNullable(map.get(id));
        }

        @Override
        public TaskGroup save(TaskGroup entity) {
            if (entity.getId() == 0) {
                try {
                    var field = TaskGroup.class.getDeclaredField("id");
                    field.setAccessible(true);
                    field.set(entity, ++index);
                } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            map.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public boolean existsByDoneIsFalseAndProject_Id(Integer projectId) {
            return map.values().stream()
                    .filter(group -> !group.isDone())
                    .anyMatch(group -> group.getProject() != null && group.getProject().getId() == projectId);
        }
    }

}

