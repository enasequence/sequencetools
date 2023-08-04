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
package uk.ac.ebi.embl.api.validation.check.entry;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class AGPValidationCheckTest {

  private Entry entry;
  private AGPValidationCheck check;
  private EntryDAOUtils entryDAOUtils;
  private EmblEntryValidationPlanProperty planProperty;
  List<String> linkageEvidences;

  @Before
  public void setUp() throws SQLException {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    planProperty = new EmblEntryValidationPlanProperty();
    EntryFactory entryFactory = new EntryFactory();
    SequenceFactory sequenceFactory = new SequenceFactory();
    Sequence sequence = sequenceFactory.createSequence();
    entry = entryFactory.createEntry();
    entry.setSequence(sequence);
    check = new AGPValidationCheck();
    entryDAOUtils = createMock(EntryDAOUtils.class);
    linkageEvidences = new ArrayList<String>();
    linkageEvidences.add("paired-ends");
  }

  @Test
  public void testCheck_withNoagpRows() throws ValidationEngineException, SQLException {
    check.setEmblEntryValidationPlanProperty(planProperty);
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_withNoDatabaseConnection() throws ValidationEngineException, SQLException {
    check.setEmblEntryValidationPlanProperty(planProperty);
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testcheck_withvalidAGProws() throws SQLException, ValidationEngineException {
    AgpRow validComponentrow1 = new AgpRow();
    AgpRow validGaprow1 = new AgpRow();
    validComponentrow1.setObject("IWGSC_CSS_6DL_scaff_3330716");
    validComponentrow1.setObject_beg(1l);
    validComponentrow1.setObject_end(330l);
    validComponentrow1.setPart_number(1);
    validComponentrow1.setComponent_type_id("W");
    validComponentrow1.setComponent_beg(1l);
    validComponentrow1.setComponent_end(330l);
    validComponentrow1.setComponent_id("IWGSC_CSS_6DL_contig_209591");
    validComponentrow1.setOrientation("+");

    validGaprow1.setObject("IWGSC_CSS_6DL_scaff_3330716");
    validGaprow1.setObject_beg(331);
    validGaprow1.setObject_end(354l);
    validGaprow1.setPart_number(2);
    validGaprow1.setComponent_type_id("N");
    validGaprow1.setGap_length(24l);
    validGaprow1.setGap_type("scaffold");
    validGaprow1.setLinkageevidence(linkageEvidences);
    HashMap<String, AssemblySequenceInfo> assemblyseqinfo =
        new HashMap<String, AssemblySequenceInfo>();
    AssemblySequenceInfo sequenceInfo = new AssemblySequenceInfo(400, 2, null);
    assemblyseqinfo.put("IWGSC_CSS_6DL_contig_209591".toUpperCase(), sequenceInfo);
    entry.getSequence().addAgpRow(validComponentrow1);
    entry.getSequence().addAgpRow(validGaprow1);
    planProperty.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
    planProperty.analysis_id.set("ERZ00001");
    planProperty.assemblySequenceInfo.set(assemblyseqinfo);
    check.setEmblEntryValidationPlanProperty(planProperty);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testcheck_emptyAgpRow() throws SQLException, ValidationEngineException {
    AgpRow inValidGaprow1 = new AgpRow();
    entry.getSequence().addAgpRow(inValidGaprow1);
    planProperty.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
    planProperty.fileType.set(FileType.AGP);
    check.setEmblEntryValidationPlanProperty(planProperty);
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("AGPValidationCheck-13", Severity.ERROR));
  }

  @Test
  public void testcheck_invalidAgpComponentRow() throws SQLException, ValidationEngineException {
    AgpRow inValidComponentrow1 = new AgpRow();
    inValidComponentrow1.setObject("IWGSC_CSS_6DL_scaff_3330716");
    inValidComponentrow1.setObject_beg(1);
    inValidComponentrow1.setObject_end(331l);
    inValidComponentrow1.setPart_number(1);
    inValidComponentrow1.setComponent_type_id("X");
    inValidComponentrow1.setComponent_beg(1l);
    inValidComponentrow1.setComponent_end(330l);
    inValidComponentrow1.setComponent_id("IWGSC_CSS_6DL_contig_209591");
    inValidComponentrow1.setOrientation("#");
    HashMap<String, AssemblySequenceInfo> assemblyseqinfo =
        new HashMap<String, AssemblySequenceInfo>();
    AssemblySequenceInfo sequenceInfo = new AssemblySequenceInfo(400, 2, null);
    assemblyseqinfo.put("IWGSC_CSS_6DL_contig_209592".toUpperCase(), sequenceInfo);
    entry.getSequence().addAgpRow(inValidComponentrow1);
    planProperty.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
    planProperty.fileType.set(FileType.AGP);
    planProperty.analysis_id.set("ERZ00001");
    planProperty.assemblySequenceInfo.set(assemblyseqinfo);
    check.setEmblEntryValidationPlanProperty(planProperty);
    ValidationResult result = check.check(entry);
    assertEquals(4, result.getMessages().size());
    assertEquals(1, result.count("AGPValidationCheck-3", Severity.ERROR));
    assertEquals(1, result.count("AGPValidationCheck-5", Severity.ERROR));
    assertEquals(1, result.count("AGPValidationCheck-11", Severity.ERROR));
    assertEquals(1, result.count("AGPValidationCheck-9", Severity.ERROR));
  }

  @Test
  public void testcheck_invalidAgpGapRow() throws SQLException, ValidationEngineException {
    AgpRow validComponentrow1 = new AgpRow();
    AgpRow inValidGaprow1 = new AgpRow();
    validComponentrow1.setObject("IWGSC_CSS_6DL_scaff_3330716");
    validComponentrow1.setObject_beg(1);
    validComponentrow1.setObject_end(330l);
    validComponentrow1.setPart_number(1);
    validComponentrow1.setComponent_type_id("W");
    validComponentrow1.setComponent_beg(1l);
    validComponentrow1.setComponent_end(330l);
    validComponentrow1.setComponent_id("IWGSC_CSS_6DL_contig_209591");
    validComponentrow1.setOrientation("+");
    inValidGaprow1.setObject("IWGSC_CSS_6DL_scaff_3330716");
    inValidGaprow1.setObject_beg(331);
    inValidGaprow1.setObject_end(354l);
    inValidGaprow1.setPart_number(2);
    inValidGaprow1.setComponent_type_id("N");
    inValidGaprow1.setGap_length(23l);
    inValidGaprow1.setGap_type("scaffol");
    inValidGaprow1.setLinkage("NO");
    linkageEvidences.add("paired-nds");
    inValidGaprow1.setLinkageevidence(linkageEvidences);
    entry.getSequence().addAgpRow(validComponentrow1);
    entry.getSequence().addAgpRow(inValidGaprow1);
    planProperty.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
    planProperty.fileType.set(FileType.AGP);
    check.setEmblEntryValidationPlanProperty(planProperty);
    ValidationResult result = check.check(entry);
    assertEquals(2, result.getMessages().size());
    assertEquals(1, result.count("AGPValidationCheck-4", Severity.ERROR));
    assertEquals(1, result.count("AGPValidationCheck-8", Severity.ERROR));
  }

  @Test
  public void testcheck_AGPRowwithinvalidObjectBeginandObjectEnd()
      throws SQLException, ValidationEngineException {
    AgpRow validComponentrow1 = new AgpRow();
    AgpRow inValidGaprow1 = new AgpRow();
    validComponentrow1.setObject("IWGSC_CSS_6DL_scaff_3330716");
    validComponentrow1.setObject_beg(1);
    validComponentrow1.setObject_end(200l);
    validComponentrow1.setPart_number(1);
    validComponentrow1.setComponent_type_id("W");
    validComponentrow1.setComponent_beg(1l);
    validComponentrow1.setComponent_end(330l);
    validComponentrow1.setComponent_id("IWGSC_CSS_6DL_contig_209591");
    validComponentrow1.setOrientation("+");
    inValidGaprow1.setObject("IWGSC_CSS_6DL_scaff_3330716");
    inValidGaprow1.setObject_beg(331);
    inValidGaprow1.setObject_end(354l);
    inValidGaprow1.setPart_number(2);
    inValidGaprow1.setComponent_type_id("N");
    inValidGaprow1.setGap_length(24l);
    inValidGaprow1.setGap_type("scaffold");
    inValidGaprow1.setLinkageevidence(linkageEvidences);
    entry.getSequence().addAgpRow(validComponentrow1);
    entry.getSequence().addAgpRow(inValidGaprow1);
    planProperty.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
    planProperty.fileType.set(FileType.AGP);
    check.setEmblEntryValidationPlanProperty(planProperty);
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("AGPValidationCheck-7", Severity.ERROR));
    assertEquals(1, result.count("AGPValidationCheck-11", Severity.ERROR));
  }

  @Test
  public void testcheck_AGPRowwithinvalidgapLength()
      throws SQLException, ValidationEngineException {
    AgpRow validComponentrow1 = new AgpRow();
    AgpRow inValidGaprow1 = new AgpRow();
    validComponentrow1.setObject("IWGSC_CSS_6DL_scaff_3330716");
    validComponentrow1.setObject_beg(1);
    validComponentrow1.setObject_end(330l);
    validComponentrow1.setPart_number(1);
    validComponentrow1.setComponent_type_id("W");
    validComponentrow1.setComponent_beg(1l);
    validComponentrow1.setComponent_end(330l);
    validComponentrow1.setComponent_id("IWGSC_CSS_6DL_contig_209591");
    validComponentrow1.setOrientation("+");
    inValidGaprow1.setObject("IWGSC_CSS_6DL_scaff_3330716");
    inValidGaprow1.setObject_beg(331);
    inValidGaprow1.setObject_end(354l);
    inValidGaprow1.setPart_number(2);
    inValidGaprow1.setComponent_type_id("N");
    inValidGaprow1.setGap_length(21l);
    inValidGaprow1.setGap_type("scaffold");
    inValidGaprow1.setLinkageevidence(linkageEvidences);
    entry.getSequence().addAgpRow(validComponentrow1);
    entry.getSequence().addAgpRow(inValidGaprow1);
    planProperty.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
    planProperty.fileType.set(FileType.AGP);
    check.setEmblEntryValidationPlanProperty(planProperty);
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("AGPValidationCheck-8", Severity.ERROR));
  }
}
