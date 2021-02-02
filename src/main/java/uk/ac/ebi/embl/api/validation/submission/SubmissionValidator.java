package uk.ac.ebi.embl.api.validation.submission;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.AssemblyInfoNameCheck;
import uk.ac.ebi.embl.api.validation.helper.MasterSourceFeatureUtils;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;

import uk.ac.ebi.embl.api.validation.report.DefaultSubmissionReporter;
import uk.ac.ebi.embl.flatfile.reader.FeatureReader;
import uk.ac.ebi.ena.webin.cli.validator.api.ValidationResponse;
import uk.ac.ebi.ena.webin.cli.validator.api.Validator;
import uk.ac.ebi.ena.webin.cli.validator.manifest.GenomeManifest;
import uk.ac.ebi.ena.webin.cli.validator.manifest.Manifest;
import uk.ac.ebi.ena.webin.cli.validator.manifest.SequenceManifest;
import uk.ac.ebi.ena.webin.cli.validator.manifest.TranscriptomeManifest;
import uk.ac.ebi.ena.webin.cli.validator.reference.Attribute;

import java.io.File;
import java.util.*;

public class SubmissionValidator implements Validator<Manifest,ValidationResponse> {

    private SubmissionOptions options;

    public SubmissionValidator() {

    }

    public SubmissionValidator(SubmissionOptions options) {
        this.options = options;
    }

