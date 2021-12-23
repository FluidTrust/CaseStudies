package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult;

public class ActionBasedQueryResultJsonSerializer extends JsonSerializer<ActionBasedQueryResult> {

    @Override
    public void serialize(ActionBasedQueryResult value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartArray();
        for (var entry : value.getResults().entrySet()) {
            gen.writeStartObject();
            serializers.defaultSerializeField("characteristics", entry.getValue(), gen);
            serializers.defaultSerializeField("actionSequence", entry.getKey(), gen);
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }

    @Override
    public Class<ActionBasedQueryResult> handledType() {
        return ActionBasedQueryResult.class;
    }

}
