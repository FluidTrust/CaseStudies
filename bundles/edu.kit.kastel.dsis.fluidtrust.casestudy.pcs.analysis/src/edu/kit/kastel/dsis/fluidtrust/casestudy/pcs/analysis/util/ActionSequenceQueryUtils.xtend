package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.util

import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequenceElement
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.CallingActionSequenceElement
import org.palladiosimulator.pcm.core.entity.Entity
import org.palladiosimulator.pcm.parameter.VariableUsage
import org.palladiosimulator.pcm.seff.ExternalCallAction
import org.palladiosimulator.pcm.seff.SetVariableAction
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall

class ActionSequenceQueryUtils {
	
	val extension PCMConversionUtils pcmConversionUtils = new PCMConversionUtils
	
	def dispatch getVariableUsageByName(SetVariableAction action, ActionSequenceElement<?> element, String name) {
		action.localVariableUsages_SetVariableAction.getVariableUsageByName(name)
	}
	
	def dispatch getVariableUsageByName(ExternalCallAction action, ActionSequenceElement<?> element, String name) {
		val typedElement = element as CallingActionSequenceElement<?>
		val variableUsages = typedElement.callingPart ? action.inputVariableUsages__CallAction : action.returnVariableUsage__CallReturnAction
		return variableUsages.getVariableUsageByName(name)
	}
	
	def dispatch getVariableUsageByName(EntryLevelSystemCall action, ActionSequenceElement<?> element, String name) {
		val typedElement = element as CallingActionSequenceElement<?>
		val variableUsages = typedElement.callingPart ? action.inputParameterUsages_EntryLevelSystemCall : action.outputParameterUsages_EntryLevelSystemCall
		return variableUsages.getVariableUsageByName(name)
	}
	
	protected def getVariableUsageByName(Iterable<VariableUsage> variableUsages, String name) {
		variableUsages.findFirst[namedReference__VariableUsage.stringName == name]
	}
	
	
	def getVariableNames(ActionSequenceElement<?> element) {
		(element.element as Entity).getVariableNames(element)
	}
	
	protected def dispatch getVariableNames(SetVariableAction action, ActionSequenceElement<?> element) {
		action.localVariableUsages_SetVariableAction.map[namedReference__VariableUsage.stringName]
	}
	
	protected def dispatch getVariableNames(ExternalCallAction action, ActionSequenceElement<?> element) {
		val typedElement = element as CallingActionSequenceElement<?>
		val variableUsages = typedElement.callingPart ? action.inputVariableUsages__CallAction : action.returnVariableUsage__CallReturnAction
		variableUsages.map[namedReference__VariableUsage.stringName]
	}
	
	protected def dispatch getVariableNames(EntryLevelSystemCall action, ActionSequenceElement<?> element) {
		val typedElement = element as CallingActionSequenceElement<?>
		val variableUsages = typedElement.callingPart ? action.inputParameterUsages_EntryLevelSystemCall : action.outputParameterUsages_EntryLevelSystemCall
		variableUsages.map[namedReference__VariableUsage.stringName]
	}
}