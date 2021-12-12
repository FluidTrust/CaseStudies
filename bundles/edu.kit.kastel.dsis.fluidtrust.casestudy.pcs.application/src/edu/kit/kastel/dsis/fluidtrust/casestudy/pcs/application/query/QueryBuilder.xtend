package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.query

import de.uka.ipd.sdq.workflow.jobs.JobFailedException
import org.palladiosimulator.dataflow.confidentiality.pcm.workflow.TransitiveTransformationTrace
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Enumeration

class QueryBuilder {

        protected val TransitiveTransformationTrace trace;
        protected val EnumCharacteristicType assignedRolesCT;
        protected val EnumCharacteristicType acObjectCT;

        new(TransitiveTransformationTrace trace, EnumCharacteristicType assignedRolesCT, EnumCharacteristicType acObjectCT) {
            super();
            this.trace = trace;
            this.assignedRolesCT = assignedRolesCT;
            this.acObjectCT = acObjectCT;
        }

        def String getAccessControlPolicyFacts() throws JobFailedException '''
			% =====================
			% Access control policy
			% =====================
			«FOR e : AccessControlPolicy.POLICY.entrySet.sortBy[key.getName()]»
				«FOR acObject : e.value»
					allowed('«getFactId(assignedRolesCT.getType(), e.key)»', '«getFactId(acObjectCT.getType(), acObject)»').
				«ENDFOR»
			«ENDFOR»
        '''

		def getQueryVariables() {
			#["N", "PIN", "R", "O", "S"]
		}

		def String getQueryRule() throws JobFailedException '''
			% ==========
			% Query rule
			% ==========
			query(«queryVariables.join(", ")») :-
				(process(N);actor(N);store(N)),
				\+ (isACallSending(N); isACallReceiving(N); isASEFFEntry(N); isASEFFExit(N); containedInStore(N)),
				nodeCharacteristic(N, '«getFactId(assignedRolesCT)»', R),
				inputPin(N, PIN),
				flowTree(N, PIN, S),
				characteristic(N, PIN, '«getFactId(acObjectCT)»', O, S),
				\+ allowed(R, O).
		'''
        
        protected def String getFactId(EnumCharacteristicType ct) throws JobFailedException {
            var factIds = trace.getFactIds(ct);
            if (factIds.size() != 1) {
                throw new JobFailedException("Could not find fact for characteristic type " + ct.getName() + ".");
            }
            return factIds.iterator().next();
        }
        
        protected def String getFactId(Enumeration literalsEnumeration, Named namedElement)
                throws JobFailedException {
            val elementName = namedElement.getName();
            val literal = literalsEnumeration.literals.filter[name == elementName].findFirst[true];
            val factIds = trace.getLiteralFactIds(literal);
            if (factIds.size() != 1) {
                throw new JobFailedException("Could not find fact for literal " + literal.getName() + ".");
            }
            return factIds.iterator()
                .next();
        }

	
}
