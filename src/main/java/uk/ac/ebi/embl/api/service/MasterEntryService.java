package uk.ac.ebi.embl.api.service;

import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.embl.api.contant.AnalysisType;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.XRef;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtilsImpl;
import uk.ac.ebi.embl.api.validation.helper.EntryUtils;
import uk.ac.ebi.embl.api.validation.helper.ReferenceUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlan;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;

public class MasterEntryService {

    MasterEntryService() {
    }

    /**
     * Creates a master entry from Webin CLI manifest or from submitted XMLs.
     */
    public Entry createMasterEntry(SubmissionOptions options, ValidationResult validationResult) throws ValidationEngineException {
        Entry masterEntry;

        if (options.isWebinCLI) {
            masterEntry = getMasterEntryFromWebinCli(options);
        } else {
            EraproDAOUtils utils = new EraproDAOUtilsImpl(
                options.eraproConnection.get(),
                options.webinUsername.get(),
                options.webinPassword.get(),
                options.biosamplesWebinUsername.get(),
                options.biosamplesWebinPassword.get());
            masterEntry = getMasterEntryFromSubmittedXml(options, utils);
        }

        if (Context.transcriptome == options.context.get() && masterEntry != null) {
            masterEntry.getSequence().setMoleculeType("transcribed RNA");
        }

        // Validate and fix the master entry.
        EmblEntryValidationPlan validationPlan = new EmblEntryValidationPlan(options.getEntryValidationPlanProperty());
        validationResult.append(validationPlan.execute(masterEntry));
        return masterEntry;
    }

    Entry getMasterEntryFromSubmittedXml(SubmissionOptions options, EraproDAOUtils eraproDAOUtils) throws ValidationEngineException {

        try {
            Entry masterEntry = eraproDAOUtils.createMasterEntry(options.analysisId.get(), getAnalysisType(options));
            if (masterEntry != null) {

                if ( StringUtils.isNotBlank(masterEntry.getComment().getText())) {
                    masterEntry.setComment(new Text(formatComment(masterEntry.getComment().getText())));
                }

                if (Context.transcriptome == options.context.get() ) {
                    String ccLine = eraproDAOUtils.getCommentsToTranscriptomeMaster(options.analysisId.get());
                    if (StringUtils.isNotBlank(masterEntry.getComment().getText())) {
                        ccLine += "\n" + masterEntry.getComment().getText();
                    }
                    masterEntry.setComment(new Text(ccLine));
                }
            }
            return masterEntry;
        } catch (SQLException | IOException ex) {
            throw new ValidationEngineException(ex);
        }
    }

    private String formatComment(String commentText) throws IOException {
        StringWriter strWriter = new StringWriter();
        FlatFileWriter.writeBlock(strWriter, "", "", commentText,
                WrapChar.WRAP_CHAR_SPACE, WrapType.EMBL_WRAP, EmblPadding.CC_PADDING.length(), false, null, null);
        String comment = strWriter.toString().trim();
        if (comment.length() - 1 == comment.lastIndexOf("\n")) {
            comment = comment.substring(0, comment.length() - 1);
        }
        return comment;
    }
    private Entry getMasterEntryFromWebinCli(SubmissionOptions options) throws ValidationEngineException {
        if (!options.assemblyInfoEntry.isPresent()) {
            throw new ValidationEngineException("SubmissionOption assemblyInfoEntry must be given to generate master entry");
        }

        if (!options.source.isPresent()) {
            throw new ValidationEngineException("SubmissionOption source must be given to generate master entry");
        }

        Entry masterEntry = new Entry();
        AnalysisType analysisType = getAnalysisType(options);
        if (analysisType == null) {
            return masterEntry;
        }
        AssemblyInfoEntry infoEntry = options.assemblyInfoEntry.get();
        SourceFeature source = options.source.get();

        masterEntry.setIdLineSequenceLength(1);
        SequenceFactory sequenceFactory = new SequenceFactory();
        masterEntry.setDataClass(Entry.SET_DATACLASS);
        Sequence sequence = sequenceFactory.createSequence();
        masterEntry.setSequence(sequence);
        masterEntry.setIdLineSequenceLength(1);
        if (analysisType == AnalysisType.TRANSCRIPTOME_ASSEMBLY) {
            masterEntry.getSequence().setMoleculeType("transcribed RNA");
        } else {
            masterEntry.getSequence().setMoleculeType(infoEntry.getMoleculeType() == null ? "genomic DNA" : infoEntry.getMoleculeType());
        }
        if (masterEntry.getSequence().getTopology() == null)
            masterEntry.getSequence().setTopology(Sequence.Topology.LINEAR);
        source.setMasterLocation();
        masterEntry.addProjectAccession(new Text(infoEntry.getProjectId()));
        masterEntry.addXRef(new XRef("BioSample", infoEntry.getBiosampleId()));
        if (infoEntry.isTpa()) {
            EntryUtils.setKeyWords(masterEntry);
        }
        masterEntry.addFeature(source);
        if (options.context.get() == Context.genome)
            masterEntry.setDescription(new Text(SequenceEntryUtils.generateMasterEntryDescription(source, AnalysisType.SEQUENCE_ASSEMBLY, infoEntry.isTpa())));

        if (StringUtils.isNotBlank(options.assemblyInfoEntry.get().getAddress())
                && StringUtils.isNotBlank(options.assemblyInfoEntry.get().getAuthors())) {
            masterEntry.removeReferences();
            masterEntry.addReference(new ReferenceUtils().getSubmitterReferenceFromManifest(options.assemblyInfoEntry.get().getAuthors(),
                    options.assemblyInfoEntry.get().getAddress(), options.assemblyInfoEntry.get().getDate(), options.assemblyInfoEntry.get().getSubmissionAccountId()));
        }

        return masterEntry;
    }

    private AnalysisType getAnalysisType(SubmissionOptions options) {
        switch (options.context.get()) {
            case transcriptome:
                return AnalysisType.TRANSCRIPTOME_ASSEMBLY;
            case genome:
                return AnalysisType.SEQUENCE_ASSEMBLY;
            default:
                return null;
        }
    }
}
