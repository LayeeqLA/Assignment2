package code;

import java.io.IOException;
import java.util.List;

public class RicartAgrawala extends RoucairolCarvalho {

    // REUSE ALL FUNCTIONALITY from RC
    // just need to reset keys for requesting REPLY afresh

    public RicartAgrawala(List<Node> nodes, Node currentNode) {
        super(nodes, currentNode);
        for (int neighborId : currentNode.getNeighborIds()) {
            // No process holds keys
            this.keys.put(neighborId, false);
        }
    }

    @Override
    public synchronized void csLeave() throws ClassNotFoundException, IOException {
        super.csLeave(); // reuse from RC
        // reset KEYS for fresh REPLY from all neighbors
        for (int neighborId : keys.keySet()) {
            keys.put(neighborId, false);
        }
    }

}