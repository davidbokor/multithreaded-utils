package com.bokor.multithreaded;

import java.io.Closeable;

/**
 * A marker interface for consuming the results of an execution
 */
public interface Consumer<INPUT, OUTPUT> extends Closeable {

    /**
     * Consume the results of an execution
     */
    void consume(INPUT input, OUTPUT output);
}