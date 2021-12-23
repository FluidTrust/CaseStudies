package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.util

import de.uka.ipd.sdq.stoex.AbstractNamedReference
import de.uka.ipd.sdq.stoex.VariableReference

class PCMConversionUtils {
	
	def getStringName(AbstractNamedReference namedReference) {
		if (namedReference instanceof VariableReference) {
			return namedReference.referenceName
		}
		throw new IllegalArgumentException("Only " + VariableReference.simpleName + " are supported.")
	}
	
}