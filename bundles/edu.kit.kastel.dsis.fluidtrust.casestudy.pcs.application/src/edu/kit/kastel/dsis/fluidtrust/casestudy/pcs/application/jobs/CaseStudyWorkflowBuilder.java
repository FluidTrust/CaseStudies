package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.jobs;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import de.uka.ipd.sdq.workflow.jobs.IJob;
import de.uka.ipd.sdq.workflow.jobs.SequentialBlackboardInteractingJob;

public class CaseStudyWorkflowBuilder {

    private File resultFile;
    private File casesFolder;
    private String stackLimit;

    private CaseStudyWorkflowBuilder() {
        // intentionally left blank
    }

    public static CaseStudyWorkflowBuilder builder() {
        return new CaseStudyWorkflowBuilder();
    }

    public CaseStudyWorkflowBuilder resultFile(File resultFile) {
        this.resultFile = resultFile;
        return this;
    }

    public CaseStudyWorkflowBuilder casesFolder(File casesFolder) {
        this.casesFolder = casesFolder;
        return this;
    }

    public IJob build() throws IOException {
        // build overall job sequence
        var resultBlackboard = new AnalysisResultBlackboard();
        var job = new SequentialBlackboardInteractingJob<AnalysisResultBlackboard>();
        job.setBlackboard(resultBlackboard);

        // add case execution jobs
        var caseDTOs = findCasesInDirectory();
        for (CaseDTO caseDTO : caseDTOs) {
            job.add(new RunCaseStudyForCaseJob(caseDTO.getUsageModel(), caseDTO.getAllocationModel(), stackLimit));
        }

        // add analysis result to CSV job
        job.add(new SerializeResultsToCSVJob(resultFile));
        
        return job;
    }

    protected static class CaseDTO {
        private File directory;
        private File usageModel;
        private File allocationModel;

        public CaseDTO(Path directoryPath) {
            this.directory = directoryPath.toFile();
        }

        public File getDirectory() {
            return directory;
        }

        public void setDirectory(File directory) {
            this.directory = directory;
        }

        public File getUsageModel() {
            return usageModel;
        }

        public void setUsageModel(File usageModel) {
            this.usageModel = usageModel;
        }

        public File getAllocationModel() {
            return allocationModel;
        }

        public void setAllocationModel(File allocationModel) {
            this.allocationModel = allocationModel;
        }

    }

    protected Collection<CaseDTO> findCasesInDirectory() throws IOException {
        final Map<Path, CaseDTO> foundCases = new HashMap<>();

        Files.walkFileTree(casesFolder.toPath(), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName()
                    .toString()
                    .endsWith(".usagemodel")) {
                    CaseDTO caseDTO = getCaseDTO(file.getParent());
                    caseDTO.setUsageModel(file.toFile());
                }
                if (file.getFileName()
                    .toString()
                    .endsWith(".allocation")) {
                    CaseDTO caseDTO = getCaseDTO(file.getParent());
                    caseDTO.setAllocationModel(file.toFile());
                }
                return super.visitFile(file, attrs);
            }

            private CaseDTO getCaseDTO(Path directoryPath) {
                return foundCases.computeIfAbsent(directoryPath, CaseDTO::new);
            }
        });

        foundCases.entrySet()
            .removeIf(dto -> dto.getValue()
                .getAllocationModel() == null
                    || dto.getValue()
                        .getUsageModel() == null);

        return foundCases.values()
            .stream()
            .filter(dto -> dto.getAllocationModel() != null && dto.getUsageModel() != null)
            .sorted((d1, d2) -> d1.getDirectory()
                .compareTo(d2.getDirectory()))
            .collect(Collectors.toList());
    }

    public CaseStudyWorkflowBuilder stackLimit(String stackLimit) {
        this.stackLimit = stackLimit;
        return this;
    }

}
