package com.bokor.multithreaded;

import java.io.Closeable;
import java.util.Iterator;

/**
 * A marker interfacing for creating input.
 */
public interface Producer<INPUT> extends Iterator<INPUT>, Closeable {
}