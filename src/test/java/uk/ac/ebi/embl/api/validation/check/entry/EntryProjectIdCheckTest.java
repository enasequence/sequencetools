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
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;

public class EntryProjectIdCheckTest {

  private EntryProjectIdCheck check, check1;
  QualifierFactory qualifierFactory;
  FeatureFactory featureFactory;
  Feature feature;
  Qualifier qualifier;
  private TaxonomyClient taxonomyClient;
  private SourceFeature source;
  EmblEntryValidationPlanProperty property;
  EntryDAOUtils entryDAOUtils;

  @Before
  public void setUp() throws SQLException {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    check = new EntryProjectIdCheck();
    taxonomyClient = createMock(TaxonomyClient.class);
    property = new EmblEntryValidationPlanProperty();
    check1 = new EntryProjectIdCheck();
    property.taxonClient.set(taxonomyClient);
    property.validationScope.set(ValidationScope.EMBL);
    check1.setEmblEntryValidationPlanProperty(property);
    check.setEmblEntryValidationPlanProperty(property);
    qualifierFactory = new QualifierFactory();
    featureFactory = new FeatureFactory();
    feature = featureFactory.createFeature(Feature.GENE_FEATURE_NAME);
    source = featureFactory.createSourceFeature();
    qualifier = qualifierFactory.createQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME);
    feature.addQualifier(qualifier);
    entryDAOUtils = createMock(EntryDAOUtils.class);
    check.setEntryDAOUtils(entryDAOUtils);
  }

  @Test
  public void testCheck_NoEntry() throws ValidationEngineException {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoQualifiers() throws ValidationEngineException {
    assertTrue(check.check(new EntryFactory().createEntry()).isValid());
  }

  @Test
  public void testCheck_wrongOrganism() throws ValidationEngineException {
    Entry entry = new EntryFactory().createEntry();
    Sequence sequence = new SequenceFactory().createSequenceofLength(100, 'a');
    sequence.setTopology(Sequence.Topology.CIRCULAR);
    entry.setSequence(sequence);
    source.setSingleQualifierValue("organism", "abcdef");
    source.setSingleQualifier("virion");
    entry.addFeature(source);
    expect(taxonomyClient.isOrganismValid("abcdef")).andReturn(Boolean.FALSE);
    expect(taxonomyClient.isOrganismValid("Bacteria")).andReturn(Boolean.TRUE);
    replay(taxonomyClient);
    ValidationResult result = check1.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_Organism_noVirus() throws ValidationEngineException {
    Entry entry = new EntryFactory().createEntry();
    Sequence sequence = new SequenceFactory().createSequenceofLength(10, 'a');
    sequence.setTopology(Sequence.Topology.CIRCULAR);
    // sequence.setLength(10);
    entry.setSequence(sequence);
    source.setSingleQualifierValue("organism", "Bacteria");
    source.setSingleQualifier("virion");
    entry.addFeature(source);
    expect(taxonomyClient.isOrganismValid("abcdef")).andReturn(Boolean.FALSE);
    expect(taxonomyClient.isOrganismValid("Bacteria")).andReturn(Boolean.TRUE);
    expect(taxonomyClient.isChildOf("Bacteria", "Viruses")).andReturn(Boolean.FALSE);
    replay(taxonomyClient);
    ValidationResult result = check1.check(entry);
    assertTrue(!result.isValid());
  }

  @Test
  public void testCheck_Organism_Virus() throws ValidationEngineException {
    Entry entry = new EntryFactory().createEntry();
    Sequence sequence = new SequenceFactory().createSequenceofLength(10, 'a');
    sequence.setTopology(Sequence.Topology.CIRCULAR);
    // sequence.setLength(10);
    entry.setSequence(sequence);
    source.setSingleQualifierValue("organism", "Bacteria");
    source.setSingleQualifier("virion");
    entry.addFeature(source);
    expect(taxonomyClient.isOrganismValid("abcdef")).andReturn(Boolean.FALSE);
    expect(taxonomyClient.isOrganismValid("Bacteria")).andReturn(Boolean.TRUE);
    expect(taxonomyClient.isChildOf("Bacteria", "Viruses")).andReturn(Boolean.TRUE);
    replay(taxonomyClient);
    ValidationResult result = check1.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_Topology1() throws ValidationEngineException {
    Entry entry = new EntryFactory().createEntry();
    Sequence sequence = new SequenceFactory().createSequenceofLength(10, 'a');
    sequence.setTopology(Sequence.Topology.CIRCULAR);
    // sequence.setLength(10);
    entry.setSequence(sequence);
    Feature source = new FeatureFactory().createFeature(Feature.SOURCE_FEATURE_NAME);
    entry.addFeature(source);
    ValidationResult result = check.check(entry);
    assertTrue(!result.isValid());
    assertEquals(1, result.count("EntryProjectIdCheck2", Severity.ERROR));
  }

  @Test
  public void testCheck_Topology2() throws ValidationEngineException {
    Entry entry = new EntryFactory().createEntry();
    Sequence sequence = new SequenceFactory().createSequenceofLength(10, 'a');
    sequence.setTopology(Sequence.Topology.LINEAR);
    entry.setSequence(sequence);
    Feature source = new FeatureFactory().createFeature(Feature.SOURCE_FEATURE_NAME);
    source.addQualifier(new QualifierFactory().createQualifier("plasmid"));
    entry.addFeature(source);

    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_HasValidProjectId() throws ValidationEngineException, SQLException {
    Entry entry = new EntryFactory().createEntry();
    entry.addProjectAccession(new Text("PROJECTACC"));
    expect(entryDAOUtils.isProjectValid("PROJECTACC")).andReturn(true);
    replay(entryDAOUtils);
    check.setEntryDAOUtils(entryDAOUtils);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  @Test
  public void testCheck_SequenceLength1() throws ValidationEngineException {
    Entry entry = new EntryFactory().createEntry();
    Sequence sequence = new SequenceFactory().createSequenceofLength(100, 'a');
    sequence.setTopology(Sequence.Topology.LINEAR);
    // sequence.setLength(100);
    entry.setSequence(sequence);

    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
  }

  /*	@Test
  public void testCheck_SequenceLength2() throws ValidationEngineException {
  	Entry entry = new EntryFactory().createEntry();
  	Sequence sequence = new SequenceFactory().createSequenceofLength(100000,'a');
  	sequence.setTopology(Sequence.Topology.LINEAR);
  	//sequence.setLength(100000);
  	entry.setSequence(sequence);

  	ValidationResult result = check.check(entry);
  	assertTrue(!result.isValid());
  	assertEquals(1, result.count("EntryProjectIdCheck1", Severity.ERROR));
  }*/

  @Test
  public void testCheck_WGSEntry() throws ValidationEngineException {
    Entry entry = new EntryFactory().createEntry();
    entry.setDataClass(Entry.WGS_DATACLASS);
    Sequence sequence = new SequenceFactory().createSequenceofLength(100, 'a');
    // sequence.setLength(100);
    entry.setSequence(sequence);
    ValidationResult result = check.check(entry);
    assertTrue(!result.isValid());
    assertEquals(1, result.count("EntryProjectIdCheck3", Severity.ERROR));
  }

  @Test
  public void testCheck_KeywordExists() throws ValidationEngineException {
    Entry entry = new EntryFactory().createEntry();
    Sequence sequence = new SequenceFactory().createSequenceofLength(100, 'a');
    // sequence.setLength(100);
    entry.setSequence(sequence);
    entry.setDescription(new Text("  Clostridium phage phiSM101, complete genome."));
    entry.addKeyword(new Text("complete genome"));
    ValidationResult result = check.check(entry);
    assertTrue(!result.isValid());
    assertEquals(1, result.count("EntryProjectIdCheck4", Severity.ERROR));
  }

  @Test
  public void testCheck_NoKeywordDELine1() throws ValidationEngineException {
    Entry entry = new EntryFactory().createEntry();
    Sequence sequence = new SequenceFactory().createSequenceofLength(100, 'a');
    // sequence.setLength(100);
    entry.setSequence(sequence);
    entry.addKeyword(new Text("complete genome"));
    ValidationResult result = check.check(entry);
    assertTrue(!result.isValid());
    assertEquals(1, result.count("EntryProjectIdCheck4", Severity.ERROR));
  }

  @Test
  public void testCheck_NoKeywordDELine2() throws ValidationEngineException {
    Entry entry = new EntryFactory().createEntry();
    Sequence sequence = new SequenceFactory().createSequenceofLength(100, 'a');
    // sequence.setLength(100);
    entry.setSequence(sequence);
    entry.setDescription(new Text("completegenome"));
    entry.addKeyword(new Text("complete genome"));
    ValidationResult result = check.check(entry);
    assertTrue(!result.isValid());
    assertEquals(1, result.count("EntryProjectIdCheck4", Severity.ERROR));
  }

  @Test
  public void testCheck_NoKeywordKWLine() throws ValidationEngineException {
    Entry entry = new EntryFactory().createEntry();
    entry.setDescription(new Text("  Clostridium phage phiSM101, complete genome."));
    Sequence sequence = new SequenceFactory().createSequenceofLength(100, 'a');
    // sequence.setLength(100);
    entry.setSequence(sequence);
    ValidationResult result = check.check(entry);
    assertTrue(!result.isValid());
    assertEquals(1, result.count("EntryProjectIdCheck5", Severity.ERROR));
  }

  @Test
  public void testCheck_KeywordNotExists() throws ValidationEngineException {
    Entry entry = new EntryFactory().createEntry();
    Sequence sequence = new SequenceFactory().createSequenceofLength(100, 'a');
    // sequence.setLength(100);
    entry.setSequence(sequence);
    ValidationResult result = check.check(entry);
    assertTrue(result.isValid());
    assertEquals(0, result.count("EntryProjectIdCheck4", Severity.ERROR));
  }

  @Test
  public void testCheck_Project_Locus_Tag() throws ValidationEngineException {
    Entry entry = new EntryFactory().createEntry();
    Sequence sequence = new SequenceFactory().createSequenceofLength(100, 'a');
    // sequence.setLength(100);
    entry.setSequence(sequence);
    entry.addFeature(feature);
    ValidationResult result = check.check(entry);
    assertTrue(!result.isValid());
    assertEquals(1, result.count("EntryProjectIdCheck7", Severity.ERROR));
  }

  /*@Test
  public void testCheck_Invalid_projectId() throws SQLException, ValidationEngineException
  {
  	Entry entry = new EntryFactory().createEntry();
  	entry.addProjectAccession(new Text("PRJEA0"));
  	expect(entryDAOUtils.isProjectValid("PRJEA0")).andReturn(false);
  	replay(entryDAOUtils);
  	check.setEntryDAOUtils(entryDAOUtils);
  	ValidationResult result = check.check(entry);
  	assertTrue(!result.isValid());
  	assertEquals(1, result.count("EntryProjectIdCheck9", Severity.ERROR));
  }*/
  // move this code to separate class

}
