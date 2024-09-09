package com.bokor.multithreaded;

import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class MultiThreadedRunner<INPUT, OUTPUT> implements Closeable {

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
        AtomicLong execEnd = new AtomicLong(Long.MIN_VALUE);

        AtomicLong requestsExecuted = new AtomicLong();

        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        for (int n = 0; n < threads; n++) {
            executorService.submit(() -> {

                // keep trying until there's nothing left in the producer
                boolean done = false;
                while (!done) {

                    // get the next input
                    // make sure only one thread is reading from the producer at a time
                    INPUT request = null;
                    synchronized (producer) {
                        if (producer.hasNext()) {
                            request = producer.next();
                        } else {
                            done = true;
                        }
                    }

                    if (request != null) {

                        // execute the request
                        long startTime = System.currentTimeMillis();
                        OUTPUT result = executor.execute(request);
                        long endTime = System.currentTimeMillis();

                        // update our timers
                        execStart.updateAndGet(value -> Math.min(value, startTime));
                        execEnd.updateAndGet(value -> Math.max(value, endTime));

                        // consume the results
                        // make sure only one thread can call the consumer at a time
                        synchronized (consumer) {
                            consumer.consume(request, result, endTime - startTime);
                        }
                        requestsExecuted.incrementAndGet();
                    }
                }
            });
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

        return new ExecutionStatistics(requestsExecuted.get(), Duration.ofMillis(execEnd.get() - execStart.get()));
    }

    @Override
    public void close() throws IOException {
        IOUtils.close(producer, executor, consumer);
    }
}
