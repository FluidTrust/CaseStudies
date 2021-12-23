package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis

import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequenceElement
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.CallingActionSequenceElement
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.SEFFActionSequenceElement
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.UserActionSequenceElement
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.VariableProvidingActionSequence
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.util.ActionSequenceQueryUtils
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.util.PCMConversionUtils
import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Stack
import org.eclipse.emf.ecore.EObject
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.ConfidentialityVariableCharacterisation
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.Characteristic
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.expression.LhsEnumCharacteristicReference
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.expression.NamedEnumCharacteristicReference
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.expression.VariableCharacterizationLhs
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.repository.OperationalDataStoreComponent
import org.palladiosimulator.dataflow.confidentiality.pcm.model.profile.ProfileConstants
import org.palladiosimulator.dataflow.confidentiality.pcm.queryutils.ModelQueryUtils
import org.palladiosimulator.dataflow.confidentiality.pcm.queryutils.PcmQueryUtils
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.CharacteristicType
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.And
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.ContainerCharacteristicReference
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.False
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.Not
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.Or
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.Term
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.expressions.True
import org.palladiosimulator.mdsdprofiles.api.StereotypeAPI
import org.palladiosimulator.pcm.allocation.Allocation
import org.palladiosimulator.pcm.core.composition.AssemblyContext
import org.palladiosimulator.pcm.core.entity.Entity
import org.palladiosimulator.pcm.repository.OperationSignature
import org.palladiosimulator.pcm.seff.AbstractAction
import org.palladiosimulator.pcm.seff.ExternalCallAction
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification
import org.palladiosimulator.pcm.seff.SetVariableAction
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall
import org.palladiosimulator.pcm.usagemodel.UsageScenario

class CharacteristicsCalculator {
	
	static val extension PcmQueryUtils pcmQueryUtils = new PcmQueryUtils
	static val extension PCMConversionUtils pcmConversionUtils = new PCMConversionUtils
	static val extension ModelQueryUtils modelQueryUtils = new ModelQueryUtils
	static val extension ActionSequenceQueryUtils actionSequenceQueryUtils = new ActionSequenceQueryUtils
	val Allocation allocation
	
	new(Allocation allocation) {
		this.allocation = allocation
	} 


	def boolean isAvailable(ActionSequenceElement<?> element, EnumCharacteristicType characteristicType, Literal characteristicValue) {
		val characteristics = (element as ActionSequenceElement<Entity>).getEffectiveCharacteristics.filter(EnumCharacteristic)
		characteristics.exists[type == characteristicType && values.contains(characteristicValue)]
	}
	
	def boolean isAvailable(List<ActionSequenceElement<?>> sequence, String variableName, EnumCharacteristicType characteristicType, Literal characteristicValue) {
		val currentElement = sequence.last
		val variableUsage = (currentElement.element as Entity).getVariableUsageByName(currentElement, variableName)
		val assignments = variableUsage.variableCharacterisation_VariableUsage.filter(ConfidentialityVariableCharacterisation).toList.reverseView
		
		for (assignment : assignments) {
			if (assignment.lhs.matches(characteristicType, characteristicValue)) {				
				return assignment.rhs.evaluateRhs(sequence, characteristicType, characteristicValue)
			}
		}		
		
		false
	}
	

	protected def dispatch boolean evaluateRhs(ContainerCharacteristicReference term, List<ActionSequenceElement<?>> sequence, EnumCharacteristicType requestedCT, Literal requestedLiteral) {
		val ct = term.characteristicType as EnumCharacteristicType ?: requestedCT
		val literal = term.literal ?: requestedLiteral
		val requiringElement = sequence.last
		requiringElement.isAvailable(ct, literal)
	}
	
	protected def dispatch boolean evaluateRhs(NamedEnumCharacteristicReference term, List<ActionSequenceElement<?>> sequence, EnumCharacteristicType requestedCT, Literal requestedLiteral) {
		val variableName = term.namedReference.stringName
		val ct = term.characteristicType as EnumCharacteristicType ?: requestedCT
		val literal = term.literal ?: requestedLiteral
		val requiringElement = sequence.last
		val targetSequence = sequence.findVariableProvidingActionSequence(requiringElement, variableName)
		if (sequence.equals(targetSequence)) {
			throw new IllegalStateException
		}
		targetSequence.actionSequence.isAvailable(targetSequence.variableName , ct, literal)
	}
	
