package code;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import com.sun.nio.sctp.SctpChannel;

public class Runner {

    private static int nodeCount = -1; // T1
    private static int meanInterRequestDelay; // T2
    private static int meanCsExecutionTime; // T3
    private static int numberOfRequests; // T4
    private static String outputPath;

    public static void main(String[] args) {
        System.out.println("*****Initializing Service*****");

        if (args.length != 3) {
            System.err.println("Need exactly THREE args!");
            System.exit(-1);
        }

        List<Node> nodes = null;
        Node currentNode = null;
        try {
            String configPath = args[0];
            String protocol = args[1];
            int nodeId = Integer.parseInt(args[2]);
            System.out.println("PID for this host: " + nodeId);

            if (!MutexService.validateMutexProtocolString(protocol)) {
                System.err.println("\nInvalid MUTEX PROTOCOL -- RC or RA expected");
                System.exit(-1);
            }

            nodes = processConfig(configPath, nodeId);
            currentNode = Node.getNodeById(nodes, nodeId);
            System.out.println("\n****CURRENT NODE****");
            currentNode.printConfig();

            MutexService mutexService = MutexService.getService(protocol, nodes, currentNode);

            System.out.println("\n*****Starting connectivity activies*****");
            CountDownLatch latch = new CountDownLatch(2);
            Thread receiverThread = new Thread(new SocketService(currentNode,
                    mutexService, latch), "RECV-SRVC");
            receiverThread.start();

            for (Node node : currentNode.getNeighbors()) {
                int attempts = 0;
                while (node.getChannel() == null && attempts < Constants.CONNECT_MAX_ATTEMPTS) {
                    try {
                        InetSocketAddress addr = new InetSocketAddress(node.getHost(), node.getPort());
                        Thread.sleep(3000); // connection refused fix
                        SctpChannel sc = SctpChannel.open(addr, 0, 0);
                        node.setChannel(sc);
                        System.out.println("Connected successfully to node " + node.getId());
                    } catch (IOException e) {
                        System.err.println("Connect error for node " + node.getId() + " WILL RETRY");
                        System.err.println(e.getMessage() != null ? e.getMessage() : "null");
                        Thread.sleep(Constants.CONNECT_WAIT);
                        attempts++;
                    }
                }

                if (node.getChannel() == null) {
                    System.err.println("Failed to establish connection with node id: " + node.getId());
                    throw new InterruptedException("CONNECTION SETUP FAILED");
                }
            }

            // inform send channels setup
            latch.countDown();
            // wait for all receiver channels to be initialized
            latch.await();
            System.out.println("*****CONNECTIONS READY*****\n");

            Thread.sleep(6000); // TODO: waiting for other nodes to also connect if delayed
            Thread applicationThread = new Thread(new ApplicationService(mutexService,
                    meanInterRequestDelay, meanCsExecutionTime, numberOfRequests), "APPL-SRVC");
            applicationThread.start();

            // Wait for all application requests to finish
            applicationThread.join();
            currentNode.closeFileWriter();
            mutexService.shutdown(); // for processing termination of system
            receiverThread.join();

            System.out.println("\n*****END*****\n\n");

            if (nodeId == Constants.BASE_NODE) {
                System.out.println("\n***STARTING VERIFICATION***\n");
                VerificationService.verifyCSEntries(nodeCount, configPath);
            }

        } catch (NumberFormatException | IOException | InterruptedException | ClassNotFoundException e) {
            System.err.println("xxxxx---Processing error occured---xxxxx");
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            if (currentNode != null) {
                currentNode.closeFileWriter();
            }
            if (nodes != null) {
                for (Node node : nodes) {
                    try {
                        if (node.getChannel() != null) {
                            node.getChannel().close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public static List<Node> processConfig(String configPath, int currentNodeId) throws IOException {
        List<Node> nodes = Collections.synchronizedList(new ArrayList<>());
        List<String> allLines = Files.readAllLines(Paths.get(configPath));
        // int neighborIndex = 0;

        // FIX BOM encoding for UTF-16 and UTF-8 config files
        String firstLine = allLines.get(0);
        if (firstLine.codePointAt(0) == 0xfeff) {
            allLines.set(0, firstLine.substring(1, firstLine.length()));
        }

        for (String line : allLines) {
            // System.out.println(line);

            // remove inline comments
            line = line.split("#")[0].trim();

            if (!Constants.isConfigLineValid(line)) {
                continue;
            }

            if (nodeCount <= 0) {
                // read global params
                System.out.println("Global params: " + line);
                String globalParamStrings[] = line.split(" ");
                nodeCount = Integer.parseInt(globalParamStrings[0]);
                meanInterRequestDelay = Integer.parseInt(globalParamStrings[1]);
                meanCsExecutionTime = Integer.parseInt(globalParamStrings[2]);
                numberOfRequests = Integer.parseInt(globalParamStrings[3]);

            } else {
                // read node entry
                String nodeInfo[] = line.split(" ");
                nodes.add(new Node(Integer.parseInt(nodeInfo[0]), nodeInfo[1], Integer.parseInt(nodeInfo[2])));
            }
        }

        Node currentNode = Node.getNodeById(nodes, currentNodeId);
        currentNode.setNeighborIds(IntStream.rangeClosed(0, nodeCount - 1)
                .filter(id -> id != currentNodeId).toArray());
        currentNode.setNeighbors(nodes);

        // constructConvergeCastTree(nodes, currentNodeId);
        // currentNode.printConvergeCast();

        System.out.println("***PRINTING NODE CONFIG***");
        for (Node node : nodes) {
            node.printConfig();
        }

        // generate outputPath for this node
        outputPath = configPath;
        if (outputPath.endsWith(".txt")) {
            outputPath = outputPath.substring(0, outputPath.length() - 4);
        }
        outputPath = outputPath + "-" + currentNodeId + ".out";
        System.out.println("Output file location: " + outputPath);
        currentNode.initWriter(outputPath);

        return nodes;
    }

}