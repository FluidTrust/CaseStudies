package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.jobs;

import java.io.File;

public class AnalysisResult {

    private File directory;
    private File violationsFile;
    private boolean hasFoundViolations;

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public File getViolationsFile() {
        return violationsFile;
    }

    public void setViolationsFile(File violationsFile) {
        this.violationsFile = violationsFile;
    }

    public boolean hasFoundViolations() {
        return hasFoundViolations;
    }

    public void setHasFoundViolations(boolean hasFoundViolations) {
        this.hasFoundViolations = hasFoundViolations;
    }

}