	protected def dispatch boolean evaluateRhs(And term, List<ActionSequenceElement<?>> sequence, EnumCharacteristicType requestedCT, Literal requestedLiteral) {
		term.left.evaluateRhs(sequence, requestedCT, requestedLiteral) && term.right.evaluateRhs(sequence, requestedCT, requestedLiteral)
	}
	
	protected def dispatch boolean evaluateRhs(Or term, List<ActionSequenceElement<?>> sequence, EnumCharacteristicType requestedCT, Literal requestedLiteral) {
		term.left.evaluateRhs(sequence, requestedCT, requestedLiteral) || term.right.evaluateRhs(sequence, requestedCT, requestedLiteral)
	}
	
	protected def dispatch boolean evaluateRhs(Not term, List<ActionSequenceElement<?>> sequence, EnumCharacteristicType requestedCT, Literal requestedLiteral) {
		!term.term.evaluateRhs(sequence, requestedCT, requestedLiteral)
	}
	
	protected def dispatch boolean evaluateRhs(False term, List<ActionSequenceElement<?>> sequence, EnumCharacteristicType requestedCT, Literal requestedLiteral) {
		false
	}
	
	protected def dispatch boolean evaluateRhs(True term, List<ActionSequenceElement<?>> sequence, EnumCharacteristicType requestedCT, Literal requestedLiteral) {
		true
	}
	
	protected def dispatch boolean evaluateRhs(Term term, List<ActionSequenceElement<?>> sequence, EnumCharacteristicType requestedCT, Literal requestedLiteral) {
		throw new IllegalArgumentException
	}
	
	
	
	
	protected static def dispatch boolean matches(LhsEnumCharacteristicReference lhs, EnumCharacteristicType characteristicType, Literal characteristicValue) {
		if (lhs.characteristicType === null) {
			return true
		}
		
		if ((lhs.characteristicType as EnumCharacteristicType).type != characteristicType.type) {
			return false
		}
		
		if (lhs.literal === null || lhs.literal == characteristicValue) {
			return true
		}
		
		false
	}
	
	protected static def dispatch boolean matches(VariableCharacterizationLhs lhs, EnumCharacteristicType characteristicType, Literal characteristicValue) {
		throw new IllegalArgumentException
	}
	
	
	
	protected def dispatch VariableProvidingActionSequence findVariableProvidingActionSequence(List<ActionSequenceElement<?>> sequence, CallingActionSequenceElement<?> requiringElement, String variableName) {
		if (sequence.last != requiringElement) {
			throw new IllegalArgumentException
		}
		
		val requiringAction = requiringElement.element as Entity

		// see if we are looking for a return variable
		if (!requiringElement.callingPart) {

			val calledSEFF = requiringAction.findCalledSEFF(requiringElement.context)
			val calledComponent = calledSEFF.seff.basicComponent_ServiceEffectSpecification
			val calledSEFFHasReturn = (calledSEFF.seff.describedService__SEFF as OperationSignature).returnType__OperationSignature !== null
			
			if (calledComponent instanceof OperationalDataStoreComponent && calledSEFFHasReturn) {
				// we are looking for the result of a database query 
				// TODO assumption here is that the DB has been filled within the given action sequence
				for (var i = sequence.size() -2; i >= 0; i--) {
					val currentElement = sequence.get(i)
					val currentAction = currentElement.element as Entity
					
					if (currentElement instanceof CallingActionSequenceElement && (currentElement as CallingActionSequenceElement<?>).isCallingPart) {
						val currentCalledSEFF = currentAction.findCalledSEFF(currentElement.context)
						if (currentCalledSEFF.context == calledSEFF.context && currentCalledSEFF.seff != calledSEFF.seff) {
							val variableNames = currentElement.getVariableNames
							if (variableNames.size !== 1) {
								throw new IllegalStateException
							}
							val newVariableName = variableNames.last
							return new VariableProvidingActionSequence(sequence.subList(0, i + 1), newVariableName)
						}
					}
				}
				throw new IllegalStateException
			}
			
			// filter sequence by actions of called SEFF and look for variables in SetVariableActions
			for (var i = sequence.size() - 2; i >= 0; i--) {
				val currentElement = sequence.get(i)
				val currentAction = currentElement.element as Entity
				if (currentElement.context.equals(calledSEFF.context) && currentAction instanceof SetVariableAction && currentAction.findParentOfType(ServiceEffectSpecification, false) == calledSEFF.seff) {
					val foundVariableUsage = currentAction.getVariableUsageByName(currentElement, variableName)
					if (foundVariableUsage !== null) {
						return new VariableProvidingActionSequence(sequence.subList(0, i + 1), variableName)
					}
				}
			}

		}

		// see if we are looking for a variable in current behavior
		return _findVariableProvidingActionSequence(sequence, requiringElement as ActionSequenceElement<?>, variableName)
	}
	
