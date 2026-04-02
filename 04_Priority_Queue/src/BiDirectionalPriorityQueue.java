import java.util.*;

public class BiDirectionalPriorityQueue<T> {
    private static class Node<T> {
        T item;
        int priority;

        Node<T> prev;
        Node<T> next;

        Node(T item, int priority) {
            this.item = item;
            this.priority = priority;
        }
    }

    public enum Mode {
        HIGHEST,
        LOWEST,
        OLDEST,
        NEWEST
    }

    private final TreeMap<Integer, Deque<Node<T>>> priorityMap;
    private Node<T> head;
    private Node<T> tail;
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
        Node<T> node = new Node<>(item, priority);
        appendToLinkedList(node);

        priorityMap
                .computeIfAbsent(priority, _ -> new ArrayDeque<>())
                .addLast(node);

        size++;
    }

    public T peek(Mode mode) {
        if (isEmpty()) return null;

        return switch (mode) {
            case HIGHEST -> getHighestNode().item;
            case LOWEST -> getLowestNode().item;
            case OLDEST -> head.item;
            case NEWEST -> tail.item;
        };
    }

    public T peekHighest() {
        return peek(Mode.HIGHEST);
    }

    public T peekLowest() {
        return peek(Mode.LOWEST);
    }

    public T peekOldest() {
        return peek(Mode.OLDEST);
    }

    public T peekNewest() {
        return peek(Mode.NEWEST);
    }

    private Node<T> getHighestNode() {
        Map.Entry<Integer, Deque<Node<T>>> entry = priorityMap.lastEntry();
        return entry.getValue().peekFirst();
    }

    private Node<T> getLowestNode() {
        Map.Entry<Integer, Deque<Node<T>>> entry = priorityMap.firstEntry();
        return entry.getValue().peekFirst();
    }

    private void appendToLinkedList(Node<T> node) {
        if (tail == null) {
            head = tail = node;
            return;
        }

        tail.next = node;
        node.prev = tail;
        tail = node;
    }
}