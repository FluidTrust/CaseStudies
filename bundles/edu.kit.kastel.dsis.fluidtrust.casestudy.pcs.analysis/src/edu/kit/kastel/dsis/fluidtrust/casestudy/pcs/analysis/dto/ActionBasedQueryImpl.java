package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto;

import java.util.function.Predicate;

public class ActionBasedQueryImpl implements ActionBasedQuery {

    private final Predicate<ActionSequenceElement<?>> actionSelector;
    private final Iterable<CharacteristicValue> dataCharacteristics;
    private final Iterable<CharacteristicValue> nodecharacteristics;

    public ActionBasedQueryImpl(Predicate<ActionSequenceElement<?>> actionSelector,
            Iterable<CharacteristicValue> dataCharacteristics, Iterable<CharacteristicValue> nodecharacteristics) {
        super();
        this.actionSelector = actionSelector;
        this.dataCharacteristics = dataCharacteristics;
        this.nodecharacteristics = nodecharacteristics;
    }

    @Override
    public Predicate<ActionSequenceElement<?>> getActionSelector() {
        return actionSelector;
    }

    @Override
    public Iterable<CharacteristicValue> getDataCharacteristicsToTest() {
        return dataCharacteristics;
    }

    @Override
    public Iterable<CharacteristicValue> getNodeCharacteristicsToTest() {
        return nodecharacteristics;
    }

}
