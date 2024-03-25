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
package uk.ac.ebi.embl.flatfile.reader.genbank;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.*;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.flatfile.reader.FeatureReader;
import uk.ac.ebi.embl.flatfile.reader.ReaderOptions;

public class GenbankFeatureReaderTest extends GenbankReaderTest {

  public void testReadOnlySourceFeature() throws IOException {
    initLineReader(
        "     misc_RNA        <1..>538\n"
            + "                     /note=\"contains internal transcribed spacer 1, 5.8S\n"
            + "                     ribosomal RNA, internal transcribed spacer 2, and 28S\n"
            + "                     ribosomal RNA\"\n"
            + "     source          1..538\n"
            + "                     /organism=\"Alternaria solani\"\n"
            + "                     /mol_type=\"genomic DNA\"\n"
            + "                     /host=\"Cynodon dactylon\"\n"
            + "                     /specimen_voucher=\"KUABIASCD\"\n"
            + "                     /db_xref=\"taxon:48100\"\n"
            + "                     /country=\"India\"\n"
            + "                     /collection_date=\"2009\"\n"
            + "                     /collected_by=\"M.M. Vasanthakumari\"\n"
            + "                     /PCR_primers=\"fwd_name: ITS1, fwd_seq: tccgtaggtaacctgcgg,\n"
            + "                     rev_name: ITS4, rev_seq: tcctccgcttattgatatgc\"");
    ReaderOptions rO = new ReaderOptions();
    rO.setParseSourceOnly(true);
    lineReader.setReaderOptions(rO);

    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertNotNull(entry.getFeatures());
    assertEquals(0, result.count(Severity.ERROR));
    assertEquals(1, entry.getFeatures().size());
    assertEquals("misc_RNA", entry.getFeatures().get(0).getName());

    lineReader.readLine();
    result = (new FeatureReader(lineReader)).read(entry);
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(2, entry.getFeatures().size());
    assertEquals("misc_RNA", entry.getFeatures().get(0).getName());
    assertEquals(0, entry.getFeatures().get(0).getQualifiers().size());
    assertEquals("source", entry.getFeatures().get(1).getName());
    assertEquals(7, entry.getFeatures().get(1).getQualifiers().size());
    assertEquals("genomic DNA", entry.getSequence().getMoleculeType());
  }

