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
    public void csEnter() throws IOException {
        synchronized (this) {
            // TODO: Capture CS start
            // assert no existing CS on this node
            for (Map.Entry<Integer, Boolean> nodeKV : keys.entrySet()) {
                if (nodeKV.getValue() == false) {
                    clock.incrementAndGet();
                    sendMessageToNode(new Message(currentNode.getId(), Message.MessageType.REQUEST, clock),
                            nodeKV.getKey());
                }
            }
        }

        // Block until all keys are received
        while (!checkIfAllKeysReceived());
    }

    @Override
    public synchronized void csLeave() {
        // TODO Auto-generated method stub
        System.out.println("Unimplemented method 'csLeave'");
    }

    @Override
    public synchronized void processIncomingRequest(Message message) {
        if(executingCS.get()) {
            // currently executing CS; defer the reply to this message's sender
            deferredReplies.add(message.getSender());
            return;
        }
        
        

        // TODO Auto-generated method stub
        System.out.println("Unimplemented method 'processIncomingRequest'");
    }

    @Override
    public synchronized void processIncomingReply(Message message) {
        // TODO Auto-generated method stub
        System.out.println("Unimplemented method 'processIncomingReply'");
    }

}
