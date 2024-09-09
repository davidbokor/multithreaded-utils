package com.bokor.multithreaded;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * An implementation of the {@link com.bokor.multithreaded.Producer} interface that adapts an {@link java.lang.Iterable}.
 */
public class IterableProducer<INPUT> implements Producer<INPUT> {

    private final Iterator<INPUT> iterator;

    private IterableProducer(Iterator<INPUT> iterator) {
        this.iterator = iterator;
    }

    public static <INPUT> IterableProducer<INPUT> from(Iterable<INPUT> iterable) {
        return new IterableProducer<>(iterable.iterator());
    }

    public static <INPUT> IterableProducer<INPUT> from(Stream<INPUT> stream) {
        return new IterableProducer<>(stream.iterator());
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
