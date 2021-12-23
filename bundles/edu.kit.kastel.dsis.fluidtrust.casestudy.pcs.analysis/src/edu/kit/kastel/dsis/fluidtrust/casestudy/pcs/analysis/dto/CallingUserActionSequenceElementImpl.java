package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto;

import java.util.Objects;

import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

public class CallingUserActionSequenceElementImpl extends UserActionSequenceElementImpl<EntryLevelSystemCall>
        implements CallingActionSequenceElement<EntryLevelSystemCall> {

    private final boolean isCallingPart;

    public CallingUserActionSequenceElementImpl(EntryLevelSystemCall element, boolean isCallingPart) {
        super(element);
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
        CallingUserActionSequenceElementImpl other = (CallingUserActionSequenceElementImpl) obj;
        return isCallingPart == other.isCallingPart;
    }

}
