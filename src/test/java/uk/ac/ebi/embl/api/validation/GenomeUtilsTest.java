/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.validation;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;

public class GenomeUtilsTest {

  private void addComponent(Set<String> components, String component) {
    components.add(component.toUpperCase());
  }

  @Test
  public void testInvalidAssemblyLevel() {
    Map<String, AssemblySequenceInfo> sequenceInfo = new LinkedHashMap();
    Set<String> agpPlacedComponents = new HashSet<>();

    sequenceInfo.put("contig1", new AssemblySequenceInfo(100, -1, "contig1"));
    assertThrows(
        "Unexpected assembly level",
        ValidationEngineException.class,
        () -> GenomeUtils.calculateGenomeSize(sequenceInfo, agpPlacedComponents));
  }

  @Test
  public void testContigsOnly() throws ValidationEngineException {
    Map<String, AssemblySequenceInfo> sequenceInfo = new LinkedHashMap();
    Set<String> agpPlacedComponents = new HashSet<>();

    sequenceInfo.put("contig1", new AssemblySequenceInfo(100, 0, "contig1"));
    sequenceInfo.put("contig2", new AssemblySequenceInfo(150, 0, "contig2"));
    sequenceInfo.put("contig3", new AssemblySequenceInfo(200, 0, "contig3"));
    assertTrue(
        GenomeUtils.calculateGenomeSize(sequenceInfo, agpPlacedComponents) == 100 + 150 + 200);
  }

  @Test
  public void testChromosomesOnly() throws ValidationEngineException {
    Map<String, AssemblySequenceInfo> sequenceInfo = new LinkedHashMap();
    Set<String> agpPlacedComponents = new HashSet<>();

    sequenceInfo.put("chr1", new AssemblySequenceInfo(100, 2, "chr1"));
    assertTrue(GenomeUtils.calculateGenomeSize(sequenceInfo, agpPlacedComponents) == 100);
  }

  @Test
  public void testContigsScaffolds_PlacedContigs() throws ValidationEngineException {
    Map<String, AssemblySequenceInfo> sequenceInfo = new LinkedHashMap();
    Set<String> agpPlacedComponents = new HashSet<>();

    sequenceInfo.put("contig1", new AssemblySequenceInfo(100, 0, "contig1"));
    sequenceInfo.put("contig2", new AssemblySequenceInfo(150, 0, "contig2"));
    sequenceInfo.put("contig3", new AssemblySequenceInfo(200, 0, "contig3"));

    // AGP
    // scaffold1 contig1
    // scaffold1 gap
    // scaffold1 contig2
    // scaffold2 contig3
    sequenceInfo.put("scaffold1", new AssemblySequenceInfo(250, 1, "scaffold1"));
    sequenceInfo.put("scaffold2", new AssemblySequenceInfo(200, 1, "scaffold2"));

    addComponent(agpPlacedComponents, "contig1");
    addComponent(agpPlacedComponents, "contig2");
    addComponent(agpPlacedComponents, "contig3");

    assertTrue(GenomeUtils.calculateGenomeSize(sequenceInfo, agpPlacedComponents) == 250 + 200);
  }

  @Test
  public void testContigsScaffolds_PlacedContigsAndScaffolds() throws ValidationEngineException {
    Map<String, AssemblySequenceInfo> sequenceInfo = new LinkedHashMap();
    Set<String> agpPlacedComponents = new HashSet<>();

    sequenceInfo.put("contig1", new AssemblySequenceInfo(100, 0, "contig1"));
    sequenceInfo.put("contig2", new AssemblySequenceInfo(150, 0, "contig2"));
    sequenceInfo.put("contig3", new AssemblySequenceInfo(200, 0, "contig3"));
    sequenceInfo.put("contig4", new AssemblySequenceInfo(300, 0, "contig4"));

    // AGP
    // scaffold1 contig1
    // scaffold1 gap
    // scaffold1 contig2
    // scaffold2 contig3
    // scaffold3 scaffold2
    sequenceInfo.put("scaffold1", new AssemblySequenceInfo(250, 1, "scaffold1"));
    sequenceInfo.put("scaffold2", new AssemblySequenceInfo(200, 1, "scaffold2"));
    sequenceInfo.put("scaffold3", new AssemblySequenceInfo(200, 1, "scaffold3"));

    addComponent(agpPlacedComponents, "contig1");
    addComponent(agpPlacedComponents, "contig2");
    addComponent(agpPlacedComponents, "contig3");
    addComponent(agpPlacedComponents, "scaffold2");

    assertTrue(
        GenomeUtils.calculateGenomeSize(sequenceInfo, agpPlacedComponents) == 250 + 200 + 300);
  }

  @Test
  public void testContigsScaffoldsChromosomes() throws ValidationEngineException {
    Map<String, AssemblySequenceInfo> sequenceInfo = new LinkedHashMap();
    Set<String> agpPlacedComponents = new HashSet<>();

    sequenceInfo.put("contig1", new AssemblySequenceInfo(100, 0, "contig1")); // placed
    sequenceInfo.put("contig2", new AssemblySequenceInfo(150, 0, "contig2")); // placed
    sequenceInfo.put("contig3", new AssemblySequenceInfo(200, 0, "contig3")); // placed
    sequenceInfo.put("contig4", new AssemblySequenceInfo(300, 0, "contig4")); // placed

    // AGP
    // scaffold1 contig1
    // scaffold1 gap
    // scaffold1 contig2
    // scaffold2 contig3
    // chr1 contig4
    // chr1 scaffold2
    sequenceInfo.put("scaffold1", new AssemblySequenceInfo(250, 1, "scaffold1")); // unplaced
    sequenceInfo.put("scaffold2", new AssemblySequenceInfo(200, 1, "scaffold2")); // placed

    sequenceInfo.put("chr1", new AssemblySequenceInfo(250, 2, "chr1"));

    addComponent(agpPlacedComponents, "contig1");
    addComponent(agpPlacedComponents, "contig2");
    addComponent(agpPlacedComponents, "contig3");
    addComponent(agpPlacedComponents, "contig4");
    addComponent(agpPlacedComponents, "scaffold2");

    assertTrue(GenomeUtils.calculateGenomeSize(sequenceInfo, agpPlacedComponents) == 250 + 250);
  }

  @Test
  public void testContigsChromosomes() throws ValidationEngineException {
    Map<String, AssemblySequenceInfo> sequenceInfo = new LinkedHashMap();
    Set<String> agpPlacedComponents = new HashSet<>();

    sequenceInfo.put("contig1", new AssemblySequenceInfo(100, 0, "contig1"));
    sequenceInfo.put("contig2", new AssemblySequenceInfo(150, 0, "contig2"));
    sequenceInfo.put("contig3", new AssemblySequenceInfo(200, 0, "contig3"));
    sequenceInfo.put("contig4", new AssemblySequenceInfo(300, 0, "contig4"));

    // AGP
    // chr1 contig1
    // chr1 contig2
    // chr1 contig3
    // chr1 contig4
    sequenceInfo.put("chr1", new AssemblySequenceInfo(600, 2, "chr1"));

    addComponent(agpPlacedComponents, "contig1");
    addComponent(agpPlacedComponents, "contig2");
    addComponent(agpPlacedComponents, "contig3");
    addComponent(agpPlacedComponents, "contig4");

    assertTrue(GenomeUtils.calculateGenomeSize(sequenceInfo, agpPlacedComponents) == 600);
  }
}
