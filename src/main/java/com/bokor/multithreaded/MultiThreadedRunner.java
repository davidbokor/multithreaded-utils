package com.bokor.multithreaded;

import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class MultiThreadedRunner<INPUT, OUTPUT> implements Closeable {

    /**
     * A number to multiply against the number of threads to make sure the input queue isn't drained faster than it
     * populates. For example, if we have 10 threads and a multiplier of 3, the input queue will have a length of 30.
     */
    private static final int PRODUCER_QUEUE_LENGTH_MULTIPLIER = 3;

    private final Producer<INPUT> producer;
    private final Executor<INPUT, OUTPUT> executor;
    private final Consumer<INPUT, OUTPUT> consumer;

    private final int threads;

    public MultiThreadedRunner(Producer<INPUT> producer, Executor<INPUT, OUTPUT> executor, Consumer<INPUT, OUTPUT> consumer, int threads) {
        this.producer = producer;
        this.executor = executor;
        this.consumer = consumer;
        this.threads = threads;
    }

    public ExecutionStatistics run() {
        AtomicLong execStart = new AtomicLong(Long.MAX_VALUE);
        AtomicLong execEnd = new AtomicLong();

        // a fixed-size buffer for requests from the consumer
        BlockingQueue<INPUT> requestQueue = new LinkedBlockingDeque<>(threads * PRODUCER_QUEUE_LENGTH_MULTIPLIER);
        AtomicLong requestsSeen = new AtomicLong();

        // a buffer for responses from the executor
        AtomicLong requestsExecuted = new AtomicLong();

        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        for (int n = 0; n < threads; n++) {
            executorService.submit(() -> {
                while (producer.hasNext() || !requestQueue.isEmpty() || requestsExecuted.get() < requestsSeen.get()) {
                    try {
                        // get the request
                        INPUT request = requestQueue.poll(100, TimeUnit.MILLISECONDS);
                        if (request != null) {

                            // execute the request
                            long startTime = System.currentTimeMillis();
                            OUTPUT result = executor.execute(request);
                            long endTime = System.currentTimeMillis();

                            execStart.updateAndGet(value -> Math.min(value, startTime));
                            execEnd.updateAndGet(value -> Math.max(value, endTime));

                            // consume the results
                            // make sure only one thread can call the consumer at a time
                            synchronized (consumer) {
                                consumer.consume(request, result, endTime - startTime);
                            }
                            requestsExecuted.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }

        // start reading from the producer and adding to the request queue
        try {
            while (producer.hasNext()) {
                // add requests to the queue, waiting if it's full
                requestQueue.put(producer.next());
                requestsSeen.incrementAndGet();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // shutdown the service
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        return new ExecutionStatistics(
                requestsExecuted.get(),
                Duration.ofMillis(execEnd.get() - execStart.get())
        );
    }

    @Override
    public void close() throws IOException {
        IOUtils.close(producer, executor, consumer);
    }
}
