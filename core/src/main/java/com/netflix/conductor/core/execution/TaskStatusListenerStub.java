package com.netflix.conductor.core.execution;

import com.netflix.conductor.common.metadata.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskStatusListenerStub implements TaskStatusListener{

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskStatusListenerStub.class);

    @Override
    public void onTaskScheduled(Task task) {

        LOGGER.debug("Task {} is scheduled", task.getTaskId());
    }
}
