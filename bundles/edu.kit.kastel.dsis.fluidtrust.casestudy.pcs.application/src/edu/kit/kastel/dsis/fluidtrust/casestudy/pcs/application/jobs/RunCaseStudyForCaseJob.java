package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.jobs;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.palladiosimulator.dataflow.confidentiality.pcm.workflow.jobs.TransformPCMDFDToPrologJobBuilder;
import org.palladiosimulator.dataflow.confidentiality.transformation.workflow.blackboards.KeyValueMDSDBlackboard;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.SequentialBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class RunCaseStudyForCaseJob extends AbstractBlackboardInteractingJob<AnalysisResultBlackboard> {

    protected static final String PROLOG_PROGRAM_KEY = "prologProgramKey";
    protected static final String PROLOG_QUERY_KEY = "prologQueryKey";
    protected static final String PROLOG_ADDITIONS_KEY = "prologAdditionsKey";
    protected static final String PROLOG_QUERY_VARS_KEY = "prologQueryVarsKey";
    protected static final String PROLOG_RESULTS_KEY = "prologQueryResultsKey";
    protected static final String RESULT_METRICS_KEY = "resultMetricsKey";
    protected static final String QUERY_RULE_NAME = "query";
    protected final File usageModel;
    protected final File allocationModel;
    protected final String directory;

    public RunCaseStudyForCaseJob(File usageModel, File allocationModel) {
        this.usageModel = usageModel;
        this.allocationModel = allocationModel;
        this.directory = usageModel.getParentFile()
            .getName();
    }

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        var job = new SequentialBlackboardInteractingJob<KeyValueMDSDBlackboard>();
        job.setBlackboard(new KeyValueMDSDBlackboard());

        var usageModelURI = URI.createFileURI(usageModel.getAbsolutePath());
        var allocationURI = URI.createFileURI(allocationModel.getAbsolutePath());

        var transformationJob = TransformPCMDFDToPrologJobBuilder.create()
            .addAllocationModelByURI(allocationURI)
            .addUsageModelsByURI(usageModelURI)
            .addSerializeModelToString(PROLOG_PROGRAM_KEY)
            .enablePerformanceTweaks()
            .build();
        job.add(transformationJob);

        var queryCreationJob = new CreateAnalysisQueryJob(QUERY_RULE_NAME, TransformPCMDFDToPrologJobBuilder.DEFAULT_TRACE_KEY,
                PROLOG_QUERY_KEY, PROLOG_ADDITIONS_KEY, PROLOG_QUERY_VARS_KEY);
        job.add(queryCreationJob);
        
        var runAnalysisJob = new RunAnalysisQueryJob(QUERY_RULE_NAME, PROLOG_PROGRAM_KEY, PROLOG_ADDITIONS_KEY,
                PROLOG_QUERY_VARS_KEY, PROLOG_RESULTS_KEY);
        job.add(runAnalysisJob);
        
        var createAnalysisResultJob = new ResultMetricsExtractionJob(usageModel.getParentFile(), PROLOG_RESULTS_KEY, RESULT_METRICS_KEY);
        job.add(createAnalysisResultJob);
        
        job.execute(monitor);
        
        AnalysisResult analysisResult = job.getBlackboard()
            .get(RESULT_METRICS_KEY)
            .filter(AnalysisResult.class::isInstance)
            .map(AnalysisResult.class::cast)
            .orElseThrow(() -> new JobFailedException("Could not retrieve analysis result metrics."));

        getBlackboard().addPartition(usageModel.getParent(), analysisResult);
    }

    @Override
    public void cleanup(IProgressMonitor arg0) throws CleanupFailedException {
        // nothing to do here
    }

    @Override
    public String getName() {
        return "Analysis run for " + directory;
    }

}
