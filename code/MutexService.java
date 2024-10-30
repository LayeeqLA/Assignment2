package code;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class MutexService {

    private static MutexService service;
    protected ScalarClock clock;
    protected Node currentNode;
    protected List<Node> allNodes;
    protected Map<Integer, Boolean> keys;
    protected List<Integer> deferredReplies;
    protected AtomicBoolean executingCS;

    protected MutexService(List<Node> nodes, Node currentNode) {
        this.allNodes = nodes;
        this.currentNode = currentNode;
        this.clock = new ScalarClock();
        this.keys = new ConcurrentHashMap<>();
        this.deferredReplies = new ArrayList<>();
        this.executingCS = new AtomicBoolean(false);
    }

    public static boolean validateMutexProtocolString(String protocolString) {
        assert protocolString != null;
        try {
            return MutexProtocol.valueOf(protocolString.toUpperCase()) != null;
        } catch (IllegalArgumentException e) {
            // invalid protocol provided
            return false;
        }
    }

    public static MutexService getService(String protocolString, List<Node> nodes, Node currentNode) {
        if (MutexProtocol.valueOf(protocolString.toUpperCase()) == MutexProtocol.RC) {
            service = new RoucairolCarvalho(nodes, currentNode);
        } else if (MutexProtocol.valueOf(protocolString.toUpperCase()) == MutexProtocol.RA) {
            service = new RicartAgrawala(nodes, currentNode);
        }
        return service;
    }

    private enum MutexProtocol {
        RC, // Roucairol and Carvalho’s
        RA, // Ricart and Agrawala’s
        ;
    }

    public abstract void csEnter() throws IOException;

    public abstract void csLeave() throws IOException;

    public abstract void processIncomingRequest(Message message);

    public abstract void processIncomingReply(Message message);

    protected synchronized boolean checkIfAllKeysReceived() {
        for (Boolean keyAvailable : keys.values()) {
            if (!keyAvailable) {
                return false;   // atleast one key still not received
            }
        }

        // Reach here => all keys are received
        // mark start of CS execution
        executingCS.set(true);
        return true;
    }

    public static void main(String[] args) {
        System.out.println("TEST RA: " + validateMutexProtocolString("RA"));
        System.out.println("TEST RC: " + validateMutexProtocolString("RC"));
        System.out.println("TEST ra: " + validateMutexProtocolString("ra"));
        System.out.println("TEST rc: " + validateMutexProtocolString("rc"));
        System.out.println("TEST rca: " + validateMutexProtocolString("rca"));
        System.out.println("TEST RCA: " + validateMutexProtocolString("RCA"));
    }

    protected void sendMessageToNode(Message msg, int nodeId) throws IOException {
        Node.getNodeById(allNodes, nodeId).getChannel().send(null, null);
    }

}
