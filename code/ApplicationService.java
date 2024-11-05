package code;

import java.io.IOException;

import org.apache.commons.math3.distribution.ExponentialDistribution;

public class ApplicationService implements Runnable {

    private MutexService mutexService;
    private ExponentialDistribution interRequestDelay; // T2
    private ExponentialDistribution csExecutionTime; // T3
    private int numberOfRequests; // T4

    public ApplicationService(MutexService mutexService, int meanInterRequestDelay, int meanCSExecutionTime,
            int numberOfRequests) {
        this.mutexService = mutexService;
        this.interRequestDelay = new ExponentialDistribution(meanInterRequestDelay);
        this.csExecutionTime = new ExponentialDistribution(meanCSExecutionTime);
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
                Thread.sleep((long) interRequestDelay.sample());
            }

        } catch (InterruptedException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void executeCS(int requestNumber) throws InterruptedException {
        // Simulating CS execution
        Thread.sleep((long) csExecutionTime.sample());
        System.out.println("Completed CS Request #" + requestNumber);
    }

}
