package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto;

import java.util.Stack;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.seff.AbstractAction;

public class SEFFActionSequenceElementImpl<T extends AbstractAction> extends AbstractActionSequenceElement<T> implements SEFFActionSequenceElement<T> {

    public SEFFActionSequenceElementImpl(T element, Stack<AssemblyContext> context) {
        super(element, context);
    }

}
