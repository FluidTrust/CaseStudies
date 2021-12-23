package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.jobs;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

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
        var csvFormat = CSVFormat.DEFAULT.withHeader("directory", "foundViolation", "violationsFile");
        try (var ps = new PrintStream(resultFile)) {
            try (var csvPrinter = new CSVPrinter(ps, csvFormat)) {
                for (var directory : getBlackboard().getPartitionIds()) {
                    var result = getBlackboard().getPartition(directory);
                    csvPrinter.print(result.getDirectory()
                        .getAbsolutePath());
                    csvPrinter.print(result.hasFoundViolations());
                    csvPrinter.print(result.getViolationsFile().getAbsolutePath());
                    csvPrinter.println();
                }
            }
        } catch (IOException e) {
            throw new JobFailedException("Could not serialize results to CSV.", e);
        }
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
