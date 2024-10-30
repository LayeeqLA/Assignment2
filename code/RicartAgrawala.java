package code;

import java.util.List;

public class RicartAgrawala extends MutexService {

    public RicartAgrawala(List<Node> nodes, Node currentNode) {
        super(nodes, currentNode);
        for (int neighborId : currentNode.getNeighborIds()) {
            // No process holds keys
            this.keys.put(neighborId, false);
        }
    }

    @Override
    public void csEnter() {
        // TODO Auto-generated method stub
        System.out.println("Unimplemented method 'csEnter'");
    }

    @Override
    public void csLeave() {
        // TODO Auto-generated method stub
        System.out.println("Unimplemented method 'csLeave'");
    }

    @Override
    public synchronized void processIncomingRequest(Message message) {
        // TODO Auto-generated method stub
        System.out.println("Unimplemented method 'processIncomingRequest'");
    }

    @Override
    public synchronized void processIncomingReply(Message message) {
        // TODO Auto-generated method stub
        System.out.println("Unimplemented method 'processIncomingReply'");
    }

}