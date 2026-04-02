import java.util.*;

public class BiDirectionalPriorityQueue<T> {
    public enum Mode {
        HIGHEST,
        LOWEST,
    }

    private final TreeMap<Integer, Deque<T>> priorityMap;
    private int size;

    public BiDirectionalPriorityQueue() {
        this.priorityMap = new TreeMap<>();
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void enqueue(T item, int priority) {
        priorityMap
                .computeIfAbsent(priority, _ -> new ArrayDeque<>())
                .addLast(item);

        size++;
    }

    public T peek(Mode mode) {
        if (isEmpty()) return null;

        return switch (mode) {
            case HIGHEST -> getHighest();
            case LOWEST -> getLowest();
        };
    }

    public T peekHighest() {
        return peek(Mode.HIGHEST);
    }

    public T peekLowest() {
        return peek(Mode.LOWEST);
    }

    private T getHighest() {
        Map.Entry<Integer, Deque<T>> entry = priorityMap.lastEntry();
        return entry.getValue().peekFirst();
    }

    private T getLowest() {
        Map.Entry<Integer, Deque<T>> entry = priorityMap.firstEntry();
        return entry.getValue().peekFirst();
    }
}