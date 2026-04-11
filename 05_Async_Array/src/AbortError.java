public class AbortError extends RuntimeException {
    public AbortError() {
        super("Operation aborted");
    }
}