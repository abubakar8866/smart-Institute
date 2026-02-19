package util;

import java.util.concurrent.atomic.AtomicInteger;

public final class IdGenerator {

    private static final AtomicInteger counter = new AtomicInteger(1000);

    private IdGenerator() { }

    public static Integer generateId() {
        return counter.incrementAndGet();
    }
    
    public static void initialize(Integer lastId) {
        counter.set(lastId);
    }
    
}
