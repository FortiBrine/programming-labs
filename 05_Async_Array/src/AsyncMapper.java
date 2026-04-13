@FunctionalInterface
public interface AsyncMapper<T, R> {
    void map(T item, Callback<R> callback);
}