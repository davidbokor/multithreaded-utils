package com.bokor.multithreaded;

import java.util.Iterator;

/**
 * An implementation of the {@link com.bokor.multithreaded.Producer} interface that adapts an {@link java.lang.Iterable}.
 */
public class IterableProducer<INPUT> implements Producer<INPUT> {

    private final Iterator<INPUT> iterator;

    public IterableProducer(Iterable<INPUT> iterable) {
        this.iterator = iterable.iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public INPUT next() {
        return iterator.next();
    }

    @Override
    public void close() {
        // DO NOTHING
    }
}
