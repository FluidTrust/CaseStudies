package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.json;

import java.io.IOException;
import java.util.stream.Collectors;

import org.palladiosimulator.pcm.core.entity.Entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult.ActionBasedQueryResultDTO;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequenceElement;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.CallingActionSequenceElement;

public class ActionBasedQueryResultDTOJsonSerializer extends JsonSerializer<ActionBasedQueryResultDTO> {

    @Override
    public void serialize(ActionBasedQueryResultDTO value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        serializeCompact(value, gen, serializers);
    }

    protected void serializeCompact(ActionBasedQueryResultDTO value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartObject();
        
        var element = getElementRepresentation(value.getElement());
        var elementContext = value.getElement().getContext().stream().map(ac -> String.format("%s (%s)", ac.getEntityName(), ac.getId())).collect(Collectors.joining(" | "));
        gen.writeStringField("element", element);
        if (!value.getElement().getContext().isEmpty()) {
            gen.writeStringField("context", elementContext);
        }       
        
        gen.writeArrayFieldStart("nodeCharacteristics");
        for (var characteristic : value.getNodeCharacteristics()) {
            gen.writeString(String.format("%s.%s", characteristic.getCharacteristicType().getName(), characteristic.getCharacteristicLiteral().getName()));
        }
        gen.writeEndArray();

        gen.writeObjectFieldStart("dataCharacteristics");
        for (var entry : value.getDataCharacteristics()
            .entrySet()) {
            
            gen.writeArrayFieldStart(entry.getKey());
            for (var characteristic : entry.getValue()) {
                gen.writeString(String.format("%s.%s", characteristic.getCharacteristicType().getName(), characteristic.getCharacteristicLiteral().getName()));
            }
            gen.writeEndArray();
        }
        gen.writeEndObject();
        
        gen.writeEndObject();
    }

    private String getElementRepresentation(ActionSequenceElement<?> element) {
        Entity modelElement = (Entity)element.getElement();
        String modelElementTypeName = modelElement.eClass().getName();
        if (element instanceof CallingActionSequenceElement<?>) {
            if (((CallingActionSequenceElement<?>) element).isCallingPart()) {
                modelElementTypeName = String.format("%s (%s)", modelElementTypeName, "calling");
            } else {
                modelElementTypeName = String.format("%s (%s)", modelElementTypeName, "returning");
            }
        }

        return String.format("%s (%s) of type %s", modelElement.getEntityName(), modelElement.getId(), modelElementTypeName);
    }

    protected void serializeFull(ActionBasedQueryResultDTO value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartObject();
        serializers.defaultSerializeField("element", value.getElement(), gen);
        serializers.defaultSerializeField("nodeCharacteristics", value.getNodeCharacteristics(), gen);
        gen.writeArrayFieldStart("dataCharacteristics");
        for (var entry : value.getDataCharacteristics()
            .entrySet()) {
            gen.writeStartObject();
            serializers.defaultSerializeField(entry.getKey(), entry.getValue(), gen);
            gen.writeEndObject();
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

    @Override
    public Class<ActionBasedQueryResultDTO> handledType() {
        return ActionBasedQueryResultDTO.class;
    }

}
