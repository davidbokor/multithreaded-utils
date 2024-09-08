package com.bokor.multithreaded;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An implementation of the {@link com.bokor.multithreaded.Consumer} that accumulates the result into a list
 */
public class AccumulatingConsumer<INPUT, OUTPUT> implements Consumer<INPUT, OUTPUT> {

    private final List<Pair<INPUT, OUTPUT>> results = new ArrayList<>();

    @Override
    public void consume(INPUT input, OUTPUT output, long executionTime) {
        results.add(new Pair<>(input, output, executionTime));
    }

    public List<Pair<INPUT, OUTPUT>> getResults() {
        return Collections.unmodifiableList(results);
    }

    @Override
    public void close() {
        // DO NOTHING
    }

    public static class Pair<INPUT, OUTPUT> {
        private final INPUT input;
        private final OUTPUT output;

        private final long executionTime;

        private Pair(INPUT input, OUTPUT output, long executionTime) {
            this.input = input;
            this.output = output;
            this.executionTime = executionTime;
        }

        public INPUT getInput() {
            return input;
        }

        public OUTPUT getOutput() {
            return output;
        }

        public long getExecutionTime() {
            return executionTime;
        }
    }
}
