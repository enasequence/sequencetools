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
package uk.ac.ebi.embl.api.entry.genomeassembly;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;

public class ChromosomeEntryTest {

  @Test
  public void testSetAndGetQualifiers_MacronuclearLocation() {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setChromosomeLocation("macronuclear");

    List<Qualifier> qualifiers = entry.setAndGetQualifiers(false);

    assertEquals(1, qualifiers.size());
    assertEquals(Qualifier.MACRONUCLEAR_QUALIFIER_NAME, qualifiers.get(0).getName());
    assertNull(qualifiers.get(0).getValue()); // macronuclear has no value
  }

  @Test
  public void testSetAndGetQualifiers_MacronuclearLocation_CaseInsensitive() {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setChromosomeLocation("Macronuclear"); // Mixed case

    List<Qualifier> qualifiers = entry.setAndGetQualifiers(false);

    assertEquals(1, qualifiers.size());
    assertEquals(Qualifier.MACRONUCLEAR_QUALIFIER_NAME, qualifiers.get(0).getName());
    assertNull(qualifiers.get(0).getValue());
  }

  @Test
  public void testSetAndGetQualifiers_MacronuclearLocation_UpperCase() {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setChromosomeLocation("MACRONUCLEAR"); // Upper case

    List<Qualifier> qualifiers = entry.setAndGetQualifiers(false);

    assertEquals(1, qualifiers.size());
    assertEquals(Qualifier.MACRONUCLEAR_QUALIFIER_NAME, qualifiers.get(0).getName());
  }

  @Test
  public void testSetAndGetQualifiers_MitochondrionLocation() {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setChromosomeLocation("mitochondrion");

    List<Qualifier> qualifiers = entry.setAndGetQualifiers(false);

    assertEquals(1, qualifiers.size());
    assertEquals(Qualifier.ORGANELLE_QUALIFIER_NAME, qualifiers.get(0).getName());
    assertEquals("mitochondrion", qualifiers.get(0).getValue());
  }

  @Test
  public void testSetAndGetQualifiers_NucleomorphLocation() {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setChromosomeLocation("nucleomorph");

    List<Qualifier> qualifiers = entry.setAndGetQualifiers(false);

    assertEquals(1, qualifiers.size());
    assertEquals(Qualifier.ORGANELLE_QUALIFIER_NAME, qualifiers.get(0).getName());
    assertEquals("nucleomorph", qualifiers.get(0).getValue());
  }

  @Test
  public void testSetAndGetQualifiers_ChloroplastLocation() {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setChromosomeLocation("chloroplast");

    List<Qualifier> qualifiers = entry.setAndGetQualifiers(false);

    assertEquals(1, qualifiers.size());
    assertEquals(Qualifier.ORGANELLE_QUALIFIER_NAME, qualifiers.get(0).getName());
    assertEquals("plastid:chloroplast", qualifiers.get(0).getValue());
  }

  @Test
  public void testSetAndGetQualifiers_HydrogenosomeLocation() {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setChromosomeLocation("hydrogenosome");

    List<Qualifier> qualifiers = entry.setAndGetQualifiers(false);

    assertEquals(1, qualifiers.size());
    assertEquals(Qualifier.ORGANELLE_QUALIFIER_NAME, qualifiers.get(0).getName());
    assertEquals("hydrogenosome", qualifiers.get(0).getValue());
  }

  @Test
  public void testSetAndGetQualifiers_ChromosomeWithName() {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setChromosomeName("1");
    entry.setChromosomeType("chromosome");

    List<Qualifier> qualifiers = entry.setAndGetQualifiers(false);

    assertEquals(1, qualifiers.size());
    assertEquals(Qualifier.CHROMOSOME_QUALIFIER_NAME, qualifiers.get(0).getName());
    assertEquals("1", qualifiers.get(0).getValue());
  }

