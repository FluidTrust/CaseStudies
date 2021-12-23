package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.jobs;

import java.io.File;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.dataflow.confidentiality.transformation.workflow.blackboards.KeyValueMDSDBlackboard;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult;

public class ResultMetricsExtractionJob extends AbstractBlackboardInteractingJob<KeyValueMDSDBlackboard> {

    protected final File directory;
    protected final String violationsKey;
    protected final File violationsFile;
    protected final String analysisResultKey;

    public ResultMetricsExtractionJob(File directory, String violationsKey, File violationsFile, String analysisResultKey) {
        this.directory = directory;
        this.violationsKey = violationsKey;
        this.violationsFile = violationsFile;
        this.analysisResultKey = analysisResultKey;
    }

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        var violations = getBlackboard().get(violationsKey)
            .filter(ActionBasedQueryResult.class::isInstance)
            .map(ActionBasedQueryResult.class::cast)
            .orElseThrow(() -> new JobFailedException("Prolog results are not in expected format."));

        var analysisResult = createAnalysisResult(violations);
        getBlackboard().put(analysisResultKey, analysisResult);
    }

    private AnalysisResult createAnalysisResult(ActionBasedQueryResult violations) {
        var analysisResult = new AnalysisResult();

        analysisResult.setDirectory(directory);
        analysisResult.setViolationsFile(violationsFile);
        analysisResult.setHasFoundViolations(violations.getResults()
            .values()
            .stream()
            .flatMap(Collection::stream)
            .findAny()
            .isPresent());

        return analysisResult;
    }

    @Override
    public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
        // nothing to do here
    }

    @Override
    public String getName() {
        return "Result metrics extraction";
    }

}
