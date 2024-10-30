package code;

import java.util.concurrent.atomic.AtomicLong;

public class ScalarClock {

    private AtomicLong clock;

    public ScalarClock() {
        clock = new AtomicLong();
    }

    public ScalarClock(long messageClock) {
        clock = new AtomicLong(messageClock);
    }

    public long incrementAndGet() {
        return clock.incrementAndGet();
    }

    public long getCurrent() {
        return clock.get();
    }

    public long mergeMessageClockAndIncrement(ScalarClock msgClock) {
        if (msgClock.getCurrent() > this.clock.get()) {
            // if incoming message has higher value -> update this node's clock
            this.clock.set(msgClock.getCurrent());
        }
        return this.clock.incrementAndGet();
    }

}
