package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.jobs;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.core.runtime.IProgressMonitor;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class SerializeResultsToCSVJob extends AbstractBlackboardInteractingJob<AnalysisResultBlackboard> {

    protected final File resultFile;

    public SerializeResultsToCSVJob(File resultFile) {
        this.resultFile = resultFile;
    }

    @Override
    public void execute(IProgressMonitor arg0) throws JobFailedException, UserCanceledException {
        var csvFormat = CSVFormat.DEFAULT.withHeader("directory", "foundViolation", "rawViolation");
        try (var ps = new PrintStream(resultFile)) {
            try (var csvPrinter = new CSVPrinter(ps, csvFormat)) {
                for (var directory : getBlackboard().getPartitionIds()) {
                    var result = getBlackboard().getPartition(directory);
                    csvPrinter.print(result.getDirectory()
                        .getAbsolutePath());
                    csvPrinter.print(result.hasFoundViolation());
                    csvPrinter.print(serializeFoundViolations(result.getFoundViolations()));
                    csvPrinter.println();
                }
            }
        } catch (IOException e) {
            throw new JobFailedException("Could not serialize results to CSV.", e);
        }
    }

    protected String serializeFoundViolations(List<Map<String, Object>> violations) {
        var serializedViolations = new ArrayList<String>(violations.size());
        for (var violation : violations) {
            var sortedKeys = new ArrayList<>(violation.keySet());
            sortedKeys.sort((s1, s2) -> s1.compareTo(s2));
            var variableDefinitions = new ArrayList<String>(sortedKeys.size());
            for (var variableName : sortedKeys) {
                if ("S".equals(variableName)) {
                    // avoid spamming the CSV file
                    continue;
                }
                var value = violation.get(variableName);
                var serializedValue = value.toString();
                if (value instanceof String) {
                    serializedValue = String.format("'%s'", value);
                }
                variableDefinitions.add(String.format("%s = %s", variableName, serializedValue));
            }
            serializedViolations.add(variableDefinitions.stream()
                .collect(Collectors.joining(", ")));
        }
        return serializedViolations.stream()
            .collect(Collectors.joining(";" + System.lineSeparator()));
    }

    @Override
    public void cleanup(IProgressMonitor arg0) throws CleanupFailedException {
        // nothing to do here
    }

    @Override
    public String getName() {
        return "Serialize results to CSV file";
    }

}
