package com.netflix.conductor.core.config;

import java.util.Optional;

public interface ConfigProp {

    String TASKEXECLOG_INDEXING_ENABLED_PROPERTY_NAME = "conductor.app.taskExecLogIndexingEnabled";
    boolean TASKEXECLOG_INDEXING_ENABLED_DEFAULT_VALUE = true;
    int PRUNING_DAYS_TO_KEEP_DEFAULT_VALUE = 28;   // 4 weeks
    int PRUNING_BATCH_SIZE_DEFAULT_VALUE = 2000;

    /**
     * @param key         Name of the property
     * @param defaultValue Default value when not specified
     * @return User defined integer property.
     */
    default int getIntProperty(String key, int defaultValue) {

        String val = getProperty(key, Integer.toString(defaultValue));
        try {
            defaultValue = Integer.parseInt(val);
        } catch (NumberFormatException ignored) {
        }
        return defaultValue;
    }

    /**
     * @param key         Name of the property
     * @param defaultValue Default value when not specified
     * @return User defined string property.
     */
    default String getProperty(String key, String defaultValue) {

        String val;
        val = System.getenv(key.replace('.', '_'));
        if (val == null || val.isEmpty()) {
            val = Optional.ofNullable(System.getProperty(key))
                    .orElse(defaultValue);
        }
        return val;
    }

    default boolean getBooleanProperty(String name, boolean defaultValue) {

        String val = getProperty(name, null);

        if (val != null) {
            return Boolean.parseBoolean(val);
        } else {
            return defaultValue;
        }
    }

    default boolean getBoolProperty(String name, boolean defaultValue) {
        String value = getProperty(name, null);
        if (null == value || value.trim().length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    /**
     * @return if true(default), enables task execution log indexing
     */
    default boolean isTaskExecLogIndexingEnabled() {
        return getBooleanProperty(TASKEXECLOG_INDEXING_ENABLED_PROPERTY_NAME, TASKEXECLOG_INDEXING_ENABLED_DEFAULT_VALUE);
    }

    /**
     * @return number of days to keep workflows that are not 'Completed'
     */
    default int getPruningDaysToKeep()
    {
        return Integer.parseInt(System.getenv().getOrDefault("ENV_WORKFLOW_PRUNING_DAYS_TO_KEEP", Integer.toString(PRUNING_DAYS_TO_KEEP_DEFAULT_VALUE)));
    }

    /**
     * @return the number of records (wprkflows or tasks) to prune
     */
    default int getPruningBatchSize()
    {
        return Integer.parseInt(System.getenv().getOrDefault("ENV_WORKFLOW_PRUNING_BATCH_SIZE", Integer.toString(PRUNING_BATCH_SIZE_DEFAULT_VALUE)));
    }
}
