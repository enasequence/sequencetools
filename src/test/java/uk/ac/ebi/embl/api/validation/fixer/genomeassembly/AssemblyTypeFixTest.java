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
package uk.ac.ebi.embl.api.validation.fixer.genomeassembly;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyType;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class AssemblyTypeFixTest {

  AssemblyInfoEntry entry = new AssemblyInfoEntry();
  AssemblyTypeFix fix;

  @Test
  public void testAssembyTypeCaseFixedValue() {
    entry.setAssemblyType("metagenome-assembled genome (mag)");
    fix = new AssemblyTypeFix();
    ValidationResult result = fix.check(entry);
    assertEquals(1, result.getMessages("AssemblyinfoAssemblyTypeFix").size());
    assertEquals(AssemblyType.METAGENOME_ASSEMBLEDGENOME.getFixedValue(), entry.getAssemblyType());

    entry.setAssemblyType("Covid-19 outbreak");
    fix = new AssemblyTypeFix();
    result = fix.check(entry);
    assertEquals(1, result.getMessages("AssemblyinfoAssemblyTypeFix").size());
    assertEquals(AssemblyType.COVID_19_OUTBREAK.getFixedValue(), entry.getAssemblyType());

    entry.setAssemblyType("Environmental single-cell amplified genome (SAG)");
    fix = new AssemblyTypeFix();
    result = fix.check(entry);
    assertEquals(1, result.getMessages("AssemblyinfoAssemblyTypeFix").size());
    assertEquals(
        AssemblyType.ENVIRONMENTALSINGLE_CELLAMPLIFIEDGENOME.getFixedValue(),
        entry.getAssemblyType());
  }

  @Test
  public void testAssembyTypeCaseNoFix() {
    entry.setAssemblyType("Metagenome-Assembled Genome (MAG)");
    fix = new AssemblyTypeFix();
    ValidationResult result = fix.check(entry);
    assertEquals(0, result.getMessages(Severity.FIX).size());
    assertEquals(AssemblyType.METAGENOME_ASSEMBLEDGENOME.getFixedValue(), entry.getAssemblyType());

    entry.setAssemblyType("COVID-19 outbreak");
    fix = new AssemblyTypeFix();
    result = fix.check(entry);
    assertEquals(0, result.getMessages(Severity.FIX).size());
    assertEquals(AssemblyType.COVID_19_OUTBREAK.getFixedValue(), entry.getAssemblyType());
    assertEquals(
        "COVID-19 outbreak",
        AssemblyType.COVID_19_OUTBREAK.getFixedValue(),
        entry.getAssemblyType());

    entry.setAssemblyType("Environmental Single-Cell Amplified Genome (SAG)");
    fix = new AssemblyTypeFix();
    result = fix.check(entry);
    assertEquals(0, result.getMessages(Severity.FIX).size());
    assertEquals(
        AssemblyType.ENVIRONMENTALSINGLE_CELLAMPLIFIEDGENOME.getFixedValue(),
        entry.getAssemblyType());
    assertEquals(
        "Environmental Single-Cell Amplified Genome (SAG)",
        AssemblyType.ENVIRONMENTALSINGLE_CELLAMPLIFIEDGENOME.getFixedValue());
  }
}
