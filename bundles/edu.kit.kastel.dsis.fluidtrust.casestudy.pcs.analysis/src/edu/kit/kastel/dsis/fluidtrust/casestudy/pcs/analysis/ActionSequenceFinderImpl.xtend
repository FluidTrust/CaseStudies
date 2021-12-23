package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis

import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequenceElement
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.CallingSEFFActionSequenceElementImpl
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.CallingUserActionSequenceElementImpl
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.SEFFActionSequenceElementImpl
import java.util.ArrayList
import java.util.Collection
import java.util.List
import java.util.Stack
import org.palladiosimulator.dataflow.confidentiality.pcm.queryutils.ModelQueryUtils
import org.palladiosimulator.dataflow.confidentiality.pcm.queryutils.PcmQueryUtils
import org.palladiosimulator.pcm.core.composition.AssemblyContext
import org.palladiosimulator.pcm.seff.AbstractAction
import org.palladiosimulator.pcm.seff.BranchAction
import org.palladiosimulator.pcm.seff.ExternalCallAction
import org.palladiosimulator.pcm.seff.SetVariableAction
import org.palladiosimulator.pcm.seff.StartAction
import org.palladiosimulator.pcm.seff.StopAction
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction
import org.palladiosimulator.pcm.usagemodel.Branch
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall
import org.palladiosimulator.pcm.usagemodel.Start
import org.palladiosimulator.pcm.usagemodel.Stop
import org.palladiosimulator.pcm.usagemodel.UsageModel
import java.util.Collections
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequence

class ActionSequenceFinderImpl {
	
	static val extension ModelQueryUtils MODEL_QUERY_UTILS = new ModelQueryUtils
	static val extension PcmQueryUtils PCM_QUERY_UTILS = new PcmQueryUtils

