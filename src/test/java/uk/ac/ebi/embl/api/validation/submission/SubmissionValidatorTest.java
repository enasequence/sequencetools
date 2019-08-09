package uk.ac.ebi.embl.api.validation.submission;

import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.ena.webin.cli.validator.file.SubmissionFile;
import uk.ac.ebi.ena.webin.cli.validator.file.SubmissionFiles;
import uk.ac.ebi.ena.webin.cli.validator.manifest.GenomeManifest;
import uk.ac.ebi.ena.webin.cli.validator.manifest.Manifest;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;
import uk.ac.ebi.ena.webin.cli.validator.reference.Study;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class SubmissionValidatorTest {
    @Test
    public void testMapGenomeManifestToSubmissionOptions() throws ValidationEngineException {
        Manifest manifest = new GenomeManifest();
        manifest.setName("test_manifest");
        SubmissionFiles<GenomeManifest.FileType> submissionFiles = new SubmissionFiles<>();
        submissionFiles.add(new SubmissionFile( GenomeManifest.FileType.FASTA, new File("fastaFile")));
        submissionFiles.add(new SubmissionFile( GenomeManifest.FileType.AGP, new File("agpFile")));
        submissionFiles.add(new SubmissionFile( GenomeManifest.FileType.UNLOCALISED_LIST, new File("unlocalisedListFile")));
        manifest.setFiles(submissionFiles);

        manifest.setReportDir(new File("/home/reports"));
        manifest.setProcessDir(new File("/home/process"));
        Sample sample = new Sample();
        sample.setBioSampleId("SAM1234");
        sample.setOrganism("Homo sapiens");
        sample.setTaxId(9606);
        manifest.setSample(sample);

        Study study = new Study();
        study.setBioProjectId("PRJ1234");
        study.setLocusTags(Collections.singletonList("SPLJ"));
        manifest.setStudy(study);

        ((GenomeManifest) manifest).setAssemblyType("SEQUENCE_ASSEMBLY");
        ((GenomeManifest) manifest).setMoleculeType("genomic DNA");
        manifest.setAddress("wellcome genome campus");
        manifest.setAuthors("Senthil.V");

        SubmissionOptions options = new SubmissionValidator().mapManifestToSubmissionOptions(manifest);
        AssemblyInfoEntry infoEntry = options.assemblyInfoEntry.get();
        assertEquals("test_manifest", infoEntry.getName());
        assertEquals("PRJ1234",infoEntry.getStudyId());
        assertEquals("wellcome genome campus", infoEntry.getAddress());
        assertEquals("Senthil.V", infoEntry.getAuthors());
        assertEquals("SEQUENCE_ASSEMBLY", infoEntry.getAssemblyType());
        assertEquals("SAM1234", infoEntry.getBiosampleId());
        assertEquals("genomic DNA", infoEntry.getMoleculeType());
        assertEquals("SPLJ", options.locusTagPrefixes.get().get(0));
        assertEquals(Context.genome, options.context.get());
        assertEquals(3, options.submissionFiles.get().getFiles().size());
        assertEquals(new File("/home/reports").getAbsolutePath(), options.reportDir.get());
        assertEquals(new File("/home/process").getAbsolutePath(), options.processDir.get());
        SourceFeature source = options.source.get();
        assertEquals("9606",source.getSingleQualifier(Qualifier.DB_XREF_QUALIFIER_NAME).getValue());
        assertEquals("Homo sapiens", source.getScientificName());
    }


}
