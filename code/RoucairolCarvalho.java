package code;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RoucairolCarvalho extends MutexService {

    public RoucairolCarvalho(List<Node> nodes, Node currentNode) {
        super(nodes, currentNode);
        for (int neighborId : currentNode.getNeighborIds()) {
            // Hold key if i < j for all Pi, Pj
            this.keys.put(neighborId, currentNode.getId() < neighborId);
        }
    }

    @Override
    public void csEnter() throws IOException, ClassNotFoundException {
        synchronized (this) {
            assert !csExecuting.get();  // should not be executing
            csCurrentRequestTime = clock.getCurrent(); // mark clock when the CS request was observed
            csRequestPending.set(true);
            // assert no existing CS on this node
            for (Map.Entry<Integer, Boolean> nodeKV : keys.entrySet()) {
                if (nodeKV.getValue() == false) {
                    sendRequest(nodeKV.getKey());
                }
            }
        }

        // Block until all keys are received
        while (!checkIfAllKeysReceived());
    }

    @Override
    public synchronized void csLeave() throws ClassNotFoundException, IOException {
        assert csExecuting.getAndSet(false);    // should be executing before stoping
        csInfo.setEnd(clock.incrementAndGet());
        csInfo.print();
        // TODO: Flush csInfo to a file writing thread???

        // reset CS details
        csInfo = null;
        csCurrentRequestTime = null;

        sendDeferredReplies();
    }

    @Override
    public synchronized void processIncomingRequest(Message message) throws ClassNotFoundException, IOException {
        int senderId = message.getSender();

        if (csExecuting.get()) {
            // currently executing CS; defer the reply to this message's sender
            deferredReplies.add(senderId);
            return;
        }

        if (!csRequestPending.get()) {
            // no pending CS request at this node; can send REPLY back immediately
            assert csRequestPending == null;
            sendReply(senderId);
            return;
        }

        assert csCurrentRequestTime != null;
        if (message.getClock() < csCurrentRequestTime) {
            // incoming request has lower TS, allow it to execute first
            sendReply(senderId);
            // send request again to this node to get back key
            sendRequest(senderId);
        } else if (message.getClock() == csCurrentRequestTime && senderId < currentNode.getId()) {
            // if same timestamp, tie break using node ID => lower ID has more priority
            sendReply(senderId);
            // send request again to this node to get back key
            sendRequest(senderId);
        } else {
            // this node's request has more priority
            // defer the reply to this message's sender
            deferredReplies.add(senderId);
        }

        // TODO: verify this logic [if we need to increment if we receive a request]
        // TODO: I think we need two clocks for REQUEST message, one is CS clock and one is system sync clock
        clock.mergeMessageClockAndIncrement(message.getClock());
    }

    @Override
    public synchronized void processIncomingReply(Message message) {
        keys.put(message.getSender(), true);
        clock.mergeMessageClockAndIncrement(message.getClock());
    }

}
