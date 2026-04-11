import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AsyncArrayUtils {
    public static <T, R> CompletableFuture<List<R>> asyncMapFuture(
            List<T> input,
            FutureMapper<T, R> mapper,
            AbortController.AbortSignal signal
    ) {
        CompletableFuture<List<R>> finalFuture = new CompletableFuture<>();

        if (signal != null && signal.aborted()) {
            finalFuture.completeExceptionally(new AbortError());
            return finalFuture;
        }

        List<CompletableFuture<R>> futures = new ArrayList<>();
        Runnable onAbort = () -> {
            finalFuture.completeExceptionally(new AbortError());

            for (CompletableFuture<R> future : futures) {
                future.cancel(true);
            }
        };

        if (signal != null) {
            signal.addAbortListener(onAbort);
        }

        for (T item : input) {
            if (signal != null && signal.aborted()) {
                onAbort.run();
                break;
            }

            futures.add(mapper.map(item));
        }

        CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .whenComplete((ignored, err) -> {
                    if (signal != null) {
                        signal.removeAbortListener(onAbort);
                    }

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
