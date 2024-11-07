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

    @Override
    public synchronized void processIncomingRequest(Message message) throws ClassNotFoundException, IOException {
        clock.mergeMessageClockAndIncrement(message.getSystemClock());
        int senderId = message.getSender();

        if (csExecuting.get()) {
            // currently executing CS; defer the reply to this message's sender
            deferredReplies.add(senderId);
            return;
        }

        if (!csRequestPending.get()) {
            // no pending CS request at this node; can send REPLY back immediately
            sendReply(senderId);
            return;
        }

        long csCurrentRequestTime = csInfo.getRequestClock();
        assert csCurrentRequestTime != 0;
        if (message.getProtocolClock() < csCurrentRequestTime) {
            // incoming request has lower TS, allow it to execute first
            sendReply(senderId);
        } else if ((message.getProtocolClock().equals(csCurrentRequestTime)) && (senderId < currentNode.getId())) {
            System.out.println("same csReqTime; this node LOST tie breaker");
            // if same timestamp, tie break using node ID => lower ID has more priority
            sendReply(senderId);
        } else {
            // this node's request has more priority
            // defer the reply to this message's sender
            deferredReplies.add(senderId);
        }
    }

}