  @Test
  public void testSetAndGetQualifiers_PlasmidWithName() {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setChromosomeName("pBR322");
    entry.setChromosomeType("plasmid");

    List<Qualifier> qualifiers = entry.setAndGetQualifiers(false);

    assertEquals(1, qualifiers.size());
    assertEquals(Qualifier.PLASMID_QUALIFIER_NAME, qualifiers.get(0).getName());
    assertEquals("pBR322", qualifiers.get(0).getValue());
  }

  @Test
  public void testSetAndGetQualifiers_SegmentMultipartite() {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setChromosomeName("II");
    entry.setChromosomeType("multipartite");

    List<Qualifier> qualifiers = entry.setAndGetQualifiers(false);

    assertEquals(1, qualifiers.size());
    assertEquals(Qualifier.SEGMENT_QUALIFIER_NAME, qualifiers.get(0).getName());
    assertEquals("II", qualifiers.get(0).getValue());
  }

  @Test
  public void testSetAndGetQualifiers_SegmentSegmented() {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setChromosomeName("III");
    entry.setChromosomeType("segmented");

    List<Qualifier> qualifiers = entry.setAndGetQualifiers(false);

    assertEquals(1, qualifiers.size());
    assertEquals(Qualifier.SEGMENT_QUALIFIER_NAME, qualifiers.get(0).getName());
    assertEquals("III", qualifiers.get(0).getValue());
  }

  @Test
  public void testSetAndGetQualifiers_Monopartite() {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setChromosomeType("monopartite");

    List<Qualifier> qualifiers = entry.setAndGetQualifiers(false);

    assertEquals(1, qualifiers.size());
    assertEquals(Qualifier.NOTE_QUALIFIER_NAME, qualifiers.get(0).getName());
    assertEquals("monopartite", qualifiers.get(0).getValue());
  }

  @Test
  public void testSetAndGetQualifiers_VirusFlag_PhageLocation() {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setChromosomeLocation("Phage");

    // With virus flag = true, should not create organelle qualifier
    List<Qualifier> qualifiers = entry.setAndGetQualifiers(true);

    assertEquals(0, qualifiers.size());
  }

  @Test
  public void testSetAndGetQualifiers_VirusFlag_MitochondrionLocation() {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setChromosomeLocation("mitochondrion");

    // With virus flag = true, should not create organelle qualifier
    List<Qualifier> qualifiers = entry.setAndGetQualifiers(true);

    assertEquals(0, qualifiers.size());
  }

  @Test
  public void testSetAndGetQualifiers_MacronuclearWithVirusFlag() {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setChromosomeLocation("macronuclear");

    // Macronuclear should still work with virus flag
    List<Qualifier> qualifiers = entry.setAndGetQualifiers(true);

    assertEquals(1, qualifiers.size());
    assertEquals(Qualifier.MACRONUCLEAR_QUALIFIER_NAME, qualifiers.get(0).getName());
  }

  @Test
  public void testSetAndGetQualifiers_NoLocation_NoName() {
    ChromosomeEntry entry = new ChromosomeEntry();

    List<Qualifier> qualifiers = entry.setAndGetQualifiers(false);

    assertEquals(0, qualifiers.size());
  }

  @Test
  public void testSetAndGetQualifiers_CalledTwice_ReturnsSameList() {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setChromosomeLocation("macronuclear");

    List<Qualifier> qualifiers1 = entry.setAndGetQualifiers(false);
    List<Qualifier> qualifiers2 = entry.setAndGetQualifiers(false);

    // Should return the same cached list
    assertSame(qualifiers1, qualifiers2);
    assertEquals(1, qualifiers1.size());
  }

  @Test
  public void testSetAndGetQualifiers_InvalidLocation() {
    ChromosomeEntry entry = new ChromosomeEntry();
    entry.setChromosomeLocation("invalid_location");

    List<Qualifier> qualifiers = entry.setAndGetQualifiers(false);

    // Invalid location should not create any qualifiers
    assertEquals(0, qualifiers.size());
  }
}
