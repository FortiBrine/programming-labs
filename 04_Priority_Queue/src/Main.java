import java.util.*;

public class Main {
    public static void main(String[] args) {
        BiDirectionalPriorityQueue<String> q = new BiDirectionalPriorityQueue<>();

        q.enqueue("A", 3);
        q.enqueue("B", 1);
        q.enqueue("C", 5);
        q.enqueue("D", 3);
        q.enqueue("E", 5);

        System.out.println("peekHighest = " + q.peekHighest());
        System.out.println("peekLowest  = " + q.peekLowest());
        System.out.println("peekOldest  = " + q.peekOldest());
        System.out.println("peekNewest  = " + q.peekNewest());
    }
}