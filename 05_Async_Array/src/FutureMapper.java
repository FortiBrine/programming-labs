import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface FutureMapper<T, R> {
    CompletableFuture<R> map(T item);
}