package code;

import java.util.concurrent.atomic.AtomicInteger;

public class ScalarClock {

    private AtomicInteger clock;

    public ScalarClock() {
        clock = new AtomicInteger();
    }

    public int incrementAndGet() {
        return clock.incrementAndGet();
    }

    public int getCurrent() {
        return clock.get();
    }

    public int mergeMessageClockAndIncrement(ScalarClock msgClock) {
        if (msgClock.getCurrent() > this.clock.get()) {
            // if incoming message has higher value -> update this node's clock
            this.clock.set(msgClock.getCurrent());
        }
        return this.clock.incrementAndGet();
    }

}
