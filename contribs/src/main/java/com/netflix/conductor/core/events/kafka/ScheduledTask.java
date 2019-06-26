package com.netflix.conductor.core.events.kafka;


public class ScheduledTask {
    public String workflowId;
    public String taskId;
    public String workflowType;
    public String correlationId;
    public String scheduledTime;
    public String startTime;
    public String updateTime;
    public String endTime;
    public String status;
    public String reasonForIncompletion;
    public String executionTime;
    public String queueWaitTime;
    public String taskDefName;
    public String taskType;
    public String input;
    public String output;
    public String referenceTaskName;
    public String taskDescription;
    public int retryCount;

    public String getWorkflowId() {
        return workflowId;
    }
    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }
    public String getTaskId() {
        return taskId;
    }
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    public String getWorkflowType() {
        return workflowType;
    }
    public void setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
    }
    public String getCorrelationId() {
        return correlationId;
    }
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    public String getScheduledTime() {
        return scheduledTime;
    }
    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
    public String getStartTime() {
        return startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public String getUpdateTime() {
        return updateTime;
    }
    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
    public String getEndTime() {
        return endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getReasonForIncompletion() {
        return reasonForIncompletion;
    }
    public void setReasonForIncompletion(String reasonForIncompletion) {
        this.reasonForIncompletion = reasonForIncompletion;
    }
    public String getExecutionTime() {
        return executionTime;
    }
    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }
    public String getQueueWaitTime() {
        return queueWaitTime;
    }
    public void setQueueWaitTime(String queueWaitTime) {
        this.queueWaitTime = queueWaitTime;
    }
    public String getTaskDefName() {
        return taskDefName;
    }
    public void setTaskDefName(String taskDefName) {
        this.taskDefName = taskDefName;
    }
    public String getTaskType() {
        return taskType;
    }
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    public String getInput() {
        return input;
    }
    public void setInput(String input) {
        this.input = input;
    }
    public String getOutput() {
        return output;
    }
    public void setOutput(String output) {
        this.output = output;
    }
    public String getReferenceTaskName() {
        return referenceTaskName;
    }
    public void setReferenceTaskName(String referenceTaskName) {
        this.referenceTaskName = referenceTaskName;
    }
    public String getTaskDescription() {
        return taskDescription;
    }
    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }
    public int getRetryCount() {
        return retryCount;
    }
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }



}
