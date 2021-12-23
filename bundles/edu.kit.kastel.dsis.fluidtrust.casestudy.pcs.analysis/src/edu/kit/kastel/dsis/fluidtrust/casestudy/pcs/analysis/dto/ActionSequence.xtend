package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto

import java.util.List
import org.eclipse.xtend.lib.annotations.Delegate

class ActionSequence implements List<ActionSequenceElement<?>> {
	@Delegate List<ActionSequenceElement<?>> sequence
	
	new(List<ActionSequenceElement<?>> sequence) {
		this.sequence = sequence
	}
}