	def findActionSequencesForUsageModel(UsageModel usageModel) {
		val actionSequences = new ArrayList
		for (usageScenario : usageModel.usageScenario_UsageModel) {
			val startAction = usageScenario.scenarioBehaviour_UsageScenario.actions_ScenarioBehaviour.filter(Start).findFirst[true]
			actionSequences += Collections.unmodifiableCollection(startAction.findActionSequencesForUserAction(#[]))
		}
		Collections.unmodifiableCollection(actionSequences.map[sequence|new ActionSequence(sequence)])
	}
	
	protected def dispatch Collection<List<ActionSequenceElement<?>>> findActionSequencesForUserAction(Start action, List<ActionSequenceElement<?>> previousActions) {
		action.successor.findActionSequencesForUserAction(previousActions)
	}
	
	
	protected def dispatch Collection<List<ActionSequenceElement<?>>> findActionSequencesForUserAction(Stop action, List<ActionSequenceElement<?>> previousActions) {
		val parentAction = action.findParentOfType(AbstractUserAction, false)
		if (parentAction === null) {
			return #[previousActions]
		}
		parentAction.successor.findActionSequencesForUserAction(previousActions)
	}
	
	protected def dispatch Collection<List<ActionSequenceElement<?>>> findActionSequencesForUserAction(EntryLevelSystemCall action, List<ActionSequenceElement<?>> previousActions) {
		val currentActionSequence = new ArrayList(previousActions)
		currentActionSequence += new CallingUserActionSequenceElementImpl(action, true)
		
		val calledRole = action.providedRole_EntryLevelSystemCall
		val calledSignature = action.operationSignature__EntryLevelSystemCall
		
		val calledSeff = calledRole.findCalledSeff(calledSignature, new Stack)
		val seffStartAction = calledSeff.seff.steps_Behaviour.filter(StartAction).findFirst[true]
		
		val callers = new Stack()
		callers += currentActionSequence.last
		
		seffStartAction.findActionSequencesForSEFFAction(calledSeff.context, callers, currentActionSequence)
	}
	
	protected def Collection<List<ActionSequenceElement<?>>> findActionSequencesForUserActionReturning(EntryLevelSystemCall action, List<ActionSequenceElement<?>> previousActions) {
		val currentActionSequence = new ArrayList(previousActions)
		currentActionSequence += new CallingUserActionSequenceElementImpl(action, false)
		action.successor.findActionSequencesForUserAction(currentActionSequence)
	}
	
	protected def dispatch Collection<List<ActionSequenceElement<?>>> findActionSequencesForUserAction(Branch action, List<ActionSequenceElement<?>> previousActions) {
		val result = new ArrayList
		for (transition : action.branchTransitions_Branch) {
			val startAction = transition.branchedBehaviour_BranchTransition.actions_ScenarioBehaviour.filter(Start).findFirst[true]
			val foundCallStacks = startAction.findActionSequencesForUserAction(previousActions)
			result += foundCallStacks
		}
		result
	}
	
	protected def dispatch Collection<List<ActionSequenceElement<?>>> findActionSequencesForSEFFAction(StartAction action, Stack<AssemblyContext> context, Stack<ActionSequenceElement<?>> callers, List<ActionSequenceElement<?>> previousActions) {
		action.successor_AbstractAction.findActionSequencesForSEFFAction(context, callers, previousActions)
	}
	
	protected def dispatch Collection<List<ActionSequenceElement<?>>> findActionSequencesForSEFFAction(StopAction action, Stack<AssemblyContext> context, Stack<ActionSequenceElement<?>> callers, List<ActionSequenceElement<?>> previousActions) {
		val actionParent = action.findParentOfType(AbstractAction, false)
		if (actionParent !== null) {
			return actionParent.successor_AbstractAction.findActionSequencesForSEFFAction(context, callers, previousActions)
		}
		
		val caller = callers.pop
		return caller.returnToCaller(callers, previousActions)
	}

	protected def dispatch Collection<List<ActionSequenceElement<?>>> findActionSequencesForSEFFAction(ExternalCallAction action, Stack<AssemblyContext> context, Stack<ActionSequenceElement<?>> callers, List<ActionSequenceElement<?>> previousActions) {
		val currentActionSequence = new ArrayList(previousActions)
		currentActionSequence += new CallingSEFFActionSequenceElementImpl(action, true, context)
		
		val calledSignature = action.calledService_ExternalService
		val calledRole = action.role_ExternalService
		
		val calledSeff = calledRole.findCalledSeff(calledSignature, context)
		val seffStartAction = calledSeff.seff.steps_Behaviour.filter(StartAction).findFirst[true]
		
		callers += currentActionSequence.last
		
		seffStartAction.findActionSequencesForSEFFAction(calledSeff.context, callers, currentActionSequence)
	}
	
	protected def Collection<List<ActionSequenceElement<?>>> findActionSequencesForSEFFActionReturning(ExternalCallAction action, Stack<AssemblyContext> context, Stack<ActionSequenceElement<?>> callers, List<ActionSequenceElement<?>> previousActions) {
		val currentActionSequence = new ArrayList(previousActions)
		currentActionSequence += new CallingSEFFActionSequenceElementImpl(action, false, context)
		action.successor_AbstractAction.findActionSequencesForSEFFAction(context, callers, currentActionSequence)
	}

	protected def dispatch Collection<List<ActionSequenceElement<?>>> findActionSequencesForSEFFAction(SetVariableAction action, Stack<AssemblyContext> context, Stack<ActionSequenceElement<?>> callers, List<ActionSequenceElement<?>> previousActions) {
		val currentActionSequence = new ArrayList(previousActions)
		currentActionSequence += new SEFFActionSequenceElementImpl(action, context)
		action.successor_AbstractAction.findActionSequencesForSEFFAction(context, callers, currentActionSequence)
	}
	
	protected def dispatch Collection<List<ActionSequenceElement<?>>> findActionSequencesForSEFFAction(BranchAction action, Stack<AssemblyContext> context, Stack<ActionSequenceElement<?>> callers, List<ActionSequenceElement<?>> previousActions) {
		val result = new ArrayList
		for (transition : action.branches_Branch) {
			val startAction = transition.branchBehaviour_BranchTransition.steps_Behaviour.filter(StartAction).findFirst[true]
			val foundCallStacks = startAction.findActionSequencesForSEFFAction(context, callers.copy, previousActions)
			result += foundCallStacks
		}
		result
	}
	
	protected def dispatch Collection<List<ActionSequenceElement<?>>> returnToCaller(CallingUserActionSequenceElementImpl caller, Stack<ActionSequenceElement<?>> callers, List<ActionSequenceElement<?>> previousActions) {
		if (!callers.isEmpty) {
			throw new IllegalStateException
		}
		caller.element.findActionSequencesForUserActionReturning(previousActions)
	}
	
	protected def dispatch Collection<List<ActionSequenceElement<?>>> returnToCaller(CallingSEFFActionSequenceElementImpl caller, Stack<ActionSequenceElement<?>> callers, List<ActionSequenceElement<?>> previousActions) {
		caller.element.findActionSequencesForSEFFActionReturning(caller.context, callers, previousActions)
	}
	
	protected def dispatch Collection<List<ActionSequenceElement<?>>> returnToCaller(ActionSequenceElement<?> caller, Stack<ActionSequenceElement<?>> callers, List<ActionSequenceElement<?>> previousActions) {
		throw new IllegalArgumentException("No dispatch logic for caller of type " + caller.class.simpleName + " available.")
	}

	protected static def <T> Stack<T> copy(Stack<T> stack) {
		val copy = new Stack<T>
		copy.addAll(stack)
		copy
	}
	
}