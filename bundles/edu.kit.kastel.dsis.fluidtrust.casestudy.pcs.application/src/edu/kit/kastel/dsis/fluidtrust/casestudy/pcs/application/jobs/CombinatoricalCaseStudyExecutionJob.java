package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.jobs;

import org.palladiosimulator.dataflow.confidentiality.transformation.workflow.blackboards.KeyValueMDSDBlackboard;

import de.uka.ipd.sdq.workflow.jobs.DynamicSequentialBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.mdsd.blackboard.ModelLocation;

public class CombinatoricalCaseStudyExecutionJob<T extends KeyValueMDSDBlackboard> extends DynamicSequentialBlackboardInteractingJob<T> {

	protected final ModelLocation usageModelLocation;
	protected final ModelLocation allocationModelLocation;
	protected T originalBlackboard;

	public CombinatoricalCaseStudyExecutionJob(ModelLocation usageModelLocation,
			ModelLocation allocationModelLocation, IBlackboardInteractingJob<T> modifyingJob) {
		this.usageModelLocation = usageModelLocation;
		this.allocationModelLocation = allocationModelLocation;
	}

	
	
	@Override
	public void setBlackboard(T blackboard) {
		if (originalBlackboard == null) {
			originalBlackboard = blackboard;			
		}
		super.setBlackboard(blackboard);
	}

}
