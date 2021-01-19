package com.netflix.conductor.contribs.publisher;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.core.execution.TaskStatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Singleton
public class TaskStatusPublisher implements TaskStatusListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskStatusPublisher.class);
    //private static final String NOTIFICATION_TYPE = "workflow/TaskNotifications";
    private static final Integer QDEPTH = Integer.parseInt(System.getenv().getOrDefault("ENV_TASK_NOTIFICATION_QUEUE_SIZE", "50"));
    private BlockingQueue<Task> blockingQueue = new LinkedBlockingDeque<>(QDEPTH);

    private RestClientManager rcm;

    class ExceptionHandler implements Thread.UncaughtExceptionHandler
    {
        public void uncaughtException(Thread t, Throwable e)
        {
            LOGGER.info("An exception has been captured\n");
            LOGGER.info("Thread: {}\n", t.getName());
            LOGGER.info("Exception: {}: {}\n", e.getClass().getName(), e.getMessage());
            LOGGER.info("Stack Trace: \n");
            e.printStackTrace(System.out);
            LOGGER.info("Thread status: {}\n", t.getState());
            new ConsumerThread().start();
        }
    }

    class ConsumerThread extends Thread {

        public void run(){
            this.setUncaughtExceptionHandler(new ExceptionHandler());
            String tName = Thread.currentThread().getName();
            LOGGER.info("{}: Starting consumer thread", tName);
            Task task;
            TaskNotification taskNotification;
            String jsonTask;
            LOGGER.info("##### 5555");
            while (true) {
                try {
                    LOGGER.info("##### 6666");
                    task = blockingQueue.take();
                    LOGGER.info("##### 6666xxx");
                    if (task == null) {
                        continue;
                    }
                    LOGGER.info("##### 7777");
                    taskNotification = new TaskNotification(task);
                    jsonTask = taskNotification.toJsonString();
                    LOGGER.info("Start Publishing TaskNotification: {}", jsonTask);
                    if (taskNotification.getTaskType().equals("SUB_WORKFLOW")) {
                        LOGGER.info("Skip task '{}' notification. Task type is SUB_WORKFLOW.", taskNotification.getTaskId());
                        continue;
                    }
                    if (taskNotification.getAccountMoId().equals("")) {
                        LOGGER.info("Skip task '{}' notification. Account Id is empty.", taskNotification.getTaskId());
                        continue;
                    }
                    if (taskNotification.getDomainGroupMoId().equals("")) {
                        LOGGER.info("Skip task '{}' notification. Domain group is empty.", taskNotification.getTaskId());
                        continue;
                    }
                    publishTaskNotification(taskNotification);
                    LOGGER.info("##### 8888");
                    //blockingQueue.take();
                    LOGGER.info("##### 9999");
                    LOGGER.debug("Task {} publish is successful.", taskNotification.getTaskId());
                    Thread.sleep(5);
                }
                catch (Exception e) {
                    LOGGER.error("Failed to publish task: {} to String. Exception: {} ", this, e);
                    e.printStackTrace(System.out);
                    LOGGER.error(e.getMessage());
                }
            }
        }
    }

    @Inject
    public TaskStatusPublisher(RestClientManager rcm) {
        this.rcm = rcm;
        LOGGER.info("RRRRR RestClientManager {}", rcm.hashCode());
        ConsumerThread consumerThread = new ConsumerThread();
        LOGGER.info("##### 3333");
        consumerThread.start();
        LOGGER.info("##### 4444");
    }

    @Override
    public void onTaskScheduled(Task task) {
        try {
            LOGGER.info("##### 1111");
            blockingQueue.put(task);
            LOGGER.info("##### 2222");
        } catch (Exception e){
            LOGGER.error("Failed to enqueue task: {} to String. Exception: {}", this, e);
            LOGGER.error(e.getMessage());
        }
    }

    private void publishTaskNotification(TaskNotification taskNotification) throws IOException {
        String jsonTask = taskNotification.toJsonString();
        LOGGER.info(rcm.toString());
        //String url = rc.createUrl(NOTIFICATION_TYPE);
        rcm.postNotification(
                RestClientManager.NotificationType.TASK,
                jsonTask,
                taskNotification.getDomainGroupMoId(),
                taskNotification.getAccountMoId(),
                taskNotification.getTaskId());
    }

}