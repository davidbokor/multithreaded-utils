package com.bokor.multithreaded;

import java.time.Duration;

public class ExecutionStatistics {

    private final long count;
    private final Duration totalExecutionTime;

    public ExecutionStatistics(long count, Duration totalExecutionTime) {
        this.count = count;
        this.totalExecutionTime = totalExecutionTime;
    }

    /**
     * Get the number of requests processed
     */
    public long getCount() {
        return count;
    }

    /**
     * Get the total amount of time executing requests - the time between the start of executing the first task and
     * the completion of the last.
     */
    public Duration getTotalExecutionTime() {
        return totalExecutionTime;
    }
}
