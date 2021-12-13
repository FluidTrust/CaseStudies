package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.jobs;

import org.apache.commons.lang3.mutable.MutableObject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.dataflow.confidentiality.pcm.workflow.TransitiveTransformationTrace;
import org.palladiosimulator.dataflow.confidentiality.transformation.workflow.blackboards.KeyValueMDSDBlackboard;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.query.QueryBuilder;

public class CreateAnalysisQueryJob extends AbstractBlackboardInteractingJob<KeyValueMDSDBlackboard> {

    protected final String queryRuleName;
    protected final String traceKey;
    protected final String queryKey;
    protected final String additionsKey;
    protected final String queryVarsKey;

    public CreateAnalysisQueryJob(String queryRuleName, String traceKey, String queryKey, String additionsKey, String queryVarsKey) {
        this.queryRuleName = queryRuleName;
        this.traceKey = traceKey;
        this.queryKey = queryKey;
        this.additionsKey = additionsKey;
        this.queryVarsKey = queryVarsKey;
    }

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        var trace = getBlackboard().get(traceKey)
            .filter(TransitiveTransformationTrace.class::isInstance)
            .map(TransitiveTransformationTrace.class::cast)
            .orElseThrow(() -> new JobFailedException("Could not find transformation trace."));

        var assignedRolesCT = getEnumCharacteristicTypeByName(trace, "AssignedRoles");
        var acObjectCT = getEnumCharacteristicTypeByName(trace, "ACObject");

        var queryBuilder = new QueryBuilder(queryRuleName, trace, assignedRolesCT, acObjectCT);
        var acPolicy = queryBuilder.getAccessControlPolicyFacts();
        var queryRule = queryBuilder.getQueryRule();

        var additions = acPolicy + System.lineSeparator() + queryRule;
        
        getBlackboard().put(additionsKey, additions);
        getBlackboard().put(queryVarsKey, queryBuilder.getQueryVariables());
    }

    protected EnumCharacteristicType getEnumCharacteristicTypeByName(TransitiveTransformationTrace trace, String name)
            throws JobFailedException {
        var foundType = new MutableObject<EnumCharacteristicType>();
        var ids = trace.getFactId(ct -> {
            var correctType = ct instanceof EnumCharacteristicType && ct.getName()
                .equals(name);
            if (correctType) {
                foundType.setValue((EnumCharacteristicType) ct);
            }
            return correctType;

        });
        if (ids.size() != 1) {
            throw new JobFailedException("Characteristic type " + name + " could not be found.");
        }
        return foundType.getValue();
    }

    @Override
    public void cleanup(IProgressMonitor arg0) throws CleanupFailedException {
        // nothing to do here
    }

    @Override
    public String getName() {
        return "Create analysis query";
    }

}
