package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.jobs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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

        var queryCreationJob = new CreateAnalysisQueryJob(TransformPCMDFDToPrologJobBuilder.DEFAULT_TRACE_KEY,
                PROLOG_QUERY_KEY, PROLOG_ADDITIONS_KEY);
        job.add(queryCreationJob);
        
        job.execute(monitor);

        // result after execution:
        var program = (String)job.getBlackboard()
                .get(PROLOG_PROGRAM_KEY).get();
        var additions = job.getBlackboard().get(PROLOG_ADDITIONS_KEY).orElse("");
        var fullProgram = program + System.lineSeparator() + additions;
        
//        try {
//            Files.writeString(new File("/home/stephan/Downloads/pcs.pl").toPath(), fullProgram);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

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
