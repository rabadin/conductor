
package com.netflix.conductor.core.events.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.core.execution.TaskStatusPublisher;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class KafkaObservableQueue implements TaskStatusPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaObservableQueue.class);
    @Override
    public void publishTaskStatus(Task task) {
        publish(task);
    }

    public void publish(Task task) {
        String input = get_TaskStatus(task);
        LOGGER.info(input);
        try {
            Client client = Client.create();
            WebResource webResource = client
                    .resource("http://bullwinkle.default:7979/v1/workflow/TaskNotifications");
            ClientResponse response = webResource.type("application/json")
                    .post(ClientResponse.class, input);

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatus());
            }
            LOGGER.info("Record sent with Status " + response.getEntity(String.class));

        } catch (Exception e) {

            e.printStackTrace();

        }
    }
    public static String get_TaskStatus(Task task){
        String status="";


        try {
            ObjectMapper Obj = new ObjectMapper();
            ScheduledTask st= new ScheduledTask();
            st.setCorrelationId(task.getCorrelationId());
            st.setWorkflowId(task.getWorkflowInstanceId());
            st.setTaskId(task.getTaskId());
            st.setRetryCount(task.getRetryCount());
            st.setStatus(String.valueOf(task.getStatus()));
            st.setEndTime(String.valueOf(task.getEndTime()));
            st.setStartTime(String.valueOf(task.getStartTime()));
            st.setScheduledTime(String.valueOf(task.getScheduledTime()));
            st.setInput(new ObjectMapper().writeValueAsString(task.getInputData()));
            st.setOutput(new ObjectMapper().writeValueAsString(task.getOutputData()));
            st.setUpdateTime(String.valueOf(task.getUpdateTime()));
            st.setTaskDescription(task.getTaskDescription());
            st.setReferenceTaskName(task.getReferenceTaskName());
            st.setQueueWaitTime(String.valueOf(task.getQueueWaitTime()));
            st.setReasonForIncompletion(task.getReasonForIncompletion());
            st.setTaskType(task.getTaskType());
            st.setTaskDefName(task.getTaskDefName());
            st.setWorkflowType(task.getWorkflowType());

            status = Obj.writeValueAsString(st);


        }
        catch (IOException e){
            e.printStackTrace();
        }


        return status;
    }
    public void publishWorkflowStatus(Workflow workflow){
        String input = get_WorkflowStatus(workflow);
        LOGGER.info(input);
        try {
            Client client = Client.create();
            WebResource webResource = client
                    .resource("http://bullwinkle.default:7979/v1/workflow/WorkflowCompletionNotifications");
            ClientResponse response = webResource.type("application/json")
                    .post(ClientResponse.class, input);

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatus());
            }
            LOGGER.info("Record sent with Status " + response.getEntity(String.class));

        } catch (Exception e) {

            e.printStackTrace();

        }
    }
    public String get_WorkflowStatus(Workflow workflow){
        String status="";


        try {
            ObjectMapper Obj = new ObjectMapper();
            CompletedWorkflow completedWorkflow= new CompletedWorkflow();
            completedWorkflow.setCorrelationId(workflow.getCorrelationId());
            completedWorkflow.setEndTime(workflow.getEndTime());
            completedWorkflow.setEvent(workflow.getEvent());
            completedWorkflow.setExecutionTime(workflow.getEndTime()-workflow.getStartTime());
            completedWorkflow.setFailedReferenceTaskNames(new ObjectMapper().writeValueAsString(workflow.getFailedReferenceTaskNames()));
            completedWorkflow.setInput(new ObjectMapper().writeValueAsString(workflow.getInput()));
            completedWorkflow.setOutput(new ObjectMapper().writeValueAsString(workflow.getOutput()));
            completedWorkflow.setReasonForIncompletion(workflow.getReasonForIncompletion());
            completedWorkflow.setWorkflowType(workflow.getWorkflowType());
            completedWorkflow.setWorkflowId(workflow.getWorkflowId());
            status = Obj.writeValueAsString(completedWorkflow);

        }
        catch (IOException e){
            e.printStackTrace();
        }


        return status;
    }
}


