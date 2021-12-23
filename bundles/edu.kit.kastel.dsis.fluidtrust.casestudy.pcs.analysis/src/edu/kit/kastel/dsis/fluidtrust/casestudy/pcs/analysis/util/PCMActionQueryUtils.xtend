package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.util

import java.util.ArrayList
import java.util.HashSet
import java.util.LinkedList
import org.palladiosimulator.pcm.seff.AbstractAction
import org.palladiosimulator.pcm.seff.BranchAction
import org.palladiosimulator.pcm.seff.StopAction
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction
import org.palladiosimulator.pcm.usagemodel.Branch
import org.palladiosimulator.pcm.usagemodel.Stop

class PCMActionQueryUtils {
	
	def transitivePredecessor(AbstractUserAction action, AbstractUserAction predecessor) {
		val closure = new HashSet
		val queue = new LinkedList
		queue += action.predecessor
		while (!queue.isEmpty) {
			val current = queue.pop
			if (current !== null) {
				closure += current
				queue += current.predecessor
			}
		}
		closure.contains(predecessor)
	}
	
	protected def dispatch getPredecessors(AbstractUserAction action) {
		action.predecessor
	}
	
	protected def dispatch getPredecessors(Branch action) {
		val result = new ArrayList
		result += action.predecessor
		result += action.branchTransitions_Branch.map[branchedBehaviour_BranchTransition].flatMap[actions_ScenarioBehaviour].filter(Stop)
		result
	}
	
	def transitivePredecessor(AbstractAction action, AbstractAction predecessor) {
		val closure = new HashSet
		val queue = new LinkedList
		queue += action.predecessor_AbstractAction
		while (!queue.isEmpty) {
			val current = queue.pop
			if (current !== null) {
				closure += current
				queue += current.predecessor_AbstractAction
			}
		}
		closure.contains(predecessor)
	}
	
	protected def dispatch getPredecessors(AbstractAction action) {
		action.predecessor_AbstractAction
	}
	
	protected def dispatch getPredecessors(BranchAction action) {
		val result = new ArrayList
		result += action.predecessor_AbstractAction
		result += action.branches_Branch.map[branchBehaviour_BranchTransition].flatMap[steps_Behaviour].filter(StopAction)
		result
	}
	
	
	
}