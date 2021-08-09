package com.netflix.conductor.contribs.publisher;

import com.netflix.conductor.common.run.Workflow;

/**
 * Listener for the completed and terminated workflows
 *
 */
public interface WorkflowStatusListener {

    void onWorkflowCompleted(Workflow workflow);
    void onWorkflowTerminated(Workflow workflow);
}
