package code;

import java.io.IOException;

public class ApplicationService implements Runnable {

    private MutexService mutexService;
    private int meanInterRequestDelay; // T2
    private int meanCsExecutionTime; // T3
    private int numberOfRequests; // T4

    public ApplicationService(MutexService mutexService, int meanInterRequestDelay, int meanCsExecutionTime,
            int numberOfRequests) {
        this.mutexService = mutexService;
        this.meanInterRequestDelay = meanInterRequestDelay;
        this.meanCsExecutionTime = meanCsExecutionTime;
        this.numberOfRequests = numberOfRequests;
    }

    @Override
    public void run() {
        int requests = 0;
        try {
            while (requests < numberOfRequests) {
                requests++;

                mutexService.csEnter();

                executeCS(requests);

                mutexService.csLeave();

                // Wait for duration before next request
                // TODO: exponential distribution
                Thread.sleep(meanInterRequestDelay);

            }
        } catch (InterruptedException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void executeCS(int requestNumber) throws InterruptedException {
        // Simulating CS execution
        // TODO: exponential distribution
        Thread.sleep(meanCsExecutionTime);
        System.out.println("Completed CS Request #" + requestNumber);
    }

}
