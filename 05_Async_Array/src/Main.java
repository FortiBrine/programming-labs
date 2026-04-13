import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Pattern emailPattern = Pattern.compile(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        );

        List<String> emails = List.of(
                "test@fortibrine.me",
                "invalid-email",
                "email@gmail.com"
        );

        AbortController callbackController = new AbortController();

        AsyncMapper<String, Boolean> callbackValidator = (email, cb) -> {
            AtomicReference<Thread> threadRef = new AtomicReference<>();

            Runnable onAbort = () -> {
                Thread t = threadRef.get();
                if (t != null) {
                    t.interrupt();
                }
            };

            callbackController.signal().addAbortListener(onAbort);

            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(300);

                    if (callbackController.signal().aborted()) {
                        cb.call(new AbortError(), null);
                        return;
                    }

                    boolean isValid = emailPattern.matcher(email).matches();
                    cb.call(null, isValid);

                } catch (InterruptedException e) {
                    cb.call(new AbortError(), null);
                } finally {
                    callbackController.signal().removeAbortListener(onAbort);
                }
            });

            threadRef.set(t);
            t.start();
        };

        AsyncArrayUtils.asyncMapCallback(
                emails,
                callbackValidator,
                callbackController.signal(),
                (err, result) -> {
                    if (err != null) {
                        System.out.println("Callback error: " + err.getMessage());
                    } else {
                        System.out.println("Callback result: " + result);
                    }
                }
        );

        Thread.sleep(600);

        AbortController futureController = new AbortController();

        FutureMapper<String, Boolean> futureValidator = email ->
                CompletableFuture.supplyAsync(() -> {
                    if (futureController.signal().aborted()
                            || Thread.currentThread().isInterrupted()) {
                        throw new AbortError();
                    }

                    boolean isValid = emailPattern.matcher(email).matches();

                    if (futureController.signal().aborted()
                            || Thread.currentThread().isInterrupted()) {
                        throw new AbortError();
                    }

                    return isValid;
                });

        CompletableFuture<List<Boolean>> futureResult =
                AsyncArrayUtils.asyncMapFuture(
                        emails,
                        futureValidator,
                        futureController.signal()
                );

        futureResult
                .thenAccept(result ->
                        System.out.println("Future result: " + result)
                )
                .exceptionally(err -> {
                    Throwable cause = err.getCause() != null ? err.getCause() : err;
                    System.out.println("Future error: " + cause.getMessage());
                    return null;
                });

        Thread.sleep(600);

        List<Boolean> awaited = AsyncArrayUtils.asyncMapFuture(
                emails,
                futureValidator,
                null
        ).get();

        System.out.println("Await-style result: " + awaited);

        AbortController abortController = new AbortController();
        abortController.abort();

        AsyncArrayUtils.asyncMapFuture(
                emails,
                futureValidator,
                abortController.signal()
        ).thenAccept(result -> {
            System.out.println("Should not happen: " + result);
        }).exceptionally(err -> {
            Throwable cause = err.getCause() != null ? err.getCause() : err;
            System.out.println("Aborted correctly: " + cause.getMessage());
            return null;
        });

        Thread.sleep(600);
    }
}