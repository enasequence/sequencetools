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
package uk.ac.ebi.embl.api.validation.check.sourcefeature;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;

public class MoleculeTypeAndOrganismCheckTest {

  private Entry entry;
  private Feature source;
  private MoleculeTypeAndOrganismCheck check;
  private TaxonomyClient taxonomyClient;

  @Before
  public void setUp() throws SQLException {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    EntryFactory entryFactory = new EntryFactory();
    SequenceFactory sequenceFactory = new SequenceFactory();
    FeatureFactory featureFactory = new FeatureFactory();
    EmblEntryValidationPlanProperty property = TestHelper.testEmblEntryValidationPlanProperty();
    entry = entryFactory.createEntry();
    source = featureFactory.createSourceFeature();
    entry.addFeature(source);

    Sequence sequence = sequenceFactory.createSequence();
    entry.setSequence(sequence);

    taxonomyClient = createMock(TaxonomyClient.class);
    property.taxonClient.set(taxonomyClient);
    DataRow dataRow =
        new DataRow(
            "Deltavirus,Retro-transcribing viruses,ssRNA viruses,dsRNA viruses", "genomic RNA");
    GlobalDataSets.addTestDataSet(GlobalDataSetFile.MOLTYPE_ORGANISM, dataRow);
    check = new MoleculeTypeAndOrganismCheck();
    check.setEmblEntryValidationPlanProperty(property);
  }

  @After
  public void tearDown() {
    GlobalDataSets.resetTestDataSets();
  }

  @Test
  public void testCheck_NoEntry() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoMoleculeType() {
    entry.getSequence().setMoleculeType(null);
    source.addQualifier("organism", "Deltavirus");

    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_NoSequence() {
    entry.setSequence(null);
    source.addQualifier("organism", "Deltavirus");

    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_OneOrganism() {
    entry.getSequence().setMoleculeType("genomic RNA");
    source.addQualifier("organism", "Deltavirus");

    expect(taxonomyClient.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
    expect(
            taxonomyClient.isChildOfAny(
                "Deltavirus",
                "Deltavirus",
                "Retro-transcribing viruses",
                "ssRNA viruses",
                "dsRNA viruses"))
        .andReturn(Boolean.TRUE);
    replay(taxonomyClient);

    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_NoOrganisms() {
    entry.getSequence().setMoleculeType("genomic RNA");

    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("MoleculeTypeAndOrganismCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_NoSource() {
    entry.getSequence().setMoleculeType("genomic RNA");
    entry.removeFeature(source);

    ValidationResult result = check.check(entry);
    assertEquals(0, result.getMessages().size());
  }

  @Test
  public void testCheck_WrongOrganism() {
    entry.getSequence().setMoleculeType("genomic RNA");
    source.addQualifier("organism", "some organism");

    ValidationResult result = check.check(entry);
    assertEquals(0, result.getMessages().size()); // just leaves if the organism is not recognized
  }

  @Test
  public void testCheck_Message() {
    entry.getSequence().setMoleculeType("genomic RNA");

    ValidationResult result = check.check(entry);
    Collection<ValidationMessage<Origin>> messages =
        result.getMessages("MoleculeTypeAndOrganismCheck", Severity.ERROR);
    assertEquals(
        "Organism must belong to one of \"Deltavirus, Retro-transcribing viruses, ssRNA viruses, dsRNA viruses\" when molecule type is \"genomic RNA\".",
        messages.iterator().next().getMessage());
  }
}
