import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AbortController {
    private final AbortSignal signal = new AbortSignal();

    public AbortSignal signal() {
        return signal;
    }

    public void abort() {
        signal.abort();
    }

    public static class AbortSignal {
        private volatile boolean aborted = false;
        private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

        public boolean aborted() {
            return aborted;
        }

        public void addAbortListener(Runnable listener) {
            if (aborted) {
                listener.run();
                return;
            }
            listeners.add(listener);
        }

        public void removeAbortListener(Runnable listener) {
            listeners.remove(listener);
        }

        void abort() {
            if (aborted) return;

            aborted = true;

            for (Runnable listener : listeners) {
                listener.run();
            }

            listeners.clear();
        }
    }
}