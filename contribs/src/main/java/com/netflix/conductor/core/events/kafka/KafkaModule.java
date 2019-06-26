package com.netflix.conductor.core.events.kafka;

import com.google.inject.AbstractModule;
import com.netflix.conductor.core.execution.TaskStatusPublisher;

public class KafkaModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TaskStatusPublisher.class).to(KafkaObservableQueue.class);
    }
}
