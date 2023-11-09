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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;

public class OrganismAndPermittedQualifierCheckTest {

  private Entry entry;
  private SourceFeature source;
  private OrganismAndPermittedQualifierCheck check;
  private TaxonomyClient taxonClient;
  EntryFactory entryFactory;
  FeatureFactory featureFactory;
  EmblEntryValidationPlanProperty property;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    entryFactory = new EntryFactory();
    featureFactory = new FeatureFactory();
    entry = entryFactory.createEntry();
    property = TestHelper.testEmblEntryValidationPlanProperty();
    source = featureFactory.createSourceFeature();
    taxonClient = createMock(TaxonomyClient.class);
    property.taxonClient.set(taxonClient);
    DataRow dataRow = new DataRow("virion", "Viruses,Viroids");
    GlobalDataSets.addTestDataSet(GlobalDataSetFile.ORG_PERMITTED_QUALIFIER, dataRow);
    check = new OrganismAndPermittedQualifierCheck();
    check.setEmblEntryValidationPlanProperty(property);
  }

  @After
  public void tearDown() {
    GlobalDataSets.resetTestDataSets();
  }

  @Test
  public void testCheck_NoFeature() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoQualifiers() {
    entry.addFeature(source);
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_NoVirion() {
    source.setSingleQualifierValue("organism", "Deltavirus");
    entry.addFeature(source);
    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_NoOrganism() {
    source.setSingleQualifier("virion");
    entry.addFeature(source);
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("OrganismAndPermittedQualifierCheck1", Severity.ERROR));
  }

  @Test
  public void testCheck_NoOrganismValue() {
    source.setSingleQualifier("organism");
    source.setSingleQualifier("virion");
    entry.addFeature(source);
    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("OrganismAndPermittedQualifierCheck1", Severity.ERROR));
  }

  @Test
  public void testCheck_WrongOrganism() {
    source.setSingleQualifierValue("organism", "Bacteria");
    source.setSingleQualifier("virion");
    entry.addFeature(source);
    expect(taxonClient.isOrganismValid("Bacteria")).andReturn(Boolean.TRUE);
    expect(taxonClient.isOrganismValid("Bacteria")).andReturn(Boolean.TRUE);
    expect(taxonClient.isChildOfAny("Bacteria", "Viruses", "Viroids")).andReturn(Boolean.FALSE);
    expect(taxonClient.isChildOf("Bacteria", "Bacteria")).andReturn(Boolean.FALSE);
    expect(taxonClient.isChildOf("Bacteria", "Archaea")).andReturn(Boolean.FALSE);
    replay(taxonClient);

    ValidationResult result = check.check(entry);
    assertEquals(1, result.count("OrganismAndPermittedQualifierCheck3", Severity.ERROR));
  }

  @Test
  public void testCheck() {
    source.setSingleQualifierValue("organism", "Deltavirus");
    source.setSingleQualifier("virion");
    entry.addFeature(source);
    expect(taxonClient.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
    expect(taxonClient.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
    expect(taxonClient.isChildOfAny("Deltavirus", "Viruses", "Viroids")).andReturn(Boolean.TRUE);
    expect(taxonClient.isChildOf("Deltavirus", "Bacteria")).andReturn(Boolean.FALSE);
    expect(taxonClient.isChildOf("Deltavirus", "Archaea")).andReturn(Boolean.FALSE);
    replay(taxonClient);

    assertTrue(check.check(entry).isValid());
  }

  @Test
  public void testCheck_Message() {
    source.setSingleQualifierValue("organism", "Bacteria");
    source.setSingleQualifier("virion");
    entry.addFeature(source);
    expect(taxonClient.isOrganismValid("Bacteria")).andReturn(Boolean.TRUE);
    expect(taxonClient.isOrganismValid("Bacteria")).andReturn(Boolean.TRUE);
    expect(taxonClient.isChildOfAny("Bacteria", "Viruses", "Viroids")).andReturn(Boolean.FALSE);
    expect(taxonClient.isChildOf("Bacteria", "Bacteria")).andReturn(Boolean.FALSE);
    expect(taxonClient.isChildOf("Bacteria", "Archaea")).andReturn(Boolean.FALSE);
    replay(taxonClient);

    ValidationResult result = check.check(entry);
    Collection<ValidationMessage<Origin>> messages =
        result.getMessages("OrganismAndPermittedQualifierCheck3", Severity.ERROR);
    assertEquals(
        "Organism \"Bacteria\" not belongs to \"Viruses, Viroids\".Qualifier \"virion\" is only permitted when organism belongs to \"Viruses, Viroids\".",
        messages.iterator().next().getMessage());
  }

  @Test
  public void testCheck_withValidProduct() {
    Feature feature = featureFactory.createFeature("feature");
    feature.addQualifier(Qualifier.PRODUCT_QUALIFIER_NAME, "16S ribosomal RNA");
    entry.addFeature(feature);
    source.setSingleQualifierValue("organism", "Deltavirus");
    source.setSingleQualifier("virion");
    entry.addFeature(source);
    expect(taxonClient.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
    expect(taxonClient.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
    expect(taxonClient.isChildOfAny("Deltavirus", "Viruses", "Viroids")).andReturn(Boolean.TRUE);
    expect(taxonClient.isChildOf("Deltavirus", "Bacteria")).andReturn(Boolean.TRUE);
    expect(taxonClient.isChildOf("Deltavirus", "Archaea")).andReturn(Boolean.TRUE);
    replay(taxonClient);
    property.validationScope.set(ValidationScope.EMBL_TEMPLATE);
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult result = check.check(entry);
    assertEquals(0, result.getMessages("OrganismAndPermittedQualifierCheck2").size());
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_withValidGene() {
    Feature feature = featureFactory.createFeature("feature");
    feature.addQualifier(Qualifier.GENE_QUALIFIER_NAME, "rRNA");
    entry.addFeature(feature);
    source.setSingleQualifierValue("organism", "Deltavirus");
    source.setSingleQualifier("virion");
    entry.addFeature(source);
    expect(taxonClient.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
    expect(taxonClient.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
    expect(taxonClient.isChildOfAny("Deltavirus", "Viruses", "Viroids")).andReturn(Boolean.TRUE);
    expect(taxonClient.isChildOf("Deltavirus", "Bacteria")).andReturn(Boolean.TRUE);
    expect(taxonClient.isChildOf("Deltavirus", "Archaea")).andReturn(Boolean.TRUE);
    replay(taxonClient);
    property.validationScope.set(ValidationScope.EMBL_TEMPLATE);
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult result = check.check(entry);
    assertEquals(0, result.getMessages("OrganismAndPermittedQualifierCheck2").size());
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_withInValidProduct() {
    Feature feature = featureFactory.createFeature("feature");
    feature.addQualifier(Qualifier.PRODUCT_QUALIFIER_NAME, "16S RNA");
    entry.addFeature(feature);
    source.setSingleQualifierValue("organism", "Deltavirus");
    source.setSingleQualifier("virion");
    entry.addFeature(source);
    expect(taxonClient.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
    expect(taxonClient.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
    expect(taxonClient.isChildOfAny("Deltavirus", "Viruses", "Viroids")).andReturn(Boolean.TRUE);
    expect(taxonClient.isChildOf("Deltavirus", "Bacteria")).andReturn(Boolean.TRUE);
    expect(taxonClient.isChildOf("Deltavirus", "Archaea")).andReturn(Boolean.TRUE);
    replay(taxonClient);
    property.validationScope.set(ValidationScope.EMBL_TEMPLATE);
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult result = check.check(entry);
    assertEquals(1, result.getMessages("OrganismAndPermittedQualifierCheck2").size());
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_withInValidProductandGene() {
    Feature feature = featureFactory.createFeature("feature");
    feature.addQualifier(Qualifier.PRODUCT_QUALIFIER_NAME, "16S RNA");
    feature.addQualifier(Qualifier.GENE_QUALIFIER_NAME, "rNA");
    entry.addFeature(feature);
    source.setSingleQualifierValue("organism", "Deltavirus");
    source.setSingleQualifier("virion");
    entry.addFeature(source);
    expect(taxonClient.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
    expect(taxonClient.isOrganismValid("Deltavirus")).andReturn(Boolean.TRUE);
    expect(taxonClient.isChildOfAny("Deltavirus", "Viruses", "Viroids")).andReturn(Boolean.TRUE);
    expect(taxonClient.isChildOf("Deltavirus", "Bacteria")).andReturn(Boolean.TRUE);
    expect(taxonClient.isChildOf("Deltavirus", "Archaea")).andReturn(Boolean.TRUE);
    replay(taxonClient);
    property.validationScope.set(ValidationScope.EMBL_TEMPLATE);
    check.setEmblEntryValidationPlanProperty(property);
    ValidationResult result = check.check(entry);
    assertEquals(1, result.getMessages("OrganismAndPermittedQualifierCheck2").size());
    assertTrue(result.isValid());
  }
}
