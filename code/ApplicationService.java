package code;

public class ApplicationService implements Runnable {
    
    private SocketService csService;
    private int meanInterRequestDelay; // T2
    private int meanCsExecutionTime; // T3
    private int numberOfRequests; // T4

    public ApplicationService(SocketService csService, int meanInterRequestDelay, int meanCsExecutionTime,
            int numberOfRequests) {
        this.csService = csService;
        this.meanInterRequestDelay = meanInterRequestDelay;
        this.meanCsExecutionTime = meanCsExecutionTime;
        this.numberOfRequests = numberOfRequests;
    }

    @Override
    public void run() {
        int requests = 0;
        try{
        while(requests < numberOfRequests) {
            requests++;

            // csService.enter_cs()

            executeCS();

            // csService.exit_cs()

            // Wait for duration before next request
            // TODO: exponential distribution
            Thread.sleep(meanInterRequestDelay);

        }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void executeCS() throws InterruptedException {
        // Simulating CS execution
        // TODO: exponential distribution
        Thread.sleep(meanCsExecutionTime);
    }
    
}
