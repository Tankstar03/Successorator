package edu.ucsd.cse110.successorator.data.db;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.ucsd.cse110.successorator.lib.domain.ITasksRepository;
import edu.ucsd.cse110.successorator.lib.domain.Task;
import edu.ucsd.cse110.successorator.lib.util.Subject;
import edu.ucsd.cse110.successorator.util.LiveDataSubjectAdapter;

public class RoomTasksRepository implements ITasksRepository {
    private final TaskDao tasksDao;

    public RoomTasksRepository(TaskDao tasksDao) {
        this.tasksDao = tasksDao;
    }

    @Override
    public Subject<Task> find(int id) {
        LiveData<TaskEntity> entityLiveData = tasksDao.findAsLiveData(id);
        LiveData<Task> taskLiveData = Transformations.map(entityLiveData, TaskEntity::toTask);
        return new LiveDataSubjectAdapter<>(taskLiveData);
    }

    @Override
    public Subject<List<Task>> findAll() {
        var entityLiveData = tasksDao.findAllAsLiveData();
        var taskLiveData = Transformations.map(entityLiveData, entities -> {
            return entities.stream().map(TaskEntity::toTask).collect(Collectors.toList());
        });
        return new LiveDataSubjectAdapter<>(taskLiveData);
    }

    @Override
    public void save(Task task) {
        tasksDao.insert(TaskEntity.fromTask(task));
    }

    @Override
    public void save(List<Task> tasks) {
        var entities = tasks.stream().map(TaskEntity::fromTask).collect(Collectors.toList());
        tasksDao.insert(entities);
    }

    @Override
    public void append(Task task) {
        tasksDao.append(TaskEntity.fromTask(task));
    }

    @Override
    public void appendToEndOfUnfinishedTasks(Task task) {
        int maxSortOrder = tasksDao.getMaxSortOrder();
        int newSortOrder = maxSortOrder + 1;

        List<Task> tasks = Objects.requireNonNull(tasksDao.findAll().stream().map(TaskEntity::toTask).collect(Collectors.toList()));
        Optional<Task> firstCheckedOff = tasks.stream()
                .sorted(Comparator.comparing(Task::sortOrder))
                .filter(Task::getCheckOff)
                .findFirst();

        if (firstCheckedOff.isPresent()) {
            newSortOrder = firstCheckedOff.get().sortOrder();
            System.out.println(firstCheckedOff.get().getTask());
            System.out.println(newSortOrder);
        }

        tasksDao.shiftSortOrders(newSortOrder, maxSortOrder, 1);
        save(new Task(task.id(), task.getTask(), newSortOrder, task.getCheckOff()));
    }

    @Override
    public void toggleTaskStrikethrough(Task task) {
        boolean newState = !(task.getCheckOff());
        remove(task.id());

        if (!task.getCheckOff()) {
            appendToEndOfUnfinishedTasks(task.withCheckOff(newState));
        } else {
            prepend(task.withCheckOff(newState));
        }
    }

    @Override
    public void prepend(Task task) {
        tasksDao.prepend(TaskEntity.fromTask(task));
    }

    @Override
    public int size() {
        return tasksDao.count();
    }

    @Override
    public void remove(int id) {
        tasksDao.delete(id);
    }
}
