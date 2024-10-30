package code;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;

public class SocketService implements Runnable {

    private Node thisNode;
    private CountDownLatch latch;
    private MutexService mutexService;
    private List<Thread> neighborThreads;

    public SocketService(Node thisNode, MutexService mutexService, CountDownLatch latch) {
        this.thisNode = thisNode;
        this.latch = latch;
        this.mutexService = mutexService;
    }

    @Override
    public void run() {
        neighborThreads = new ArrayList<>();

        // SETUP SERVER
        InetSocketAddress addr = new InetSocketAddress(thisNode.getPort());
        SctpServerChannel ssc;
        try {
            ssc = SctpServerChannel.open();
            ssc.bind(addr);
            System.out.println("Started SERVER on nodeId: " + thisNode.getId() + " on port: " + thisNode.getPort());

            for (int i = 0; i < thisNode.getNeighborCount(); i++) {
                SctpChannel clientConnection = ssc.accept();
                Thread clientThread = new Thread(new ClientHandler(clientConnection, mutexService, latch));
                clientThread.start();
                neighborThreads.add(clientThread);
            }

            // inform sender thread all receivers are connected
            latch.countDown();

            // wait for send channel
            latch.await();

            for (Thread clientThread : neighborThreads) {
                clientThread.join();
            }

            ssc.close();

        } catch (IOException | InterruptedException e) {
            System.err.println("xxxxx---SOCKET SERVICE ERROR---xxxxx");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {

        private final SctpChannel channel;
        private final MutexService mutexService;
        private final CountDownLatch latch;

        public ClientHandler(SctpChannel channel, MutexService mutexService, CountDownLatch latch) {
            this.channel = channel;
            this.mutexService = mutexService;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                int pid = -1;
                latch.await(); // wait for send connections to be ready

                boolean receiving = true;
                while (receiving) {
                    // keep listening and receive incoming messages
                    ByteBuffer buf = ByteBuffer.allocateDirect(Constants.MAX_MSG_SIZE);
                    channel.receive(buf, null, null);

                    Message message = Message.fromByteBuffer(buf);
                    if (message == null) {
                        // neighbor is closing this connection
                        break;
                    }
                    if (pid == -1) {
                        // first message from neighbor
                        pid = message.getSender();
                        Thread.currentThread().setName("RECEIVER-" + pid);
                    } else {
                        assert pid == message.getSender();
                        // SHOULD RECEIVE SAME SENDER PID ON A SINGLE THREAD
                    }
                    synchronized (mutexService) {
                        System.out.println("Received message");
                        message.print();
                        switch (message.getMsgType()) {
                            case REQUEST:
                                mutexService.processIncomingRequest(message);
                                break;
                            case REPLY:
                                mutexService.processIncomingReply(message);
                                break;
                            default:
                                System.out.println(message.getMsgType() + " unexpected!");
                                receiving = false;
                                break;
                        }
                        mutexService.printCurrentClock();
                    }
                }
                System.out.println("FINISHED RECEIVING MESSAGES FOR: " + Thread.currentThread().getName());
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                System.err.println("xxxxx---CLIENT HANDLER ERROR--> " + Thread.currentThread().getName());
                System.err.println(e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
