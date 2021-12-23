package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto;

import org.eclipse.emf.ecore.EObject;

public interface CallingActionSequenceElement<T extends EObject> extends ActionSequenceElement<T> {

    boolean isCallingPart();
    default boolean isReturningPart() {
        return !isCallingPart();
    }
    
}
