package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto;

import java.util.Objects;
import java.util.Stack;

import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;

public abstract class AbstractActionSequenceElement<T extends EObject> implements ActionSequenceElement<T> {

    private final Stack<AssemblyContext> context;
    private final T element;

    public AbstractActionSequenceElement(T element, Stack<AssemblyContext> context) {
        super();
        this.element = element;
        this.context = context;
    }

    public T getElement() {
        return element;
    }

    public Stack<AssemblyContext> getContext() {
        return context;
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, element);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("rawtypes")
        AbstractActionSequenceElement other = (AbstractActionSequenceElement) obj;
        return Objects.equals(context, other.context) && Objects.equals(element, other.element);
    }

}
