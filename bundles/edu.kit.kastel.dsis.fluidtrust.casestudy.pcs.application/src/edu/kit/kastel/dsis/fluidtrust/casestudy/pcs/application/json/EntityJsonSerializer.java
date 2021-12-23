package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.json;

import java.io.IOException;

import org.palladiosimulator.pcm.core.entity.Entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class EntityJsonSerializer extends JsonSerializer<Entity> {

    @Override
    public void serialize(Entity value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", value.eClass().getName());
        gen.writeStringField("name", value.getEntityName());
        gen.writeStringField("id", value.getId());
        gen.writeEndObject();
    }

    @Override
    public Class<Entity> handledType() {
        return Entity.class;
    }

}
