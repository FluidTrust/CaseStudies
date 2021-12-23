package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.CharacteristicValue;

public class CharacteristicValueJsonSerializer extends JsonSerializer<CharacteristicValue> {

    @Override
    public void serialize(CharacteristicValue value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", value.getCharacteristicType().getName());
        gen.writeStringField("value", value.getCharacteristicLiteral().getName());
        gen.writeEndObject();
    }

    @Override
    public Class<CharacteristicValue> handledType() {
        return CharacteristicValue.class;
    }

}
