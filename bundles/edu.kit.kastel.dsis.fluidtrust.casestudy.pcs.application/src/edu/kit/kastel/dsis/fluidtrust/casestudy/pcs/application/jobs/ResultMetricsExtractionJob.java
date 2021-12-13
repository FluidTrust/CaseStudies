package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.jobs;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.dataflow.confidentiality.transformation.workflow.blackboards.KeyValueMDSDBlackboard;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class ResultMetricsExtractionJob extends AbstractBlackboardInteractingJob<KeyValueMDSDBlackboard> {

    protected final File directory;
    protected final String prologResultKey;
    protected final String analysisResultKey;

    public ResultMetricsExtractionJob(File directory, String prologResultKey, String analysisResultKey) {
        this.directory = directory;
        this.prologResultKey = prologResultKey;
        this.analysisResultKey = analysisResultKey;
    }

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        @SuppressWarnings("unchecked")
        var prologResults = getBlackboard().get(prologResultKey)
            .filter(List.class::isInstance)
            .map(List.class::cast)
            .filter(l -> l.stream()
                .allMatch(Map.class::isInstance))
            .map(l -> (List<Map<?, ?>>) l)
            .filter(l -> l.stream()
                .allMatch(m -> m.keySet()
                    .stream()
                    .allMatch(String.class::isInstance)))
            .map(l -> (List<Map<String, Object>>) ((List<?>) l))
            .orElseThrow(() -> new JobFailedException("Prolog results are not in expected format."));

        var analysisResult = createAnalysisReult(prologResults);

        getBlackboard().put(analysisResultKey, analysisResult);
    }

    private AnalysisResult createAnalysisReult(List<Map<String, Object>> prologResults) {
        var analysisResult = new AnalysisResult();

        analysisResult.setDirectory(directory);
        analysisResult.setHasFoundViolation(!prologResults.isEmpty());

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
