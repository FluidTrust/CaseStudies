package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto;

import java.util.Objects;
import java.util.Stack;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.seff.ExternalCallAction;

public class CallingSEFFActionSequenceElementImpl extends SEFFActionSequenceElementImpl<ExternalCallAction>
        implements CallingActionSequenceElement<ExternalCallAction> {

    private final boolean isCallingPart;

    public CallingSEFFActionSequenceElementImpl(ExternalCallAction element, boolean isCallingPart,
            Stack<AssemblyContext> context) {
        super(element, context);
        this.isCallingPart = isCallingPart;
    }

    @Override
    public boolean isCallingPart() {
        return isCallingPart;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(isCallingPart);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CallingSEFFActionSequenceElementImpl other = (CallingSEFFActionSequenceElementImpl) obj;
        return isCallingPart == other.isCallingPart;
    }

}
