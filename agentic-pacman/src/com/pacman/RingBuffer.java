package com.pacman;

import java.util.Arrays;

/**
 * Fixed-capacity circular queue ("ring buffer").
 *
 * <p>Phase 3 of the design: the buffer acts as Pac-Man's short-term memory /
 * context window.  Writing is O(1): the write cursor {@code head} simply walks
 * forward and, once the array is full, overwrites the oldest entry instead of
 * shifting everything (which is what a plain Python {@code list.pop(0)} +
 * {@code append} would do).
 *
 * <p>Logical order is always "oldest first": {@code get(0)} is the oldest
 * surviving element, {@code get(size()-1)} the newest.
 */
public final class RingBuffer<T> {

    /** Backing store.  Object[] because Java cannot allocate {@code new T[]}. */
    private final Object[] data;
    private final int capacity;

    /** Index where the NEXT pushed element will be written. */
    private int head;
    /** How many live elements are currently stored (<= capacity). */
    private int size;

    public RingBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("RingBuffer capacity must be > 0");
        }
        this.capacity = capacity;
        this.data = new Object[capacity];
    }

    /** Append one element in O(1); silently overwrites the oldest if full. */
    public void push(T value) {
        data[head] = value;
        head = (head + 1) % capacity;   // wrap around -> "ring"
        if (size < capacity) {
            size++;
        }
    }

    /** Read element {@code index} counted from the OLDEST (0) to the NEWEST. */
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + " outside 0.." + (size - 1));
        }
        int start = (head - size + capacity) % capacity;   // physical slot of oldest
        return (T) data[(start + index) % capacity];
    }

    public T newest() {
        return get(size - 1);
    }

    public T oldest() {
        return get(0);
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return capacity;
    }

    public boolean isFull() {
        return size == capacity;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        Arrays.fill(data, null);
        head = 0;
        size = 0;
    }

    /** How many stored elements equal {@code value} (uses .equals, like Python ==). */
    public int count(T value) {
        int n = 0;
        for (int i = 0; i < size; i++) {
            if (same(get(i), value)) {
                n++;
            }
        }
        return n;
    }

    /**
     * The most frequent element.  Ties are broken in favour of the MOST RECENT
     * value, because the scan walks from newest to oldest and only replaces the
     * champion on a strictly larger count.
     *
     * @return the dominant element, or {@code null} when the buffer is empty
     */
    public T dominant() {
        if (size == 0) {
            return null;
        }
        T best = null;
        int bestCount = 0;
        for (int i = size - 1; i >= 0; i--) {
            T candidate = get(i);
            int c = 0;
            for (int j = 0; j < size; j++) {
                if (same(get(j), candidate)) {
                    c++;
                }
            }
            if (c > bestCount) {
                bestCount = c;
                best = candidate;
            }
        }
        return best;
    }

    /** Human readable snapshot, e.g. {@code "RIGHT > UP > UP > LEFT"}. */
    public String join(String separator) {
        if (size == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(get(i));
        }
        return sb.toString();
    }

    private static boolean same(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
