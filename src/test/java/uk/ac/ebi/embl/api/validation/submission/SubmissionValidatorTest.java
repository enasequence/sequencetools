/*
 * Copyright 2019-2024 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.validation.submission;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyType;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.ena.webin.cli.validator.file.SubmissionFile;
import uk.ac.ebi.ena.webin.cli.validator.file.SubmissionFiles;
import uk.ac.ebi.ena.webin.cli.validator.manifest.GenomeManifest;
import uk.ac.ebi.ena.webin.cli.validator.manifest.Manifest;
import uk.ac.ebi.ena.webin.cli.validator.manifest.SequenceManifest;
import uk.ac.ebi.ena.webin.cli.validator.reference.Attribute;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;
import uk.ac.ebi.ena.webin.cli.validator.reference.Study;

public class SubmissionValidatorTest {
  @Test
  public void testMapGenomeManifestToSubmissionOptions() throws ValidationEngineException {
    String fasta = "/home/abc/fastaFile.fa";
    String agp = "/home/abc/agpFile.agp";
    String unlocalised = "/home/abc/unlocalisedListFile.txt";
    Manifest manifest = new GenomeManifest();
    manifest.setName("test_manifest");
    SubmissionFiles<GenomeManifest.FileType> submissionFiles = new SubmissionFiles<>();
    submissionFiles.add(
        new SubmissionFile(
            GenomeManifest.FileType.FASTA, new File(fasta), new File(fasta + ".report")));
    submissionFiles.add(
        new SubmissionFile(GenomeManifest.FileType.AGP, new File(agp), new File(agp + ".report")));
    submissionFiles.add(
        new SubmissionFile(
            GenomeManifest.FileType.UNLOCALISED_LIST,
            new File(unlocalised),
            new File(unlocalised + ".report")));
    manifest.setFiles(submissionFiles);

    manifest.setReportFile(new File("/home/reports/other_reports.report"));
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

    ((GenomeManifest) manifest).setMoleculeType("genomic DNA");
    manifest.setAddress("wellcome genome campus");
    manifest.setAuthors("Senthil.V");

    SubmissionOptions options = new SubmissionValidator().mapManifestToSubmissionOptions(manifest);
    AssemblyInfoEntry infoEntry = options.assemblyInfoEntry.get();
    assertEquals("test_manifest", infoEntry.getName());
    assertEquals("PRJ1234", infoEntry.getStudyId());
    assertEquals("wellcome genome campus", infoEntry.getAddress());
    assertEquals("Senthil.V", infoEntry.getAuthors());
    assertEquals("SAM1234", infoEntry.getBiosampleId());
    assertEquals("genomic DNA", infoEntry.getMoleculeType());
    assertEquals("SPLJ", options.locusTagPrefixes.get().get(0));
    assertEquals(Context.genome, options.context.get());
    assertEquals(3, options.submissionFiles.get().getFiles().size());
    assertEquals(new File("/home/reports").getAbsolutePath(), options.reportDir.get());
    assertEquals(new File("/home/process").getAbsolutePath(), options.processDir.get());
    SourceFeature source = options.source.get();
    assertEquals("9606", source.getSingleQualifier(Qualifier.DB_XREF_QUALIFIER_NAME).getValue());
    assertEquals("Homo sapiens", source.getScientificName());
    for (uk.ac.ebi.embl.api.validation.submission.SubmissionFile f :
        options.submissionFiles.get().getFiles()) {
      switch (f.getFileType()) {
        case FASTA:
          assertEquals(
              f.getReportFile().getAbsolutePath(), new File(fasta + ".report").getAbsolutePath());
          break;
        case AGP:
          assertEquals(
              f.getReportFile().getAbsolutePath(), new File(agp + ".report").getAbsolutePath());
          break;
        case UNLOCALISED_LIST:
          assertEquals(
              f.getReportFile().getAbsolutePath(),
              new File(unlocalised + ".report").getAbsolutePath());
          break;
      }
    }
  }

  @Test
  public void testMapGenomeManifestToSubmissionOptions_covidSourceFeature()
      throws ValidationEngineException {
    String fasta = "/home/abc/fastaFile.fa";
    String agp = "/home/abc/agpFile.agp";
    String unlocalised = "/home/abc/unlocalisedListFile.txt";
    Manifest manifest = new GenomeManifest();
    manifest.setName("test_manifest");
    SubmissionFiles<GenomeManifest.FileType> submissionFiles = new SubmissionFiles<>();
    submissionFiles.add(
        new SubmissionFile(
            GenomeManifest.FileType.FASTA, new File(fasta), new File(fasta + ".report")));
    submissionFiles.add(
        new SubmissionFile(GenomeManifest.FileType.AGP, new File(agp), new File(agp + ".report")));
    submissionFiles.add(
        new SubmissionFile(
            GenomeManifest.FileType.UNLOCALISED_LIST,
            new File(unlocalised),
            new File(unlocalised + ".report")));
    manifest.setFiles(submissionFiles);

    manifest.setReportFile(new File("/home/reports/other_reports.report"));
    manifest.setProcessDir(new File("/home/process"));
    Sample sample = new Sample();
    sample.setBioSampleId("SAM1234");
    sample.setOrganism("Homo sapiens");
    sample.setTaxId(2697049);
    List<Attribute> attributes = new ArrayList<>();
    attributes.add(new Attribute("collection date", "2021-03-22"));
    attributes.add(new Attribute("geographic location (latitude)", "58.9099"));
    attributes.add(new Attribute("geographic location (longitude)", "25.6174"));
    attributes.add(new Attribute("geographic location (country and/or sea)", "Estonia"));
    attributes.add(new Attribute("geographic location (region and locality)", "Järva maakond"));
    sample.setAttributes(attributes);
    manifest.setSample(sample);

    Study study = new Study();
    study.setBioProjectId("PRJ1234");
    study.setLocusTags(Collections.singletonList("SPLJ"));
    manifest.setStudy(study);

    ((GenomeManifest) manifest).setMoleculeType("genomic DNA");
    manifest.setAddress("wellcome genome campus");
    manifest.setAuthors("Senthil.V");

    SubmissionOptions options = new SubmissionValidator().mapManifestToSubmissionOptions(manifest);
    AssemblyInfoEntry infoEntry = options.assemblyInfoEntry.get();
    assertEquals("test_manifest", infoEntry.getName());
    assertEquals("PRJ1234", infoEntry.getStudyId());
    assertEquals("wellcome genome campus", infoEntry.getAddress());
    assertEquals("Senthil.V", infoEntry.getAuthors());
    assertEquals("SAM1234", infoEntry.getBiosampleId());
    assertEquals("genomic DNA", infoEntry.getMoleculeType());
    assertEquals("SPLJ", options.locusTagPrefixes.get().get(0));
    assertEquals(Context.genome, options.context.get());
    assertEquals(3, options.submissionFiles.get().getFiles().size());
    assertEquals(new File("/home/reports").getAbsolutePath(), options.reportDir.get());
    assertEquals(new File("/home/process").getAbsolutePath(), options.processDir.get());
    SourceFeature source = options.source.get();
    assertEquals("2697049", source.getSingleQualifier(Qualifier.DB_XREF_QUALIFIER_NAME).getValue());
    assertEquals(
        "2021-03-22",
        source.getSingleQualifier(Qualifier.COLLECTION_DATE_QUALIFIER_NAME).getValue());
    assertEquals(
        "58.9099 N 25.6174 E",
        source.getSingleQualifier(Qualifier.LAT_LON_QUALIFIER_NAME).getValue());
    assertEquals(
        "Estonia:Järva maakond",
        source
            .getSingleQualifier(Qualifier.GEO_LOCATION_QUALIFIER_NAME)
            .getValue()); // non-ascii not fixed at this point

    for (uk.ac.ebi.embl.api.validation.submission.SubmissionFile f :
        options.submissionFiles.get().getFiles()) {
      switch (f.getFileType()) {
        case FASTA:
          assertEquals(
              f.getReportFile().getAbsolutePath(), new File(fasta + ".report").getAbsolutePath());
          break;
        case AGP:
          assertEquals(
              f.getReportFile().getAbsolutePath(), new File(agp + ".report").getAbsolutePath());
          break;
        case UNLOCALISED_LIST:
          assertEquals(
              f.getReportFile().getAbsolutePath(),
              new File(unlocalised + ".report").getAbsolutePath());
          break;
      }
    }
  }

  @Test
  public void testMapSequenceManifestToSubmissionOptions() throws ValidationEngineException {
    String flatFile = "/home/abc/flatFile.fl";
    String tabFile = "/home/abc/tabFile.tsv";

    Manifest manifest = new SequenceManifest();
    manifest.setName("test_manifest");
    manifest.setAddress("wellcome genome campus");
    manifest.setAuthors("Senthil.V");
    manifest.setReportFile(new File("/home/reports/other_reports.report"));
    manifest.setProcessDir(new File("/home/process"));
    manifest.setWebinRestUri("https://wwwdev.ebi.ac.uk/ena/submit/drop-box/");
    manifest.setBiosamplesUri("https://wwwdev.ebi.ac.uk/biosamples/");

    SubmissionFiles<SequenceManifest.FileType> submissionFiles = new SubmissionFiles<>();
    submissionFiles.add(
        new SubmissionFile(
            SequenceManifest.FileType.FLATFILE,
            new File(flatFile),
            new File(flatFile + ".report")));
    submissionFiles.add(
        new SubmissionFile(
            SequenceManifest.FileType.TAB, new File(tabFile), new File(tabFile + ".report")));
    manifest.setFiles(submissionFiles);

    Sample sample = new Sample();
    sample.setBioSampleId("SAM1234");
    sample.setOrganism("Homo sapiens");
    sample.setTaxId(9606);
    manifest.setSample(sample);

    Study study = new Study();
    study.setBioProjectId("PRJ1234");
    study.setLocusTags(Collections.singletonList("SPLJ"));
    manifest.setStudy(study);

    SubmissionOptions options = new SubmissionValidator().mapManifestToSubmissionOptions(manifest);
    AssemblyInfoEntry infoEntry = options.assemblyInfoEntry.get();
    assertEquals("test_manifest", infoEntry.getName());
    assertEquals("PRJ1234", infoEntry.getStudyId());
    assertEquals("wellcome genome campus", infoEntry.getAddress());
    assertEquals("Senthil.V", infoEntry.getAuthors());
    assertEquals("SAM1234", infoEntry.getBiosampleId());
    assertEquals("SPLJ", options.locusTagPrefixes.get().get(0));
    assertEquals(Context.sequence, options.context.get());
    assertEquals(2, options.submissionFiles.get().getFiles().size());
    assertEquals(new File("/home/reports").getAbsolutePath(), options.reportDir.get());
    assertEquals(new File("/home/process").getAbsolutePath(), options.processDir.get());
    SourceFeature source = options.source.get();
    assertEquals("9606", source.getSingleQualifier(Qualifier.DB_XREF_QUALIFIER_NAME).getValue());
    assertEquals("Homo sapiens", source.getScientificName());
    assertEquals(manifest.getWebinRestUri(), options.webinRestUri.get());
    assertEquals(manifest.getBiosamplesUri(), options.biosamplesUri.get());
    for (uk.ac.ebi.embl.api.validation.submission.SubmissionFile f :
        options.submissionFiles.get().getFiles()) {
      switch (f.getFileType()) {
        case FLATFILE:
          assertEquals(
              f.getReportFile().getAbsolutePath(),
              new File(flatFile + ".report").getAbsolutePath());
          break;
        case TSV:
          assertEquals(
              f.getReportFile().getAbsolutePath(), new File(tabFile + ".report").getAbsolutePath());
          break;
      }
    }
  }

  @Test(expected = ValidationEngineException.class)
  public void testInvalidAssemblyNameInManifest() throws ValidationEngineException {
    String fasta = "/home/abc/fastaFile.fa";
    String agp = "/home/abc/agpFile.agp";
    String unlocalised = "/home/abc/unlocalisedListFile.txt";
    Manifest manifest = new GenomeManifest();
    // Invalid anme
    manifest.setName("test_%smanifest");
    SubmissionFiles<GenomeManifest.FileType> submissionFiles = new SubmissionFiles<>();
    submissionFiles.add(
        new SubmissionFile(
            GenomeManifest.FileType.FASTA, new File(fasta), new File(fasta + ".report")));
    submissionFiles.add(
        new SubmissionFile(GenomeManifest.FileType.AGP, new File(agp), new File(agp + ".report")));
    submissionFiles.add(
        new SubmissionFile(
            GenomeManifest.FileType.UNLOCALISED_LIST,
            new File(unlocalised),
            new File(unlocalised + ".report")));
    manifest.setFiles(submissionFiles);

    manifest.setReportFile(new File("/home/reports/other_reports.report"));
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
    try {
      new SubmissionValidator().mapManifestToSubmissionOptions(manifest);
    } catch (ValidationEngineException e) {
      assertTrue(e.getMessage().contains("Invalid assembly name"));
      throw e;
    }
  }

  @Test
  public void testGetAssemblyType() throws ValidationEngineException {
    SubmissionValidator validator = new SubmissionValidator();
    for (AssemblyType assemblyType : AssemblyType.values()) {
      assertNotNull(SubmissionValidator.getAssemblyType(assemblyType.getValue()));
    }

    assertNull(SubmissionValidator.getAssemblyType("Invalid"));
  }
}