  public void testRead_LocalSingleBase() throws IOException {
    initLineReader("     source          156 \n" + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertFalse(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalBase);
    assertEquals(Long.valueOf(156), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_LocalSingleBaseComplement() throws IOException {
    initLineReader(
        "     source          complement(156)\n" + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertTrue(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalBase);
    assertEquals(Long.valueOf(156), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_LocalSingleBaseLeftPartial() throws IOException {
    initLineReader("     source          <156 \n" + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertFalse(locations.isComplement());
    assertTrue(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalBase);
    assertEquals(Long.valueOf(156), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_LocalSingleBaseRightPartial() throws IOException {
    initLineReader(
        "     source          complement(<156) \n" + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertTrue(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertTrue(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalBase);
    assertEquals(Long.valueOf(156), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_RemoteSingleBaseWithoutVersion() throws IOException {
    initLineReader(
        "     source          A00001:156 \n" + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertFalse(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof RemoteBase);
    assertEquals(Long.valueOf(156), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertEquals("A00001", ((RemoteBase) location).getAccession());
    assertNull(((RemoteBase) location).getVersion());
    assertFalse(location.isComplement());
  }

  public void testRead_RemoteSingleBaseWithVersion() throws IOException {
    initLineReader(
        "     source          A00001.13:156 \n" + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertFalse(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof RemoteBase);
    assertEquals(Long.valueOf(156), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertEquals("A00001", ((RemoteBase) location).getAccession());
    assertEquals(Integer.valueOf(13), ((RemoteBase) location).getVersion());
    assertFalse(location.isComplement());
  }

  public void testRead_LocalRange() throws IOException {
    initLineReader("     source          4..156 \n" + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertFalse(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(4), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_LocalRangeComplement() throws IOException {
    initLineReader(
        "     source          complement(4..156) \n" + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertTrue(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(4), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_LocalRangeLeftPartial() throws IOException {
    initLineReader("     source          <4..156 \n" + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertFalse(locations.isComplement());
    assertTrue(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(4), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_LocalRangeRightPartial() throws IOException {
    initLineReader("     source          4..>156 \n" + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertFalse(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertTrue(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(4), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_LocalRangeRightPartial2() throws IOException {
    initLineReader(
        "     source          complement(<4..156) \n" + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertTrue(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertTrue(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(4), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_LocalRangeLeftPartial2() throws IOException {
    initLineReader(
        "     source          complement(4..>156) \n" + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertTrue(locations.isComplement());
    assertTrue(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(4), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_RemoteRangeWithoutVersion() throws IOException {
    initLineReader(
        "     source          complement(A0001:4..156) \n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertTrue(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof RemoteRange);
    assertEquals("A0001", ((RemoteRange) location).getAccession());
    assertNull(((RemoteRange) location).getVersion());
    assertEquals(Long.valueOf(4), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_RemoteRangeWithVersion() throws IOException {
    initLineReader(
        "     source          complement(A0001.1:4..156) \n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertTrue(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof RemoteRange);
    assertEquals("A0001", ((RemoteRange) location).getAccession());
    assertEquals(Integer.valueOf(1), ((RemoteRange) location).getVersion());
    assertEquals(Long.valueOf(4), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_LocalBetween() throws IOException {
    initLineReader("     source          4^156 \n" + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertFalse(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalBetween);
    assertEquals(Long.valueOf(4), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_LocalBetweenComplement() throws IOException {
    initLineReader(
        "     source          complement(4^156) \n" + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertTrue(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalBetween);
    assertEquals(Long.valueOf(4), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_LocalBetweenLeftPartial() throws IOException {
    initLineReader("     source          <4^156 \n" + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertFalse(locations.isComplement());
    assertTrue(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalBetween);
    assertEquals(Long.valueOf(4), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_LocalBetweenRightPartial() throws IOException {
    initLineReader("     source          4^>156 \n" + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertFalse(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertTrue(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalBetween);
    assertEquals(Long.valueOf(4), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_LocalBetweenRightPartial2() throws IOException {
    initLineReader(
        "     source          complement(<4^156) \n" + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertTrue(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertTrue(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalBetween);
    assertEquals(Long.valueOf(4), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_LocalBetweenLeftPartial2() throws IOException {
    initLineReader(
        "     source          complement(155^>156) \n" + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertTrue(locations.isComplement());
    assertTrue(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalBetween);
    assertEquals(Long.valueOf(155), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_RemoteBetweenWithoutVersion() throws IOException {
    initLineReader(
        "     source          complement(A0001:155^156) \n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertTrue(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof RemoteBetween);
    assertEquals("A0001", ((RemoteBetween) location).getAccession());
    assertNull(((RemoteBetween) location).getVersion());
    assertEquals(Long.valueOf(155), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_RemoteBetweenWithVersion() throws IOException {
    initLineReader(
        "     source          complement(A0001.1:155^156) \n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertTrue(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(1, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof RemoteBetween);
    assertEquals("A0001", ((RemoteBetween) location).getAccession());
    assertEquals(Integer.valueOf(1), ((RemoteBetween) location).getVersion());
    assertEquals(Long.valueOf(155), location.getBeginPosition());
    assertEquals(Long.valueOf(156), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_Join() throws IOException {
    initLineReader(
        "     source          join(1,2..34,complement(34..45),\n"
            + "                     A00001.5:34..45)\n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertFalse(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(4, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalBase);
    assertEquals(Long.valueOf(1), location.getBeginPosition());
    assertEquals(Long.valueOf(1), location.getEndPosition());
    assertFalse(location.isComplement());
    location = locations.getLocations().get(1);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(2), location.getBeginPosition());
    assertEquals(Long.valueOf(34), location.getEndPosition());
    assertFalse(location.isComplement());
    location = locations.getLocations().get(2);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(34), location.getBeginPosition());
    assertEquals(Long.valueOf(45), location.getEndPosition());
    assertTrue(location.isComplement());
    location = locations.getLocations().get(3);
    assertTrue(location instanceof RemoteRange);
    assertEquals("A00001", ((RemoteRange) location).getAccession());
    assertEquals(Integer.valueOf(5), ((RemoteRange) location).getVersion());
    assertEquals(Long.valueOf(34), location.getBeginPosition());
    assertEquals(Long.valueOf(45), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_JoinComplement() throws IOException {
    initLineReader(
        "     source          complement(join(1,2..34,complement(34..45),\n"
            + "                     A00001.5:34..45))\n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertTrue(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(4, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalBase);
    assertEquals(Long.valueOf(1), location.getBeginPosition());
    assertEquals(Long.valueOf(1), location.getEndPosition());
    assertFalse(location.isComplement());
    location = locations.getLocations().get(1);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(2), location.getBeginPosition());
    assertEquals(Long.valueOf(34), location.getEndPosition());
    assertFalse(location.isComplement());
    location = locations.getLocations().get(2);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(34), location.getBeginPosition());
    assertEquals(Long.valueOf(45), location.getEndPosition());
    assertTrue(location.isComplement());
    location = locations.getLocations().get(3);
    assertTrue(location instanceof RemoteRange);
    assertEquals("A00001", ((RemoteRange) location).getAccession());
    assertEquals(Integer.valueOf(5), ((RemoteRange) location).getVersion());
    assertEquals(Long.valueOf(34), location.getBeginPosition());
    assertEquals(Long.valueOf(45), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_JoinLeftPartial() throws IOException {
    initLineReader(
        "     source          join(<1,2..34,complement(34..45),\n"
            + "                     A00001.5:34..45)\n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertFalse(locations.isComplement());
    assertTrue(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(4, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalBase);
    assertEquals(Long.valueOf(1), location.getBeginPosition());
    assertEquals(Long.valueOf(1), location.getEndPosition());
    assertFalse(location.isComplement());
    location = locations.getLocations().get(1);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(2), location.getBeginPosition());
    assertEquals(Long.valueOf(34), location.getEndPosition());
    assertFalse(location.isComplement());
    location = locations.getLocations().get(2);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(34), location.getBeginPosition());
    assertEquals(Long.valueOf(45), location.getEndPosition());
    assertTrue(location.isComplement());
    location = locations.getLocations().get(3);
    assertTrue(location instanceof RemoteRange);
    assertEquals("A00001", ((RemoteRange) location).getAccession());
    assertEquals(Integer.valueOf(5), ((RemoteRange) location).getVersion());
    assertEquals(Long.valueOf(34), location.getBeginPosition());
    assertEquals(Long.valueOf(45), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_JoinRightPartial() throws IOException {
    initLineReader(
        "     source          join(1,2..34,complement(34..45),\n"
            + "                     A00001.5:34..>45)\n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Join<?>);
    assertFalse(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertTrue(locations.isThreePrime());
    assertEquals(4, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalBase);
    assertEquals(Long.valueOf(1), location.getBeginPosition());
    assertEquals(Long.valueOf(1), location.getEndPosition());
    assertFalse(location.isComplement());
    location = locations.getLocations().get(1);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(2), location.getBeginPosition());
    assertEquals(Long.valueOf(34), location.getEndPosition());
    assertFalse(location.isComplement());
    location = locations.getLocations().get(2);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(34), location.getBeginPosition());
    assertEquals(Long.valueOf(45), location.getEndPosition());
    assertTrue(location.isComplement());
    location = locations.getLocations().get(3);
    assertTrue(location instanceof RemoteRange);
    assertEquals("A00001", ((RemoteRange) location).getAccession());
    assertEquals(Integer.valueOf(5), ((RemoteRange) location).getVersion());
    assertEquals(Long.valueOf(34), location.getBeginPosition());
    assertEquals(Long.valueOf(45), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_Order() throws IOException {
    initLineReader(
        "     source          order(1,2..34,complement(34..45),\n"
            + "                     A00001.5:34..45)\n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Order<?>);
    assertFalse(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(4, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalBase);
    assertEquals(Long.valueOf(1), location.getBeginPosition());
    assertEquals(Long.valueOf(1), location.getEndPosition());
    assertFalse(location.isComplement());
    location = locations.getLocations().get(1);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(2), location.getBeginPosition());
    assertEquals(Long.valueOf(34), location.getEndPosition());
    assertFalse(location.isComplement());
    location = locations.getLocations().get(2);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(34), location.getBeginPosition());
    assertEquals(Long.valueOf(45), location.getEndPosition());
    assertTrue(location.isComplement());
    location = locations.getLocations().get(3);
    assertTrue(location instanceof RemoteRange);
    assertEquals("A00001", ((RemoteRange) location).getAccession());
    assertEquals(Integer.valueOf(5), ((RemoteRange) location).getVersion());
    assertEquals(Long.valueOf(34), location.getBeginPosition());
    assertEquals(Long.valueOf(45), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_OrderComplement() throws IOException {
    initLineReader(
        "     source          complement(order(1,2..34,complement(34..45),\n"
            + "                     A00001.5:34..45))\n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Order<?>);
    assertTrue(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(4, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalBase);
    assertEquals(Long.valueOf(1), location.getBeginPosition());
    assertEquals(Long.valueOf(1), location.getEndPosition());
    assertFalse(location.isComplement());
    location = locations.getLocations().get(1);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(2), location.getBeginPosition());
    assertEquals(Long.valueOf(34), location.getEndPosition());
    assertFalse(location.isComplement());
    location = locations.getLocations().get(2);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(34), location.getBeginPosition());
    assertEquals(Long.valueOf(45), location.getEndPosition());
    assertTrue(location.isComplement());
    location = locations.getLocations().get(3);
    assertTrue(location instanceof RemoteRange);
    assertEquals("A00001", ((RemoteRange) location).getAccession());
    assertEquals(Integer.valueOf(5), ((RemoteRange) location).getVersion());
    assertEquals(Long.valueOf(34), location.getBeginPosition());
    assertEquals(Long.valueOf(45), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_OrderLeftPartial() throws IOException {
    initLineReader(
        "     source          order(<1,2..34,complement(34..45),\n"
            + "                     A00001.5:34..45)\n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Order<?>);
    assertFalse(locations.isComplement());
    assertTrue(locations.isFivePrime());
    assertFalse(locations.isThreePrime());
    assertEquals(4, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalBase);
    assertEquals(Long.valueOf(1), location.getBeginPosition());
    assertEquals(Long.valueOf(1), location.getEndPosition());
    assertFalse(location.isComplement());
    location = locations.getLocations().get(1);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(2), location.getBeginPosition());
    assertEquals(Long.valueOf(34), location.getEndPosition());
    assertFalse(location.isComplement());
    location = locations.getLocations().get(2);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(34), location.getBeginPosition());
    assertEquals(Long.valueOf(45), location.getEndPosition());
    assertTrue(location.isComplement());
    location = locations.getLocations().get(3);
    assertTrue(location instanceof RemoteRange);
    assertEquals("A00001", ((RemoteRange) location).getAccession());
    assertEquals(Integer.valueOf(5), ((RemoteRange) location).getVersion());
    assertEquals(Long.valueOf(34), location.getBeginPosition());
    assertEquals(Long.valueOf(45), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_OrderRightPartial() throws IOException {
    initLineReader(
        "     source          order(1,2..34,complement(34..45),\n"
            + "                     A00001.5:34..>45)\n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    CompoundLocation<Location> locations = feature.getLocations();
    assertTrue(locations instanceof Order<?>);
    assertFalse(locations.isComplement());
    assertFalse(locations.isFivePrime());
    assertTrue(locations.isThreePrime());
    assertEquals(4, locations.getLocations().size());
    Location location = locations.getLocations().get(0);
    assertTrue(location instanceof LocalBase);
    assertEquals(Long.valueOf(1), location.getBeginPosition());
    assertEquals(Long.valueOf(1), location.getEndPosition());
    assertFalse(location.isComplement());
    location = locations.getLocations().get(1);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(2), location.getBeginPosition());
    assertEquals(Long.valueOf(34), location.getEndPosition());
    assertFalse(location.isComplement());
    location = locations.getLocations().get(2);
    assertTrue(location instanceof LocalRange);
    assertEquals(Long.valueOf(34), location.getBeginPosition());
    assertEquals(Long.valueOf(45), location.getEndPosition());
    assertTrue(location.isComplement());
    location = locations.getLocations().get(3);
    assertTrue(location instanceof RemoteRange);
    assertEquals("A00001", ((RemoteRange) location).getAccession());
    assertEquals(Integer.valueOf(5), ((RemoteRange) location).getVersion());
    assertEquals(Long.valueOf(34), location.getBeginPosition());
    assertEquals(Long.valueOf(45), location.getEndPosition());
    assertFalse(location.isComplement());
  }

  public void testRead_QualifierNoValue() throws IOException {
    initLineReader(
        "     source          156\n"
            + "                     /note\n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    List<Qualifier> qualifiers = feature.getQualifiers();
    assertEquals(1, qualifiers.size());
    Qualifier qualifier = qualifiers.get(0);
    assertEquals("note", qualifier.getName());
    assertNull(qualifier.getValue());
  }

  public void testRead_QualifierUnquotedValue() throws IOException {
    initLineReader(
        "     source          156\n"
            + "                     /note=\"value value\" \n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    List<Qualifier> qualifiers = feature.getQualifiers();
    assertEquals(1, qualifiers.size());
    Qualifier qualifier = qualifiers.get(0);
    assertEquals("note", qualifier.getName());
    assertEquals("value value", qualifier.getValue());
  }

  public void testRead_QualifierQuotedValue() throws IOException {
    initLineReader(
        "     source          156\n"
            + "                     /note=\"value value\"\n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    List<Qualifier> qualifiers = feature.getQualifiers();
    assertEquals(1, qualifiers.size());
    Qualifier qualifier = qualifiers.get(0);
    assertEquals("note", qualifier.getName());
    assertEquals("value value", qualifier.getValue());
  }

  public void testRead_QualifierLongQuotedValue() throws IOException {
    initLineReader(
        "     source          156\n"
            + "                     /note=\"value1 value2\n"
            + "                     value3 value4\n"
            + "                     value5 value6\n"
            + "                     value7 value8\" \n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    List<Qualifier> qualifiers = feature.getQualifiers();
    assertEquals(1, qualifiers.size());
    Qualifier qualifier = qualifiers.get(0);
    assertEquals("note", qualifier.getName());
    assertEquals("value1 value2 value3 value4 value5 value6 value7 value8", qualifier.getValue());
  }

  public void testRead_QualifierLongUnquotedValue() throws IOException {
    initLineReader(
        "     source          156\n"
            + "                     /note=\"value1 value2\n"
            + "                     value3 value4\n"
            + "                     value5 value6\n"
            + "                     value7 value8\"\n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    List<Qualifier> qualifiers = feature.getQualifiers();
    assertEquals(1, qualifiers.size());
    Qualifier qualifier = qualifiers.get(0);
    assertEquals("note", qualifier.getName());
    assertEquals("value1 value2 value3 value4 value5 value6 value7 value8", qualifier.getValue());
  }

  public void testRead_MultipleQualifierValue() throws IOException {
    initLineReader(
        "     source          156\n"
            + "                     /note=\"value value\"\n"
            + "                     /note=\"value value\"\n"
            + "                     /note=\"value value\"\n"
            + "                     /note=\"value value\"\n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    List<Qualifier> qualifiers = feature.getQualifiers();
    assertEquals(4, qualifiers.size());
    Qualifier qualifier = qualifiers.get(0);
    assertEquals("note", qualifier.getName());
    assertEquals("value value", qualifier.getValue());
    qualifier = qualifiers.get(1);
    assertEquals("note", qualifier.getName());
    assertEquals("value value", qualifier.getValue());
    qualifier = qualifiers.get(2);
    assertEquals("note", qualifier.getName());
    assertEquals("value value", qualifier.getValue());
    qualifier = qualifiers.get(3);
    assertEquals("note", qualifier.getName());
    assertEquals("value value", qualifier.getValue());
  }

  public void testRead_QualifierReplace() throws IOException {
    initLineReader(
        "     source          156\n"
            + "                     /replace=\"value1 value2\n"
            + "                     value3 value4\n"
            + "                     value5 value6\n"
            + "                     value7 value8\"\n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    List<Qualifier> qualifiers = feature.getQualifiers();
    assertEquals(1, qualifiers.size());
    Qualifier qualifier = qualifiers.get(0);
    assertEquals("replace", qualifier.getName());
    assertEquals("value1 value2value3 value4value5 value6value7 value8", qualifier.getValue());
  }

  public void testRead_QualifierRptUnitSeq() throws IOException {
    initLineReader(
        "     source          156\n"
            + "                     /rpt_unit_seq=\"value1 value2\n"
            + "                     value3 value4\n"
            + "                     value5 value6\n"
            + "                     value7 value8\" \n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    List<Qualifier> qualifiers = feature.getQualifiers();
    assertEquals(1, qualifiers.size());
    Qualifier qualifier = qualifiers.get(0);
    assertEquals("rpt_unit_seq", qualifier.getName());
    assertEquals("value1 value2value3 value4value5 value6value7 value8", qualifier.getValue());
  }

  public void testRead_QualifierPcrPrimers() throws IOException {
    initLineReader(
        "     source          156\n"
            + "                     /PCR_primers=\"value1 value2\n"
            + "                     value3 value4\n"
            + "                     value5 value6\n"
            + "                     value7 value8\"\n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    List<Qualifier> qualifiers = feature.getQualifiers();
    assertEquals(1, qualifiers.size());
    Qualifier qualifier = qualifiers.get(0);
    assertEquals("PCR_primers", qualifier.getName());
    assertEquals("value1 value2value3 value4value5 value6value7 value8", qualifier.getValue());
  }

  public void testRead_QualifierTranslation() throws IOException {
    initLineReader(
        "     source          156\n"
            + "                     /translation=\"value1 value2\n"
            + "                     value3 value4\n"
            + "                     value5 value6\n"
            + "                     value7 value8\"\n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    List<Qualifier> qualifiers = feature.getQualifiers();
    assertEquals(1, qualifiers.size());
    Qualifier qualifier = qualifiers.get(0);
    assertEquals("translation", qualifier.getName());
    assertEquals("value1 value2value3 value4value5 value6value7 value8", qualifier.getValue());
  }

  public void testRead_QualifierLineEndWithDash() throws IOException {
    initLineReader(
        "     source          156\n"
            + "                     /note=\"Predicted tRNA(5-methylaminomethyl-2-\n"
            + "                     thiouridylate) methyltransferase, contains the PP-loop\n"
            + "                     ATPase domain\"\n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    Feature feature = entry.getFeatures().get(0);
    assertEquals("source", feature.getName());
    List<Qualifier> qualifiers = feature.getQualifiers();
    assertEquals(1, qualifiers.size());
    Qualifier qualifier = qualifiers.get(0);
    assertEquals("note", qualifier.getName());
    assertEquals(
        "Predicted tRNA(5-methylaminomethyl-2-thiouridylate) methyltransferase, contains the PP-loop ATPase domain",
        qualifier.getValue());
  }

  public void testRead_Origin() throws IOException {
    initLineReader(
        "     source          156..\n"
            + "                     160\n"
            + "                     /note=\"value value\n"
            + "                     value value\"\n"
            + "                     /note=\"value value\"\n"
            + "                     /note=\"value value\"\n"
            + "                     /note=\"value value\" \n"
            + "                     /mol_type=\"mRNA\"");
    ValidationResult result = (new FeatureReader(lineReader)).read(entry);
    Collection<ValidationMessage<Origin>> messages = result.getMessages();
    for (ValidationMessage<Origin> message : messages) {
      System.out.println(message.getMessage());
    }
    assertEquals(0, result.count(Severity.ERROR));
    assertNotNull(entry.getFeatures());
    assertEquals(1, entry.getFeatures().size());
    FlatFileOrigin origin = (FlatFileOrigin) entry.getFeatures().get(0).getOrigin();
    assertEquals(1, origin.getFirstLineNumber());
    assertEquals(2, origin.getLastLineNumber());
    origin = (FlatFileOrigin) entry.getFeatures().get(0).getLocations().getOrigin();
    assertEquals(1, origin.getFirstLineNumber());
    assertEquals(2, origin.getLastLineNumber());
    List<Qualifier> qualifiers = entry.getFeatures().get(0).getQualifiers();
    assertEquals(4, qualifiers.size());
    origin = (FlatFileOrigin) qualifiers.get(0).getOrigin();
    assertEquals(3, origin.getFirstLineNumber());
    assertEquals(4, origin.getLastLineNumber());
    origin = (FlatFileOrigin) qualifiers.get(1).getOrigin();
    assertEquals(5, origin.getFirstLineNumber());
    assertEquals(5, origin.getLastLineNumber());
    origin = (FlatFileOrigin) qualifiers.get(2).getOrigin();
    assertEquals(6, origin.getFirstLineNumber());
    assertEquals(6, origin.getLastLineNumber());
    origin = (FlatFileOrigin) qualifiers.get(3).getOrigin();
    assertEquals(7, origin.getFirstLineNumber());
    assertEquals(7, origin.getLastLineNumber());
  }
}
