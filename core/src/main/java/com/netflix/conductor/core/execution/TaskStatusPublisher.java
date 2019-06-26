package com.netflix.conductor.core.execution;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.run.Workflow;

public interface TaskStatusPublisher {
    default void publishTaskStatus(Task task) {

    }
    default void publishWorkflowStatus(Workflow workflow){

    }
}
