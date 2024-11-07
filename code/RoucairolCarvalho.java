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
            long csCurrentRequestTime = System.currentTimeMillis();
            assert !csExecuting.get(); // should not be executing
            long csCurrentRequestClock = clock.incrementAndGet(); // mark clock when the CS request was observed
            System.out.println("Requesting CS at clock: " + csCurrentRequestClock);
            csInfo = new CritSecInfo(currentNode.getId(), csCurrentRequestClock, csCurrentRequestTime);
            csRequestPending.set(true);

            // REQUEST for key if missing
            for (Map.Entry<Integer, Boolean> nodeKV : keys.entrySet()) {
                if (nodeKV.getValue() == false) {
                    sendRequest(nodeKV.getKey());
                }
            }
        }

        // Block until all keys are received
        while (!checkIfAllKeysReceived())
            ;
    }

    @Override
    public synchronized void csLeave() throws ClassNotFoundException, IOException {
        csInfo.setEndInfo(clock.incrementAndGet(), System.currentTimeMillis());
        csInfo.print();
        currentNode.recordCritSec(csInfo);

        // reset CS details
        csInfo = null;

        sendDeferredReplies();
        assert csExecuting.getAndSet(false); // should be executing before stoping
    }

    @Override
    public synchronized void processIncomingRequest(Message message) throws ClassNotFoundException, IOException {
        clock.mergeMessageClockAndIncrement(message.getClock());
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
        if (message.getClock() < csCurrentRequestTime) {
            // incoming request has lower TS, allow it to execute first
            sendReply(senderId);
            // send request again to this node to get back key
            sendRequest(senderId);
        } else if ((message.getClock().equals(csCurrentRequestTime)) && (senderId < currentNode.getId())) {
            System.out.println("same csReqTime; this node LOST tie breaker");
            // if same timestamp, tie break using node ID => lower ID has more priority
            sendReply(senderId);
            // send request again to this node to get back key
            sendRequest(senderId);
        } else {
            // this node's request has more priority
            // defer the reply to this message's sender
            deferredReplies.add(senderId);
        }
    }

    @Override
    public synchronized void processIncomingReply(Message message) {
        clock.mergeMessageClockAndIncrement(message.getClock());
        keys.put(message.getSender(), true);
    }

}
