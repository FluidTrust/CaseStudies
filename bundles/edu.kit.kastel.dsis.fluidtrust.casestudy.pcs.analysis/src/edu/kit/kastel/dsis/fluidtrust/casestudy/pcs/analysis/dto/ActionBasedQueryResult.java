package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ActionBasedQueryResult {

    public static class ActionBasedQueryResultDTO {
        private final ActionSequenceElement<?> element;
        private final Map<String,Collection<CharacteristicValue>> dataCharacteristics;
        private final Collection<CharacteristicValue> nodeCharacteristics;

        public ActionBasedQueryResultDTO(ActionSequenceElement<?> element,
                Map<String,Collection<CharacteristicValue>> dataCharacteristics, Collection<CharacteristicValue> nodeCharacteristics) {
            super();
            this.element = element;
            this.dataCharacteristics = dataCharacteristics;
            this.nodeCharacteristics = nodeCharacteristics;
        }

        public ActionSequenceElement<?> getElement() {
            return element;
        }

        public Map<String,Collection<CharacteristicValue>> getDataCharacteristics() {
            return dataCharacteristics;
        }

        public Collection<CharacteristicValue> getNodeCharacteristics() {
            return nodeCharacteristics;
        }

    }

    private final Map<ActionSequence, Collection<ActionBasedQueryResultDTO>> results = new HashMap<>();
    
    public void addResult(ActionSequence sequence, ActionBasedQueryResultDTO result) {
        var resultContainer = results.computeIfAbsent(sequence, s -> new ArrayList<ActionBasedQueryResultDTO>());
        resultContainer.add(result);
    }
    
    public void addResult(ActionSequence sequence, Collection<ActionBasedQueryResultDTO> result) {
        result.forEach(r -> addResult(sequence, r));
    }
    
    public Map<ActionSequence, Collection<ActionBasedQueryResultDTO>> getResults() {
        return Collections.unmodifiableMap(results);
    }

}
