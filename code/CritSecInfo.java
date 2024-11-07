package code;

public class CritSecInfo {
    private static String delimiter = "---";
    private int nodeId;
    private long requestClock;
    private long startClock;
    private long endClock;
    private long requestTime;
    private long startTime;
    private long endTime;

    public CritSecInfo(int nodeId, long requestClock, long startClock, long endClock,
            long requestTime, long startTime, long endTime) {
        this.nodeId = nodeId;
        this.requestClock = requestClock;
        this.startClock = startClock;
        this.endClock = endClock;
        this.requestTime = requestTime;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public CritSecInfo(int nodeId, long requestClock, long requestTime) {
        this.nodeId = nodeId;
        this.requestClock = requestClock;
        this.requestTime = requestTime;
    }

    public void setStartInfo(long startClock, long startTime) {
        this.startClock = startClock;
        this.startTime = startTime;
    }

    public void setEndInfo(long endClock, long endTime) {
        this.endClock = endClock;
        this.endTime = endTime;
    }

    public static CritSecInfo fromFileString(String csString) {
        String[] parts = csString.split(delimiter);
        CritSecInfo csInfo = new CritSecInfo(Integer.parseInt(parts[0]), Long.parseLong(parts[1]),
                Long.parseLong(parts[2]), Long.parseLong(parts[3]), Long.parseLong(parts[4]),
                Long.parseLong(parts[5]), Long.parseLong(parts[6]));
        return csInfo;
    }

    public long getRequestClock() {
        return requestClock;
    }

    public long getStartClock() {
        return startClock;
    }

    public long getEndClock() {
        return endClock;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "=====>CRITICAL SECTION: [NodeID: " + nodeId + "] Request: " + requestClock
                + " Start: " + startClock + " End: " + endClock;
    }

    public void print() {
        System.out.println(this.toString());
    }

    public String getFileString() {
        return new StringBuffer().append(nodeId).append(delimiter)
                .append(requestClock).append(delimiter)
                .append(startClock).append(delimiter)
                .append(endClock).append(delimiter)
                .append(requestTime).append(delimiter)
                .append(startTime).append(delimiter)
                .append(endTime).append(System.lineSeparator()).toString();
    }
}
