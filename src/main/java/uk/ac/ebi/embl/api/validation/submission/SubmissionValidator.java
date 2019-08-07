package uk.ac.ebi.embl.api.validation.submission;

import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.helper.MasterSourceFeatureUtils;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;
import uk.ac.ebi.ena.api.SubmissionInterface;
import uk.ac.ebi.ena.model.manifest.GenomeManifest;
import uk.ac.ebi.ena.model.manifest.Manifest;
import uk.ac.ebi.ena.model.reference.Attribute;

import java.util.Optional;

public class SubmissionValidator implements SubmissionInterface {

    SubmissionOptions options;

    public SubmissionValidator() {

    }

    public SubmissionValidator(SubmissionOptions options) {
        this.options = options;
    }

    public void validate() throws ValidationEngineException {
        new SubmissionValidationPlan(options).execute();
    }

    @Override
    public void validate(Manifest manifest) throws ValidationEngineException{

        GenomeManifest gManifest = (GenomeManifest) manifest;
        SubmissionOptions submissionOptions = new SubmissionOptions();
        submissionOptions.context = Optional.of( Context.genome );
        AssemblyInfoEntry assemblyInfo = new AssemblyInfoEntry();
        MasterSourceFeatureUtils sourceUtils = new MasterSourceFeatureUtils();
        SubmissionFiles submissionFiles = new SubmissionFiles();

        assemblyInfo.setName( gManifest.getName() );
        assemblyInfo.setAssemblyType( gManifest.getAssemblyType() );
        assemblyInfo.setPlatform( gManifest.getPlatform() );
        assemblyInfo.setProgram( gManifest.getProgram() );
        assemblyInfo.setMoleculeType( gManifest.getMoleculeType());
        assemblyInfo.setCoverage( gManifest.getCoverage() );
        assemblyInfo.setMinGapLength( gManifest.getMinGapLength());
        assemblyInfo.setTpa(gManifest.isTpa());
        assemblyInfo.setAuthors(gManifest.getAuthors());
        assemblyInfo.setAddress(gManifest.getAddress());

        if (gManifest.getStudy() != null) {
            assemblyInfo.setStudyId(gManifest.getStudy().getBioProjectId());
            if (gManifest.getStudy().getLocusTags()!= null) {
                submissionOptions.locusTagPrefixes = Optional.of(gManifest.getStudy().getLocusTags());
            }
        }
        if (gManifest.getSample() != null) {
            assemblyInfo.setBiosampleId(gManifest.getSample().getBioSampleId());

            SourceFeature sourceFeature = new FeatureFactory().createSourceFeature();
            sourceFeature.addQualifier(Qualifier.DB_XREF_QUALIFIER_NAME, String.valueOf(gManifest.getSample().getTaxId()));
            sourceFeature.setScientificName(gManifest.getSample().getOrganism());
            for (Attribute attribute: gManifest.getSample().getAttributes()) {
                sourceUtils.addSourceQualifier(attribute.getName(), attribute.getValue(), sourceFeature);
            }
            sourceUtils.addExtraSourceQualifiers(sourceFeature, new TaxonHelperImpl(), gManifest.getName());
            submissionOptions.source = Optional.of( sourceFeature );
        }

        gManifest.files().get(GenomeManifest.FileType.FASTA).forEach(file -> submissionFiles.addFile( new SubmissionFile( SubmissionFile.FileType.FASTA, file.getFile() )));
        gManifest.files().get(GenomeManifest.FileType.AGP).forEach(file -> submissionFiles.addFile( new SubmissionFile( SubmissionFile.FileType.AGP,file.getFile() )));
        gManifest.files().get(GenomeManifest.FileType.FLATFILE).forEach(file -> submissionFiles.addFile( new SubmissionFile( SubmissionFile.FileType.FLATFILE, file.getFile() )));
        gManifest.files().get(GenomeManifest.FileType.CHROMOSOME_LIST).forEach(file -> submissionFiles.addFile( new SubmissionFile( SubmissionFile.FileType.CHROMOSOME_LIST, file.getFile() )));
        gManifest.files().get(GenomeManifest.FileType.UNLOCALISED_LIST).forEach(file -> submissionFiles.addFile( new SubmissionFile( SubmissionFile.FileType.UNLOCALISED_LIST, file.getFile() )));

        submissionOptions.assemblyInfoEntry = Optional.of( assemblyInfo );
        submissionOptions.isRemote = true;
        submissionOptions.ignoreErrors = gManifest.isIgnoreErrors();
        submissionOptions.reportDir = Optional.of(gManifest.getReportDir().getAbsolutePath());
        submissionOptions.processDir = Optional.of( gManifest.getProcessDir().getAbsolutePath());
        submissionOptions.submissionFiles = Optional.of( submissionFiles );

        this.options = submissionOptions;
        validate();
    }
}