    public void validate() throws ValidationEngineException {

        ValidationPlanResult planResult;
        try {
            planResult = new SubmissionValidationPlan(options).execute();
        } catch (ValidationEngineException e) {
            writeErrorText(e.getMessage());
            throw e;
        }

       if(planResult.hasError()) {
           if(options.isWebinCLI) {
               writeErrorText(planResult.getValidationMessage().getOriginText());
              throw new ValidationEngineException(planResult.getValidationMessage().getOriginText(), ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
           } else {
               StringBuilder sb = new StringBuilder();
               for (ValidationMessage<Origin> error : planResult.getMessages(Severity.ERROR)) {
                   sb.append(error.getMessage());
                   sb.append("\n");
               }
               throw new ValidationEngineException(StringUtils.chomp(sb.toString()), ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
           }
       }
    }

    private void writeErrorText(String errorText) {
        if (options.reportFile.isPresent()) {
            new DefaultSubmissionReporter(new HashSet<>(Arrays.asList(Severity.ERROR, Severity.WARNING, Severity.FIX, Severity.INFO)))
                    .writeToFile(options.reportFile.get(), Severity.ERROR, errorText);
        }
    }
    /**
     * Manifest to SubmissionOptions mapping.This is only for webin-cli.
     * @param manifest
     * @return ValidationResponse
     */
    @Override
    public ValidationResponse validate(Manifest manifest) {
        ValidationResponse response = new ValidationResponse(ValidationResponse.status.VALIDATION_SUCCESS);
        try {
            options = mapManifestToSubmissionOptions(manifest);
            FeatureReader.isWebinCli = true;
            validate();
        } catch (ValidationEngineException vee) {
            switch (vee.getErrorType()) {
                case VALIDATION_ERROR:
                    response.setStatus(ValidationResponse.status.VALIDATION_ERROR);
                    break;
                default:
                    new DefaultSubmissionReporter(new HashSet<>(Collections.singletonList(Severity.ERROR))).
                            writeToFile(manifest.getReportFile(), Severity.ERROR, vee.getMessage());
                    throw new RuntimeException(vee);
            }
        }
        return response;
    }

    SubmissionOptions mapManifestToSubmissionOptions(Manifest manifest) throws ValidationEngineException {
        if(manifest == null)
            throw new ValidationEngineException("Manifest can not be null.", ValidationEngineException.ReportErrorType.SYSTEM_ERROR);
        if(manifest.getReportFile() == null ) {
            throw new ValidationEngineException("Report file is missing.", ValidationEngineException.ReportErrorType.SYSTEM_ERROR);
        }
        DefaultSubmissionReporter reporter = new DefaultSubmissionReporter(new HashSet<>(Collections.singletonList(Severity.ERROR)));
        if(manifest.getProcessDir() == null ) {
            reporter.writeToFile(manifest.getReportFile(), Severity.ERROR, "Process directory is missing.");
            throw new ValidationEngineException("Process directory is missing.", ValidationEngineException.ReportErrorType.SYSTEM_ERROR);
        }
        SubmissionOptions options = new SubmissionOptions();
        //Set all common options
        AssemblyInfoEntry assemblyInfo = new AssemblyInfoEntry();
        MasterSourceFeatureUtils sourceUtils = new MasterSourceFeatureUtils();

        assemblyInfo.setName(manifest.getName());
        assemblyInfo.setAuthors(manifest.getAuthors());
        assemblyInfo.setAddress(manifest.getAddress());

        if (manifest.getStudy() != null) {
            assemblyInfo.setStudyId(manifest.getStudy().getBioProjectId());
            if (manifest.getStudy().getLocusTags() != null) {
                options.locusTagPrefixes = Optional.of(manifest.getStudy().getLocusTags());
            }
        }
        if (manifest.getSample() != null) {
            assemblyInfo.setBiosampleId(manifest.getSample().getBioSampleId());

            SourceFeature sourceFeature = new FeatureFactory().createSourceFeature();
            sourceFeature.addQualifier(Qualifier.DB_XREF_QUALIFIER_NAME, String.valueOf(manifest.getSample().getTaxId()));
            sourceFeature.setScientificName(manifest.getSample().getOrganism());
            for (Attribute attribute : manifest.getSample().getAttributes()) {
                sourceUtils.addSourceQualifier(attribute.getName(), attribute.getValue(), sourceFeature);
            }
            sourceUtils.addExtraSourceQualifiers(sourceFeature, new TaxonHelperImpl(), manifest.getName());
            options.source = Optional.of(sourceFeature);
        }

        options.isWebinCLI = true;
        options.ignoreErrors = manifest.isIgnoreErrors();
        options.reportDir = Optional.of(new File(manifest.getReportFile().getAbsolutePath()).getParent());
        options.reportFile = Optional.of(manifest.getReportFile());
        options.processDir = Optional.of(manifest.getProcessDir().getAbsolutePath());

        //Set options specific to context
        if(manifest instanceof GenomeManifest) {
            if(!new AssemblyInfoNameCheck().isValidName(manifest.getName())) {
                reporter.writeToFile(manifest.getReportFile(), Severity.ERROR, "Invalid assembly name:"+manifest.getName());
                throw new ValidationEngineException("Invalid assembly name:"+manifest.getName(), ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
            }
            options.context = Optional.of(Context.genome);
            options.submissionFiles = Optional.of(setGenomeOptions((GenomeManifest)manifest, assemblyInfo));
        } else if(manifest instanceof TranscriptomeManifest) {
            options.context = Optional.of(Context.transcriptome);
            options.submissionFiles = Optional.of(setTranscriptomeOptions((TranscriptomeManifest)manifest, assemblyInfo));
        } else {
            options.context = Optional.of(Context.sequence);
            options.submissionFiles = Optional.of(setSequenceOptions((SequenceManifest) manifest));
        }
        options.assemblyInfoEntry = Optional.of(assemblyInfo);
        return options;
    }

    private SubmissionFiles setGenomeOptions(GenomeManifest manifest, AssemblyInfoEntry assemblyInfo) {
        assemblyInfo.setAssemblyType(manifest.getAssemblyType());
        assemblyInfo.setPlatform(manifest.getPlatform());
        assemblyInfo.setProgram(manifest.getProgram());
        assemblyInfo.setMoleculeType(manifest.getMoleculeType());
        assemblyInfo.setCoverage(manifest.getCoverage());
        assemblyInfo.setMinGapLength(manifest.getMinGapLength());
        assemblyInfo.setTpa(manifest.isTpa());

        SubmissionFiles submissionFiles = new SubmissionFiles();
        manifest.files().get(GenomeManifest.FileType.FASTA).forEach(file -> submissionFiles.addFile(
                new SubmissionFile(SubmissionFile.FileType.FASTA, file.getFile(), new File(file.getFile()+ SequenceEntryUtils.FIXED_FILE_SUFFIX ), file.getReportFile())));
        manifest.files().get(GenomeManifest.FileType.AGP).forEach(file -> submissionFiles.addFile(
                new SubmissionFile(SubmissionFile.FileType.AGP, file.getFile(),new File(file.getFile()+SequenceEntryUtils.FIXED_FILE_SUFFIX), file.getReportFile())));
        manifest.files().get(GenomeManifest.FileType.FLATFILE).forEach(file -> submissionFiles.addFile(
                new SubmissionFile(SubmissionFile.FileType.FLATFILE, file.getFile(),new File(file.getFile()+SequenceEntryUtils.FIXED_FILE_SUFFIX), file.getReportFile())));
        manifest.files().get(GenomeManifest.FileType.CHROMOSOME_LIST).forEach(file -> submissionFiles.addFile(
                new SubmissionFile(SubmissionFile.FileType.CHROMOSOME_LIST, file.getFile(),new File(file.getFile()+SequenceEntryUtils.FIXED_FILE_SUFFIX), file.getReportFile())));
        manifest.files().get(GenomeManifest.FileType.UNLOCALISED_LIST).forEach(file -> submissionFiles.addFile(
                new SubmissionFile(SubmissionFile.FileType.UNLOCALISED_LIST, file.getFile(),new File(file.getFile()+SequenceEntryUtils.FIXED_FILE_SUFFIX), file.getReportFile())));
        return submissionFiles;
    }

    private SubmissionFiles setSequenceOptions(SequenceManifest manifest) {
        SubmissionFiles submissionFiles = new SubmissionFiles();
        manifest.files().get(SequenceManifest.FileType.FLATFILE).forEach(file -> submissionFiles.addFile(
                new SubmissionFile(SubmissionFile.FileType.FLATFILE, file.getFile(),new File(file.getFile()+SequenceEntryUtils.FIXED_FILE_SUFFIX), file.getReportFile())));
        manifest.files().get(SequenceManifest.FileType.TAB).forEach(file -> submissionFiles.addFile(
                new SubmissionFile(SubmissionFile.FileType.TSV, file.getFile(),new File(file.getFile()+SequenceEntryUtils.FIXED_FILE_SUFFIX), file.getReportFile())));
        return submissionFiles;
    }

    private SubmissionFiles setTranscriptomeOptions(TranscriptomeManifest manifest, AssemblyInfoEntry assemblyInfo) {
        assemblyInfo.setName(manifest.getName());
        assemblyInfo.setPlatform(manifest.getPlatform());
        assemblyInfo.setProgram(manifest.getProgram());
        assemblyInfo.setTpa(manifest.isTpa());

        SubmissionFiles submissionFiles = new SubmissionFiles();
        manifest.files().get(TranscriptomeManifest.FileType.FASTA).forEach(file -> submissionFiles.addFile(
                new SubmissionFile(SubmissionFile.FileType.FASTA, file.getFile(),new File(file.getFile()+SequenceEntryUtils.FIXED_FILE_SUFFIX), file.getReportFile())));
        manifest.files().get(TranscriptomeManifest.FileType.FLATFILE).forEach(file -> submissionFiles.addFile(
                new SubmissionFile(SubmissionFile.FileType.FLATFILE, file.getFile(),new File(file.getFile()+SequenceEntryUtils.FIXED_FILE_SUFFIX), file.getReportFile())));
        return submissionFiles;
    }
}
