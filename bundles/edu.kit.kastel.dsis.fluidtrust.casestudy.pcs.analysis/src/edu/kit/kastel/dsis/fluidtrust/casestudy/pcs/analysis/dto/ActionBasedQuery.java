package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto;

import java.util.function.Predicate;

public interface ActionBasedQuery {

    Iterable<CharacteristicValue> getDataCharacteristicsToTest();

    Iterable<CharacteristicValue> getNodeCharacteristicsToTest();

    Predicate<ActionSequenceElement<?>> getActionSelector();

}
