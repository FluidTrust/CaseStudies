package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto;

import java.util.Stack;

import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;

public interface ActionSequenceElement<T extends EObject> {
    T getElement();

    Stack<AssemblyContext> getContext();

}
