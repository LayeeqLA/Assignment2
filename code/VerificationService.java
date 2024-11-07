package code;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VerificationService {

    public static void verifyCSEntries(int nodeCount, String protocol, String configPath) throws IOException {
        /*
         * ASSUMES NODE IDs ARE IN ORDER: 0 ... n-1
         */

        String baseOutputPath = configPath;
        if (baseOutputPath.endsWith(".txt")) {
            baseOutputPath = baseOutputPath.substring(0, baseOutputPath.length() - 4);
        }

        List<String> paths = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) {
            paths.add(baseOutputPath + "-" + i + ".out");
        }

        List<CritSecInfo> critSections = new ArrayList<>();
        for (String path : paths) {
            List<String> allLines = Files.readAllLines(Paths.get(path));
            // FIX BOM encoding for UTF-16 and UTF-8 config files
            String firstLine = allLines.get(0);
            if (firstLine.codePointAt(0) == 0xfeff) {
                allLines.set(0, firstLine.substring(1, firstLine.length()));
            }
            if ("".equalsIgnoreCase(allLines.get(allLines.size() - 1).trim())) {
                // remove last line if blank
                allLines.remove(allLines.size() - 1);
            }
            for (String csEntryLine : allLines) {
                critSections.add(CritSecInfo.fromFileString(csEntryLine));
            }
        }

        Collections.sort(critSections, Comparator.comparingLong(csInfo -> csInfo.getStartClock()));

        for (int i = 0; i < critSections.size() - 1; i++) {
            if (critSections.get(i).getEndClock() >= critSections.get(i + 1).getStartClock()) {
                System.out.println("---CRITICAL SECTION VIOLATION FOUND---");
                critSections.get(i).print();
                critSections.get(i + 1).print();
                return;
            }
        }

        System.out.println("---MUTEX VERIFIED: NO CS VIOLATION---\n");

        calculateAndStoreResults(baseOutputPath, nodeCount, protocol, critSections);
    }

    private static void calculateAndStoreResults(String baseOutputPath, int nodeCount, String protocol,
            List<CritSecInfo> critSections) {
        double meanResponseTime = 0;
        double msgCount = 0;
        double csExecuted = critSections.size();
        long systemStartTime = Long.MAX_VALUE;
        long systemEndTime = Long.MIN_VALUE;

        for (CritSecInfo csInfo : critSections) {
            meanResponseTime += (csInfo.getEndTime() - csInfo.getRequestTime());
            msgCount += csInfo.getMessageCount();
            systemStartTime = Long.min(systemStartTime, csInfo.getStartTime());
            systemEndTime = Long.max(systemEndTime, csInfo.getEndTime());
        }
        System.out.println("MESSAGE COMPLEXITY: " + msgCount / csExecuted);
        System.out.println("AVG RESPONSE TIME: " + meanResponseTime / csExecuted);
        System.out.println("THROUGHPUT: " + csExecuted * 1000 / (double) (systemEndTime - systemStartTime));

        try {
            FileWriter writer = new FileWriter(
                    baseOutputPath + "-" + protocol + "-" + System.currentTimeMillis() + ".txt");
            writer.write("PROTOCOL: " + protocol + System.lineSeparator());
            writer.write("CS COUNT: " + csExecuted + System.lineSeparator());
            writer.write("MESSAGE COMPLEXITY: " + (msgCount / csExecuted) + System.lineSeparator());
            writer.write("AVG RESPONSE TIME: " + (meanResponseTime / csExecuted) + System.lineSeparator());
            writer.write("THROUGHPUT: " + csExecuted * 1000 / (double) (systemEndTime - systemStartTime)
                    + System.lineSeparator());
            writer.close();
        } catch (IOException e) {
            System.out.println("Failed to write results to file");
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Need exactly TWO args!");
            System.exit(-1);
        }
        String configPath = args[0];
        String protocol = args[1];
        int nodeCount = Integer.parseInt(args[2]);
        MutexService.validateMutexProtocolString(protocol);
        verifyCSEntries(nodeCount, protocol, configPath);

    }

}
