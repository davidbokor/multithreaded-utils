package com.bokor.multithreaded;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiThreadedRunnerTest {

    @Test
    public void test() {
        int count = 1000;
        int threads = 10;

        Producer<Integer> producer = IterableProducer.from(IntStream.rangeClosed(1, count).boxed());
        AccumulatingConsumer<Integer, Double> consumer = new AccumulatingConsumer<>();
        Executor<Integer, Double> executor = new Executor<>() {
            @Override
            public Double execute(Integer request) {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextLong(30));
                } catch (InterruptedException ignore) {
                }
                return request.doubleValue() / 100.0;
            }

            @Override
            public void close() {
                // DO NOTHING
            }
        };

        MultiThreadedRunner<Integer, Double> runner = new MultiThreadedRunner<>(producer, executor, consumer, threads);
        ExecutionStatistics stats = runner.run();

        // get the results
        Set<Double> results = consumer.getResults().stream().map(AccumulatingConsumer.Pair::getOutput).collect(Collectors.toSet());
        assertEquals(count, results.size());
        assertEquals(count, stats.getCount());

        IntStream.rangeClosed(1, count).boxed().map(t -> t.doubleValue() / 100.0).forEach(t -> assertTrue(results.contains(t)));
    }
}