	protected def dispatch VariableProvidingActionSequence findVariableProvidingActionSequence(List<ActionSequenceElement<?>> sequence, ActionSequenceElement<?> requiringElement, String variableName) {
		if (sequence.last != requiringElement) {
			throw new IllegalArgumentException
		}
		val requiringAction = requiringElement.element
		val requiringSEFF = requiringAction.findParentOfType(ServiceEffectSpecification, false)
		var firstActionInBehavior = sequence.size() - 1
		for (var i = sequence.size() - 2; i >= 0; i--) {
			val currentElement = sequence.get(i)
			val currentAction = currentElement.element as Entity
			// find variable at any predecessor
			if (currentElement.context.equals(requiringElement.context) && currentAction.findParentOfType(ServiceEffectSpecification, false) == requiringSEFF) {
				firstActionInBehavior = i
				val foundVariableUsage = currentAction.getVariableUsageByName(currentElement, variableName)
				if (foundVariableUsage !== null) {
					return new VariableProvidingActionSequence(sequence.subList(0, i + 1), variableName)
				}
			}
		}
		
		// find variable via transmitted parameter
		val parameterProvidingElement = sequence.get(firstActionInBehavior - 1)
		val parameterProvidingAction = parameterProvidingElement.element as Entity
		val foundVariableUsage = parameterProvidingAction.getVariableUsageByName(parameterProvidingElement, variableName)
		if (foundVariableUsage !== null) {
			return new VariableProvidingActionSequence(sequence.subList(0, firstActionInBehavior), variableName)
		}

		throw new IllegalStateException
	}
	


	
	protected def dispatch findCalledSEFF(ExternalCallAction eca, Stack<AssemblyContext> context) {
		eca.role_ExternalService.findCalledSeff(eca.calledService_ExternalService, context)
	}
	
	protected def dispatch findCalledSEFF(EntryLevelSystemCall elsc, Stack<AssemblyContext> context) {
		elsc.providedRole_EntryLevelSystemCall.findCalledSeff(elsc.operationSignature__EntryLevelSystemCall, context)
	}
	
	


	protected def dispatch getEffectiveCharacteristics(UserActionSequenceElement<AbstractUserAction> element) {
		val scenarioBehavior = element.element.findParentOfType(UsageScenario, false)
		scenarioBehavior.characteristics
	}
	
	protected def dispatch getEffectiveCharacteristics(SEFFActionSequenceElement<AbstractAction> element) {
		val resourceContainers = element.context.map[assemblyContext | allocation.allocationContexts_Allocation.findFirst[allocationContext | allocationContext.assemblyContext_AllocationContext == assemblyContext]].filterNull.map[resourceContainer_AllocationContext]
		val elementsToConsider = new ArrayList<EObject>
		elementsToConsider += element.context.reverseView
		elementsToConsider += resourceContainers
		val characteristics = elementsToConsider.map[characteristics]
		characteristics.effectiveCharacteristics
	}
	
	protected static def effectiveCharacteristics(List<List<Characteristic<? extends CharacteristicType>>> characteristics) {
		val calculatedCharacteristics = new HashMap<CharacteristicType, Characteristic<? extends CharacteristicType>>
		for (characteristicLevel : characteristics) {
			for (characteristic : characteristicLevel) {
				calculatedCharacteristics.putIfAbsent(characteristic.type, characteristic)
			}
		}
		calculatedCharacteristics.values.sortBy[id]
	}
	
	protected static def getCharacteristics(EObject eobject) {
		StereotypeAPI.<List<Characteristic<? extends CharacteristicType>>>getTaggedValueSafe(eobject,
			ProfileConstants.characterisable.value, ProfileConstants.characterisable.stereotype).orElse(#[])
	}

}