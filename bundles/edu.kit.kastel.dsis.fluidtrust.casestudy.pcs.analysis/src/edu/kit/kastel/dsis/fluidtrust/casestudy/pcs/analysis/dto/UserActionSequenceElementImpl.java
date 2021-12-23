package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto;

import java.util.Stack;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

public class UserActionSequenceElementImpl<T extends AbstractUserAction> extends AbstractActionSequenceElement<T> implements UserActionSequenceElement<T> {

    public UserActionSequenceElementImpl(T element) {
        super(element, new Stack<>());
    }

}
