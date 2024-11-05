package code;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.sun.nio.sctp.SctpChannel;

public class Node {

    private int id;
    private String host;
    private int port;
    private SctpChannel channel;
    private int[] neighborIds;
    private List<Node> neighbors;
    private BufferedWriter writer;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public SctpChannel getChannel() {
        return channel;
    }

    public void setChannel(SctpChannel channel) {
        this.channel = channel;
    }

    public int[] getNeighborIds() {
        return neighborIds;
    }

    public void setNeighborIds(int[] neighborIds) {
        this.neighborIds = neighborIds;
    }

    public Node(int id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
        // this.parent = null;
        // this.children = new LinkedList<>();
        this.writer = null;
    }

    public void printConfig() {
        System.out.println("ID: " + id + " HOST: " + host + " PORT: " + port
                + " Neighbors: " + Arrays.toString(neighborIds));
    }

    public void setNeighbors(List<Node> nodes) {
        this.neighbors = Arrays.stream(neighborIds).mapToObj(nodes::get).collect(Collectors.toList());
    }

    public static Node getNodeById(List<Node> nodes, int nodeId) {
        return nodes.stream().filter(n -> n.getId() == nodeId).findFirst().orElseGet(null);
    }

    public List<Node> getNeighbors() {
        return neighbors;
    }

    public int getNeighborCount() {
        return neighborIds.length;
    }

    public void initWriter(String filePath) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(filePath));
    }

    public void recordCritSec(CritSecInfo critSecInfo) throws IOException {
        writer.write(critSecInfo.getFileString());
    }

    public void closeFileWriter() {
        if (this.writer != null) {
            try {
                this.writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
