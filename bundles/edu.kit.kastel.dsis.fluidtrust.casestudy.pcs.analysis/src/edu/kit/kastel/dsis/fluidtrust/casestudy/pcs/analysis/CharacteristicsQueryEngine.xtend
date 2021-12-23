package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis

import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQuery
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult.ActionBasedQueryResultDTO
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequence
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequenceElement
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.CharacteristicValue
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.util.ActionSequenceQueryUtils
import java.util.ArrayList
import java.util.Collection
import java.util.HashMap
import java.util.List
import java.util.Map

class CharacteristicsQueryEngine {

	static val extension ActionSequenceQueryUtils actionSequenceQueryUtils = new ActionSequenceQueryUtils
	val CharacteristicsCalculator characteristicsCalculator
	val Collection<ActionSequence> actionSequences

	new(CharacteristicsCalculator characteristicsCalculator, Collection<ActionSequence> actionSequences) {
		this.characteristicsCalculator = characteristicsCalculator
		this.actionSequences = actionSequences
	}

	def ActionBasedQueryResult query(ActionBasedQuery query) {
		val result = new ActionBasedQueryResult
		for (actionSequence : actionSequences) {
			result.addResult(actionSequence, query(actionSequence, query))
		}
		result
	}

	protected def query(ActionSequence actionSequence, ActionBasedQuery query) {
		val results = new ArrayList
		for (var i = 0; i < actionSequence.size(); i++) {
			val action = actionSequence.get(i)
			if (query.actionSelector.test(action)) {

				val nodeCharacteristics = new ArrayList
				for (nodeCharacteristicTest : query.nodeCharacteristicsToTest) {
					if (characteristicsCalculator.isAvailable(action, nodeCharacteristicTest.characteristicType, nodeCharacteristicTest.characteristicLiteral)) {
						nodeCharacteristics += nodeCharacteristicTest
					}
				}

				val currentSequence = actionSequence.subList(0, i+1)
				val allVariableNames = action.variableNames
				val Map<String,Collection<CharacteristicValue>> dataCharacteristics = new HashMap
				for (dataCharacteristicTest : query.dataCharacteristicsToTest) {
					for (variableName : allVariableNames) {
						if (currentSequence.isAvailableOnVariable(variableName, dataCharacteristicTest)) {
							dataCharacteristics.computeIfAbsent(variableName, [new ArrayList]) += dataCharacteristicTest
						}
					}
				}

				results += new ActionBasedQueryResultDTO(action, dataCharacteristics, nodeCharacteristics)
			}
		}
		return results
	}

	protected def isAvailableOnVariable(List<ActionSequenceElement<?>> sequence, String variableName, CharacteristicValue characteristicValueTest) {
		characteristicsCalculator.isAvailable(sequence, variableName, characteristicValueTest.characteristicType, characteristicValueTest.characteristicLiteral)
	}

}