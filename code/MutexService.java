package code;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sun.nio.sctp.MessageInfo;

public abstract class MutexService {

    private static MutexService service;
    protected ScalarClock clock;
    protected Node currentNode;
    protected List<Node> allNodes;
    protected Set<Integer> finishedNodes;
    protected Map<Integer, Boolean> keys;
    protected Set<Integer> deferredReplies;
    protected AtomicBoolean csExecuting; // true if node is currently in CS
    protected AtomicBoolean csRequestPending; // true if application has made CS request but not yet fulfilled
    protected CritSecInfo csInfo;

    protected MutexService(List<Node> nodes, Node currentNode) {
        this.allNodes = nodes;
        this.currentNode = currentNode;
        this.clock = new ScalarClock();
        this.keys = new ConcurrentHashMap<>();
        this.deferredReplies = new HashSet<>();
        this.csExecuting = new AtomicBoolean(false);
        this.csRequestPending = new AtomicBoolean(false);
        this.csInfo = null;
        if (currentNode.getId() == Constants.BASE_NODE) {
            this.finishedNodes = new HashSet<>();
        }
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

    public abstract void processIncomingRequest(Message message) throws IOException, ClassNotFoundException;

    public abstract void processIncomingReply(Message message);

    public void processIncomingFinish(Message message) {
        finishedNodes.add(message.getSender());
    }

    public void processIncomingTerminate(Message message) throws IOException {
        for (Node node : currentNode.getNeighbors()) {
            node.getChannel().close();
        }
    }

    protected synchronized boolean checkIfAllKeysReceived() {
        for (Boolean keyAvailable : keys.values()) {
            if (!keyAvailable) {
                return false; // atleast one key still not received
            }
        }

        // Reach here => all keys are received
        // mark start of CS execution
        long startTime = System.currentTimeMillis();
        csExecuting.set(true);
        csRequestPending.set(false);
        csInfo.setStartInfo(clock.incrementAndGet(), startTime);
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
        msg.print(" Destination: " + destinationNodeId + " ---->SENT");
        Node.getNodeById(allNodes, destinationNodeId).getChannel().send(msg.toByteBuffer(), messageInfo);
    }

    protected synchronized void sendReply(int destinationNodeId) throws IOException, ClassNotFoundException {
        // discard key
        keys.put(destinationNodeId, false);
        // increment clock first for piggyback
        Message reply = new Message(currentNode.getId(), Message.MessageType.REPLY, clock.incrementAndGet());
        sendMessageToNode(reply, destinationNodeId);
    }

    protected synchronized void sendRequest(int destinationNodeId) throws IOException, ClassNotFoundException {
        if (csInfo != null) {
            // for capturing message complexity when a CS Request initiated
            csInfo.incrementMessageCount();
        }
        Message request = new Message(currentNode.getId(), Message.MessageType.REQUEST, csInfo.getRequestClock(),
                clock.incrementAndGet());
        sendMessageToNode(request, destinationNodeId);
    }

    protected synchronized void sendFinish() throws IOException, ClassNotFoundException {
        // increment clock first for piggyback
        Message finish = new Message(currentNode.getId(), Message.MessageType.FINISH, null, clock.incrementAndGet());
        sendMessageToNode(finish, Constants.BASE_NODE);
    }

    protected synchronized void sendTerminate(int destinationNodeId) throws IOException, ClassNotFoundException {
        // increment clock first for piggyback
        Message terminate = new Message(currentNode.getId(), Message.MessageType.TERMINATE, null, clock.incrementAndGet());
        sendMessageToNode(terminate, destinationNodeId);
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

    public void shutdown() throws IOException, ClassNotFoundException {
        if (currentNode.getId() == Constants.BASE_NODE) {
            finishedNodes.add(Constants.BASE_NODE);

            // block until we get FINISH from all nodes
            while (finishedNodes.size() != allNodes.size())
                ;

            // send terminate to all others
            for (int neighborId : currentNode.getNeighborIds()) {
                sendTerminate(neighborId);
            }

        } else {
            // send finish to BASE NODE 0
            sendFinish();
        }
    }
}
