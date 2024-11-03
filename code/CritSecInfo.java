package code;

public class CritSecInfo {
    private static String delimiter = "---";
    private int nodeId;
    private long start;
    private long end;

    public CritSecInfo(int nodeId, long start) {
        this.nodeId = nodeId;
        this.start = start;
    }

    public static CritSecInfo fromFileString(String csString) {
        String[] parts = csString.split(delimiter);
        CritSecInfo csInfo = new CritSecInfo(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        csInfo.setEnd(Integer.parseInt(parts[2]));
        return csInfo;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "=====>CRITICAL SECTION: [NodeID: " + nodeId + "] Start: " + start + " End: " + end;
    }

    public void print() {
        System.out.println(this.toString());
    }

    public String getFileString() {
        return new StringBuffer().append(nodeId).append(delimiter).append(start)
                .append(delimiter).append(end).append(System.lineSeparator()).toString();
    }
}
