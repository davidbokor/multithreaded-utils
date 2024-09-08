package com.bokor.multithreaded;

import java.io.Closeable;

/**
 * A marker interface for some process that executes some task on the given input, producing some output.
 */
public interface Executor<INPUT, OUTPUT> extends Closeable {

    /**
     * Execute some task on the given input, producing some output.
     */
    OUTPUT execute(INPUT request);
}