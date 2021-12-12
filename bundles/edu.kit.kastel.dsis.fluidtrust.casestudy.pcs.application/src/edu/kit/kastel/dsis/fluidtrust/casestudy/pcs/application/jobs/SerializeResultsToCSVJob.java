package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.jobs;

import java.io.File;

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
        // TODO Auto-generated method stub
        
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
