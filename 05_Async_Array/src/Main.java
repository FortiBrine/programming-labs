import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        Pattern emailPattern = Pattern.compile(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        );

        FutureMapper<String, Boolean> emailValidator = email ->
                CompletableFuture.supplyAsync(() -> {
                    return emailPattern.matcher(email).matches();
                });

        List<String> emails = List.of(
                "test@fortibrine.me",
                "invalid-email",
                "email@gmail.com"
        );

        CompletableFuture<List<Boolean>> resultFuture =
                AsyncArrayUtils.asyncMapFuture(emails, emailValidator);

        List<Boolean> results = resultFuture.join();
        System.out.println(results);

    }
}
