// Xuan Thinh Le - a1807507
// A counter for server, content server, and content server 
// to maintain logical order of the requests (PUT and GET)

public class LamportClock {
    private long val = 0;

    // constructor
    public LamportClock() {
    }

    public LamportClock(Long val) {
        this.val = val;
    }

    // Increase the value of the clock
    public synchronized void increment() {
        this.val++;
    }

    // Set the value to max and increment it
    public synchronized void maxIncrease(Long v) {
        this.val = Long.max(v, this.val);
        this.val++;
    }
}