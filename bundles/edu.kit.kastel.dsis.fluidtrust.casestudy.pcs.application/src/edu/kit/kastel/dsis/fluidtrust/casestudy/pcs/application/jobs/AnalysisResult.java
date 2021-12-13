package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.jobs;

import java.io.File;

public class AnalysisResult {

    private File directory;
    private boolean hasFoundViolation;

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public boolean hasFoundViolation() {
        return hasFoundViolation;
    }

    public void setHasFoundViolation(boolean hasFoundViolation) {
        this.hasFoundViolation = hasFoundViolation;
    }

}
