package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequence;

public class ActionSequenceJsonSerializer extends JsonSerializer<ActionSequence> {

    @Override
    public void serialize(ActionSequence value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        serializers.defaultSerializeValue(value.toArray(), gen);
    }

    @Override
    public Class<ActionSequence> handledType() {
        return ActionSequence.class;
    }

}
