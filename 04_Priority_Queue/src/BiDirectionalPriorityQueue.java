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

    public T dequeue(Mode mode) {
        if (isEmpty()) return null;

        Node<T> node = switch (mode) {
            case HIGHEST -> removeHighestNode();
            case LOWEST -> removeLowestNode();
            case OLDEST -> head;
            case NEWEST -> tail;
        };

        if (mode == Mode.OLDEST || mode == Mode.NEWEST) {
            removeFromPriorityBucket(node);
            unlinkFromLinkedList(node);
            size--;
        }

        return node.item;
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

    public T dequeueHighest() {
        return dequeue(Mode.HIGHEST);
    }

    public T dequeueLowest() {
        return dequeue(Mode.LOWEST);
    }

    public T dequeueOldest() {
        return dequeue(Mode.OLDEST);
    }

    public T dequeueNewest() {
        return dequeue(Mode.NEWEST);
    }

    private Node<T> getHighestNode() {
        Map.Entry<Integer, Deque<Node<T>>> entry = priorityMap.lastEntry();
        return entry.getValue().peekFirst();
    }

    private Node<T> getLowestNode() {
        Map.Entry<Integer, Deque<Node<T>>> entry = priorityMap.firstEntry();
        return entry.getValue().peekFirst();
    }

    private Node<T> removeHighestNode() {
        Map.Entry<Integer, Deque<Node<T>>> entry = priorityMap.lastEntry();
        Deque<Node<T>> bucket = entry.getValue();

        Node<T> node = bucket.removeFirst();
        if (bucket.isEmpty()) {
            priorityMap.remove(entry.getKey());
        }

        unlinkFromLinkedList(node);
        size--;
        return node;
    }

    private Node<T> removeLowestNode() {
        Map.Entry<Integer, Deque<Node<T>>> entry = priorityMap.firstEntry();
        Deque<Node<T>> bucket = entry.getValue();

        Node<T> node = bucket.removeFirst();
        if (bucket.isEmpty()) {
            priorityMap.remove(entry.getKey());
        }

        unlinkFromLinkedList(node);
        size--;
        return node;
    }

    private void removeFromPriorityBucket(Node<T> node) {
        Deque<Node<T>> bucket = priorityMap.get(node.priority);
        if (bucket == null) {
            return;
        }

        bucket.removeFirstOccurrence(node);

        if (bucket.isEmpty()) {
            priorityMap.remove(node.priority);
        }
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

    private void unlinkFromLinkedList(Node<T> node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }

        node.prev = null;
        node.next = null;
    }
}