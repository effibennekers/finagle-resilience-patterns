package hello;


import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BaseTest {
    private static final int NUMBER_OF_THREADS = 200;
    private static final int NUMBER_OF_STEPS = 10;


    public void runTask(final Supplier<TaskResponse> executor) {
        final List<Task> tasks = new LinkedList<>();
        final ConcurrentMap<Integer, AtomicInteger> statusConterMap = new ConcurrentHashMap<>();
        final Consumer<Integer> statusReporter = status -> {
            final AtomicInteger value = statusConterMap.putIfAbsent(status, new AtomicInteger(0));
            value.incrementAndGet();
        };
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            tasks.add(new Task(i, NUMBER_OF_STEPS, executor, statusReporter));
        }

        tasks.forEach(Thread::start);
        tasks.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        statusConterMap.forEach((status, counter) ->  System.err.println ("Status: " + status + " occured " + counter.intValue() + " times"));
    }
}
