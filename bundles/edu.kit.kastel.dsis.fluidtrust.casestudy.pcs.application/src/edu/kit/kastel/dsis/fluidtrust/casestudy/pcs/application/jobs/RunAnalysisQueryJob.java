package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.dataflow.confidentiality.transformation.workflow.blackboards.KeyValueMDSDBlackboard;
import org.prolog4j.Prover;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.internal.Activator;

public class RunAnalysisQueryJob extends AbstractBlackboardInteractingJob<KeyValueMDSDBlackboard> {

    protected final String queryRuleName;
    protected final String prologProgramKey;
    protected final String prologAdditionsKey;
    protected final String queryVariablesKey;
    protected final String resultsKey;

    public RunAnalysisQueryJob(String queryRuleName, String prologProgramKey, String prologAdditionsKey,
            String queryVariablesKey, String resultsKey) {
        this.queryRuleName = queryRuleName;
        this.prologProgramKey = prologProgramKey;
        this.prologAdditionsKey = prologAdditionsKey;
        this.queryVariablesKey = queryVariablesKey;
        this.resultsKey = resultsKey;
    }

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        // extract information from blackboard
        var program = getBlackboard().get(prologProgramKey)
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .orElseThrow(() -> new JobFailedException("No prolog program available to execute."));
        var additions = getBlackboard().get(prologAdditionsKey)
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .orElse("");
        @SuppressWarnings("unchecked")
        var queryVariables = getBlackboard().get(queryVariablesKey)
            .filter(List.class::isInstance)
            .map(List.class::cast)
            .filter(l -> l.stream()
                .allMatch(String.class::isInstance))
            .map(l -> (List<String>) l)
            .orElseThrow(() -> new JobFailedException("No query variables available."));

        // instantiate a prolog interpreter
        var proverFactories = Activator.getInstance()
            .getProverManager()
            .getProvers()
            .values();
        if (proverFactories.size() != 1) {
            throw new JobFailedException("No unique prolog interpreter available.");
        }
        var proverFactory = proverFactories.iterator()
            .next();
        var prover = proverFactory.createProver();

        // run actual analysis
        monitor.beginTask("Run analysis in Prolog", 1);
        var results = runAnalysis(prover, program, additions, queryVariables);
        monitor.worked(1);

        // store analysis results in blackboard
        getBlackboard().put(resultsKey, results);
    }

    protected List<Map<String, Object>> runAnalysis(Prover prover, String program, String additions,
            List<String> queryVariables) {
        var fullProgram = program + System.lineSeparator() + additions;
        var goal = String.format("%s(%s)", queryRuleName, queryVariables.stream()
            .collect(Collectors.joining(", ")));

        prover.addTheory(fullProgram);
        var query = prover.query(goal);
        var solution = query.solve();

        List<Map<String, Object>> result = new ArrayList<>();

        for (var iter = solution.iterator(); iter.hasNext(); iter.next()) {
            var variables = new HashMap<String, Object>();
            result.add(variables);
            for (var queryVariable : queryVariables) {
                variables.put(queryVariable, iter.get(queryVariable));
            }
        }

        return result;
    }

    @Override
    public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
        // nothing to do here
    }

    @Override
    public String getName() {
        return "Run Analysis Query";
    }

}
