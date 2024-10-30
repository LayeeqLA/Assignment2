package code;

public class CritSecInfo {
    private int nodeId;
    private long start;
    private long end;

    public CritSecInfo(int nodeId, long start) {
        this.nodeId = nodeId;
        this.start = start;
    }

    public long getStart() {
        return start;
    }

    // public void setStart(long start) {
    //     this.start = start;
    // }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public void print() {
        System.out.println("CRITICAL SECTION: [NodeID: " + nodeId + "] Start: " + start + " End: " + end);
    }
}
