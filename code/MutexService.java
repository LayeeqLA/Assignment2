package code;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sun.nio.sctp.MessageInfo;

public abstract class MutexService {

    private static MutexService service;
    protected ScalarClock clock;
    protected Node currentNode;
    protected List<Node> allNodes;
    protected Map<Integer, Boolean> keys;
    protected List<Integer> deferredReplies;
    protected AtomicBoolean csExecuting; // true if node is currently in CS
    protected AtomicBoolean csRequestPending; // true if application has made CS request but not yet fulfilled
    protected Long csCurrentRequestTime;
    protected CritSecInfo csInfo; // TODO: merge csCurrentRequestTime into csInfo?

    protected MutexService(List<Node> nodes, Node currentNode) {
        this.allNodes = nodes;
        this.currentNode = currentNode;
        this.clock = new ScalarClock();
        this.keys = new ConcurrentHashMap<>();
        this.deferredReplies = new ArrayList<>();
        this.csExecuting = new AtomicBoolean(false);
        this.csRequestPending = new AtomicBoolean(false);
        this.csInfo = null;
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

    public abstract void csEnter() throws IOException, ClassNotFoundException;

    public abstract void csLeave() throws IOException, ClassNotFoundException;

    public abstract void processIncomingRequest(Message message) throws ClassNotFoundException, IOException;

    public abstract void processIncomingReply(Message message);

    protected synchronized boolean checkIfAllKeysReceived() {
        for (Boolean keyAvailable : keys.values()) {
            if (!keyAvailable) {
                return false; // atleast one key still not received
            }
        }

        // Reach here => all keys are received
        // mark start of CS execution
        csExecuting.set(true);
        csInfo = new CritSecInfo(currentNode.getId(), clock.incrementAndGet());
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

    protected synchronized void sendMessageToNode(Message msg, int destinationNodeId)
            throws IOException, ClassNotFoundException {
        MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0);
        Node.getNodeById(allNodes, destinationNodeId).getChannel().send(msg.toByteBuffer(), messageInfo);
    }

    protected synchronized void sendReply(int destinationNodeId) throws IOException, ClassNotFoundException {
        // increment clock first for piggyback
        Message reply = new Message(currentNode.getId(), Message.MessageType.REPLY, clock.incrementAndGet());
        sendMessageToNode(reply, destinationNodeId);
    }

    protected synchronized void sendRequest(int destinationNodeId) throws IOException, ClassNotFoundException {
        clock.incrementAndGet(); // increment first for piggyback
        Message reply = new Message(currentNode.getId(), Message.MessageType.REQUEST, csCurrentRequestTime);
        sendMessageToNode(reply, destinationNodeId);
    }

    protected synchronized void sendDeferredReplies() throws IOException, ClassNotFoundException {
        for (int destinationNodeId : deferredReplies) {
            sendReply(destinationNodeId);
        }
        deferredReplies.clear();
    }

    public synchronized void printCurrentClock() {
        System.out.println("CURRENT CLOCK---> " + clock.getCurrent());
    }

}
