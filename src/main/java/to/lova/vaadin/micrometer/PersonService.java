package to.lova.vaadin.micrometer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;

@Service
class PersonService {

    private static final List<String> FIRST_NAMES =
            List.of("Ada", "Linus", "Grace", "Alan", "Margaret", "Edsger", "Barbara", "Donald");
    private static final List<String> LAST_NAMES =
            List.of("Lovelace", "Torvalds", "Hopper", "Turing", "Hamilton", "Dijkstra", "Liskov", "Knuth");

    private final List<Person> people = IntStream.range(0, 5000)
            .mapToObj(i -> {
                var first = FIRST_NAMES.get(i % FIRST_NAMES.size());
                var last = LAST_NAMES.get((i / FIRST_NAMES.size()) % LAST_NAMES.size());
                return new Person(i, first, last, (first + "." + last + i + "@example.com").toLowerCase());
            })
            .toList();

    private final AtomicBoolean slow = new AtomicBoolean(false);

    void setSlow(boolean slow) {
        this.slow.set(slow);
    }

    boolean isSlow() {
        return slow.get();
    }

    @Observed(name = "person.fetch", contextualName = "person-fetch")
    Stream<Person> fetch(int offset, int limit) {
        maybeHiccup();
        return people.stream().skip(offset).limit(limit);
    }

    @Observed(name = "person.count", contextualName = "person-count")
    int count() {
        maybeHiccup();
        return people.size();
    }

    @Observed(name = "person.break", contextualName = "person-break")
    void breakSomething() {
        throw new IllegalStateException("simulated outage in person service");
    }

    private void maybeHiccup() {
        if (!slow.get()) {
            return;
        }
        // uniform 300-700 ms — p95 ≈ 680 ms (trips warning), p99 ≈ 696 ms (trips critical)
        var delay = 300 + ThreadLocalRandom.current().nextInt(400);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }
}
