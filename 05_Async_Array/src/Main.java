import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Pattern emailPattern = Pattern.compile(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        );

        List<String> emails = List.of(
                "test@fortibrine.me",
                "invalid-email",
                "email@gmail.com"
        );

        FutureMapper<String, Boolean> emailValidator = email ->
                CompletableFuture.supplyAsync(() -> {
                    return emailPattern.matcher(email).matches();
                });

        Thread.sleep(700);

        AbortController futureController = new AbortController();
        futureController.abort();

        CompletableFuture<List<Boolean>> futureResult = AsyncArrayUtils.asyncMapFuture(
                emails,
                emailValidator,
                futureController.signal()
        );

        futureResult
                .thenAccept(result -> System.out.println("Future result: " + result))
                .exceptionally(err -> {
                    System.out.println("Future error: " + err.getCause().getMessage());
                    return null;
                });

    }
}
