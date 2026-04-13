import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncArrayUtils {

    public static <T, R> void asyncMapCallback(
            List<T> input,
            AsyncMapper<T, R> mapper,
            AbortController.AbortSignal signal,
            Callback<List<R>> finalCallback
    ) {
        if (signal != null && signal.aborted()) {
            finalCallback.call(new AbortError(), null);
            return;
        }

        List<R> result = new ArrayList<>(Collections.nCopies(input.size(), null));

        if (input.isEmpty()) {
            finalCallback.call(null, result);
            return;
        }

        AtomicBoolean finished = new AtomicBoolean(false);
        AtomicInteger completed = new AtomicInteger(0);

        Runnable onAbort = () -> {
            if (finished.compareAndSet(false, true)) {
                finalCallback.call(new AbortError(), null);
            }
        };

        if (signal != null) {
            signal.addAbortListener(onAbort);
        }

        for (int i = 0; i < input.size(); i++) {
            final int index = i;

            if (signal != null && signal.aborted()) {
                onAbort.run();
                return;
            }

            try {
                mapper.map(input.get(index), (err, value) -> {
                    if (err != null) {
                        if (!finished.compareAndSet(false, true)) return;

                        if (signal != null) {
                            signal.removeAbortListener(onAbort);
                        }

                        finalCallback.call(err, null);
                        return;
                    }

                    result.set(index, value);

                    if (completed.incrementAndGet() == input.size()) {
                        if (!finished.compareAndSet(false, true)) return;

                        if (signal != null) {
                            signal.removeAbortListener(onAbort);
                        }

                        finalCallback.call(null, result);
                    }
                });
            } catch (Exception err) {
                if (finished.compareAndSet(false, true)) {
                    if (signal != null) {
                        signal.removeAbortListener(onAbort);
                    }

                    finalCallback.call(err, null);
                }

                return;
            }
        }
    }

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
            if (!finalFuture.isDone()) {
                finalFuture.completeExceptionally(new AbortError());
            }

            for (CompletableFuture<R> future : futures) {
                future.cancel(true);
            }
        };

        if (signal != null) {
            signal.addAbortListener(onAbort);
        }

        try {
            for (T item : input) {
                if (signal != null && signal.aborted()) {
                    onAbort.run();
                    break;
                }

                futures.add(mapper.map(item));
            }
        } catch (Exception err) {
            if (signal != null) {
                signal.removeAbortListener(onAbort);
            }

            finalFuture.completeExceptionally(err);
            return finalFuture;
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