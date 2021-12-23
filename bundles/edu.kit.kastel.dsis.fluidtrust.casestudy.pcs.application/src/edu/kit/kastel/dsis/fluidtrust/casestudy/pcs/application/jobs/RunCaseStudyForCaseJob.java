package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.jobs;

import java.io.File;
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.palladiosimulator.dataflow.confidentiality.transformation.workflow.blackboards.KeyValueMDSDBlackboard;
import org.palladiosimulator.dataflow.confidentiality.transformation.workflow.jobs.LoadModelJob;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.SequentialBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import de.uka.ipd.sdq.workflow.mdsd.blackboard.ModelLocation;

public class RunCaseStudyForCaseJob extends AbstractBlackboardInteractingJob<AnalysisResultBlackboard> {

    protected static final String PCM_MODEL_PARTITION = "pcmModels";
    protected static final String RESULT_METRICS_KEY = "resultMetricsKey";
    private static final String ALL_CHARACTERISTICS_RESULT_KEY = "resultAllCharacteristicsKey";
    private static final String VIOLATIONS_RESULT_KEY = "resultViolationsKey";
    protected final File usageModel;
    protected final File allocationModel;
    protected final File directory;
    protected final String stackLimit;

    public RunCaseStudyForCaseJob(File usageModel, File allocationModel, String stackLimit) {
        this.usageModel = usageModel;
        this.allocationModel = allocationModel;
        this.directory = usageModel.getParentFile();
        this.stackLimit = stackLimit;
    }

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        var job = new SequentialBlackboardInteractingJob<KeyValueMDSDBlackboard>();
        job.setBlackboard(new KeyValueMDSDBlackboard());

        var usageModelURI = URI.createFileURI(usageModel.getAbsolutePath());
        var usageModelLocation = new ModelLocation(PCM_MODEL_PARTITION, usageModelURI);
        var allocationURI = URI.createFileURI(allocationModel.getAbsolutePath());
        var allocationLocation = new ModelLocation(PCM_MODEL_PARTITION, allocationURI);

        var loadUsageModelJob = new LoadModelJob<KeyValueMDSDBlackboard>(Arrays.asList(usageModelLocation, allocationLocation));
        job.add(loadUsageModelJob);
        
        var runAnalysisJob = new RunJavaBasedAnalysisJob(usageModelLocation, allocationLocation, ALL_CHARACTERISTICS_RESULT_KEY, VIOLATIONS_RESULT_KEY);
        job.add(runAnalysisJob);

        var allCharacteristicsResultFile = new File(directory, "allCharacteristics.json");
        var serializeAllCharacteristicsJob = new SerialiseActionBasedQueryResultJob(ALL_CHARACTERISTICS_RESULT_KEY, allCharacteristicsResultFile);
        job.add(serializeAllCharacteristicsJob);
        
        var violationsResultFile = new File(directory, "violations.json");
        var serializeViolationsJob = new SerialiseActionBasedQueryResultJob(VIOLATIONS_RESULT_KEY, violationsResultFile);
        job.add(serializeViolationsJob);
        
        var createAnalysisResultJob = new ResultMetricsExtractionJob(directory, VIOLATIONS_RESULT_KEY, violationsResultFile, RESULT_METRICS_KEY);
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
        return "Analysis run for " + directory.getName();
    }

}
