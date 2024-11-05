package code;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VerificationService {

    public static void verifyCSEntries(int nodeCount, String configPath) throws IOException {
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

        Collections.sort(critSections, Comparator.comparingLong(csInfo -> csInfo.getStart()));

        for (int i = 0; i < critSections.size() - 1; i++) {
            if (critSections.get(i).getEnd() >= critSections.get(i + 1).getStart()) {
                System.out.println("---CRITICAL SECTION VIOLATION FOUND---");
                critSections.get(i).print();
                critSections.get(i + 1).print();
                return;
            }
        }

        System.out.println("---MUTEX VERIFIED: NO CS VIOLATION---\n");
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Need exactly TWO args!");
            System.exit(-1);
        }
        int nodeCount = Integer.parseInt(args[0]);
        String configPath = args[1];

        verifyCSEntries(nodeCount, configPath);

    }

}
