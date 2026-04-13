@FunctionalInterface
public interface Callback<T> {
    void call(Exception err, T value);
}