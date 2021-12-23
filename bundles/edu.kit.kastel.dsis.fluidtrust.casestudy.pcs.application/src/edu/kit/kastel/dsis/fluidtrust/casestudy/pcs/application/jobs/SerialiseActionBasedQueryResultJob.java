package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.jobs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.dataflow.confidentiality.transformation.workflow.blackboards.KeyValueMDSDBlackboard;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.json.ActionBasedQueryResultDTOJsonSerializer;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.json.ActionBasedQueryResultJsonSerializer;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.json.ActionSequenceJsonSerializer;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.json.CharacteristicValueJsonSerializer;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.json.EntityJsonSerializer;

public class SerialiseActionBasedQueryResultJob extends AbstractBlackboardInteractingJob<KeyValueMDSDBlackboard> {

    private final String resultKey;
    private final File resultFile;

    public SerialiseActionBasedQueryResultJob(String resultKey, File resultFile) {
        this.resultKey = resultKey;
        this.resultFile = resultFile;
    }

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        monitor.beginTask("Serialize JSON", 1);
        var allCharacteristicsResult = getBlackboard().get(resultKey)
            .filter(ActionBasedQueryResult.class::isInstance)
            .map(ActionBasedQueryResult.class::cast)
            .orElseThrow(() -> new JobFailedException("Could not load query result."));
        var jsonFactory = createJsonFactory();
        serializeCharacteristics(allCharacteristicsResult, jsonFactory);
        monitor.done();
    }

    private void serializeCharacteristics(ActionBasedQueryResult allCharacteristicsResult, JsonFactory jsonFactory)
            throws JobFailedException {
        try (var baos = new FileOutputStream(resultFile)) {
            var generator = jsonFactory.createGenerator(baos, JsonEncoding.UTF8);
            generator.writeObject(allCharacteristicsResult);
        } catch (IOException e) {
            throw new JobFailedException("Error while serializing JSON", e);
        }
    }

    private JsonFactory createJsonFactory() {
        var jsonFactory = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
            .registerModule(new SimpleModule().addSerializer(new EntityJsonSerializer())
                .addSerializer(new CharacteristicValueJsonSerializer())
                .addSerializer(new ActionSequenceJsonSerializer())
                .addSerializer(new ActionBasedQueryResultJsonSerializer())
                .addSerializer(new ActionBasedQueryResultDTOJsonSerializer()))
            .getFactory();
        return jsonFactory;
    }

    @Override
    public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
        // nothing to do here
    }

    @Override
    public String getName() {
        return "Serialize all characteristics";
    }

}
