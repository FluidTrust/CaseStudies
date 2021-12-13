package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.jobs;

import java.io.File;
import java.util.List;
import java.util.Map;

public class AnalysisResult {

    private File directory;
    private List<Map<String, Object>> foundViolations; 

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public boolean hasFoundViolation() {
        return !foundViolations.isEmpty();
    }

    public List<Map<String, Object>> getFoundViolations() {
        return foundViolations;
    }

    public void setFoundViolations(List<Map<String, Object>> foundViolations) {
        this.foundViolations = foundViolations;
    }

}
