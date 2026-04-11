import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AsyncArrayUtils {
    public static <T, R> CompletableFuture<List<R>> asyncMapFuture(
            List<T> input,
            FutureMapper<T, R> mapper
    ) {
        CompletableFuture<List<R>> finalFuture = new CompletableFuture<>();

        List<CompletableFuture<R>> futures = new ArrayList<>();
        for (T item : input) {
            futures.add(mapper.map(item));
        }

        CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .whenComplete((ignored, err) -> {
                    if (finalFuture.isDone()) return;

                    if (err != null) {
                        finalFuture.completeExceptionally(err);
                        return;
                    }

                    List<R> result = futures.stream()
                            .map(CompletableFuture::join)
                            .toList();

                    finalFuture.complete(result);
                });

        return finalFuture;
    }

}
