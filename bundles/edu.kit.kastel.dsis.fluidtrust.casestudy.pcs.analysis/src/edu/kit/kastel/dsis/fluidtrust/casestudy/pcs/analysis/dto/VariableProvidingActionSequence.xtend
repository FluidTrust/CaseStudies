package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto

import java.util.List
import org.eclipse.xtend.lib.annotations.Data

@Data
class VariableProvidingActionSequence {
	val List<ActionSequenceElement<?>> actionSequence
	val String variableName
}