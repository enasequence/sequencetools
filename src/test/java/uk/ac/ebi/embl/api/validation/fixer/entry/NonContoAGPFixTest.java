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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class NonContoAGPFixTest {

  private Entry entry;
  private NonContoAGPFix check;
  public EntryFactory entryFactory;
  public LocationFactory locationFactory;
  public SequenceFactory sequenceFactory;
  public FeatureFactory featureFactory;
  public Feature assemblyGapFeature, assemblyGapFeature1;
  public EntryDAOUtils entryDAOUtils;
  public EmblEntryValidationPlanProperty planProperty;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
    entryFactory = new EntryFactory();
    featureFactory = new FeatureFactory();
    locationFactory = new LocationFactory();
    entry = entryFactory.createEntry();
    SequenceFactory sequenceFactory = new SequenceFactory();
    entry.setSequence(sequenceFactory.createSequence());
    check = new NonContoAGPFix();
    entryDAOUtils = createMock(EntryDAOUtils.class);
    planProperty = TestHelper.testEmblEntryValidationPlanProperty();
    assemblyGapFeature = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    Order<Location> order = new Order<Location>();
    order.addLocation(locationFactory.createLocalRange(12L, 21L));
    assemblyGapFeature.setLocations(order);
    assemblyGapFeature.addQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME, "10");
    assemblyGapFeature.addQualifier(Qualifier.GAP_TYPE_QUALIFIER_NAME, "within scaffold");
    assemblyGapFeature.addQualifier(Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME, "paired-ends");
    assemblyGapFeature1 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
    Order<Location> order1 = new Order<Location>();
    order1.addLocation(locationFactory.createLocalRange(29L, 35L));
    assemblyGapFeature1.setLocations(order1);
    assemblyGapFeature1.addQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME, "5");
    assemblyGapFeature1.addQualifier(Qualifier.GAP_TYPE_QUALIFIER_NAME, "within scaffold");
    assemblyGapFeature1.addQualifier(Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME, "paired-ends");
    locationFactory = new LocationFactory();
    sequenceFactory = new SequenceFactory();
    entry.setSequence(sequenceFactory.createSequence());
    entry.getSequence().setSequence(ByteBuffer.wrap("aaaaaaaaaaannnnnnnnnnaaaaaaaa".getBytes()));
    entry.setSubmitterAccession("nonconentry");
    entry.setPrimaryAccession("AC0001");
    entry.getSequence().setVersion(1);
  }

  @Test
  public void testCheck_NoEntry() throws ValidationEngineException {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoAssemblyGapfeature() throws ValidationEngineException {
    check.check(entry);
    assertEquals(0, entry.getSequence().getAgpRows().size());
  }

  @Test
  public void testCheck_withoneAssemblyGapFeature() throws ValidationEngineException, SQLException {
    entry.addFeature(assemblyGapFeature);
    check.check(entry);
    assertEquals(3, entry.getSequence().getAgpRows().size());
    AgpRow row1 = entry.getSequence().getAgpRows().get(0);
    AgpRow row2 = entry.getSequence().getAgpRows().get(1);
    AgpRow row3 = entry.getSequence().getAgpRows().get(2);
    assertFalse(row1.isGap());
    assertEquals("nonconentry", row1.getObject());
    assertEquals("AC0001.1", row1.getObject_acc());
    assertEquals(Long.valueOf(1), row1.getObject_beg());
    assertEquals(Long.valueOf(11), row1.getObject_end());
    assertEquals(Long.valueOf(1), row1.getComponent_beg());
    assertEquals(Long.valueOf(11), row1.getComponent_end());
    assertEquals("AC0001.1", row1.getComponent_acc());
    assertEquals("nonconentry", row1.getComponent_id());
    assertEquals("O", row1.getComponent_type_id());
    assertNull(row1.getGap_length());
    assertNull(row1.getGap_type());
    assertNull(row1.getLinkageevidence());
    assertFalse(row1.isGap());
    assertEquals("+", row1.getOrientation());
    assertEquals(Integer.valueOf(1), row1.getPart_number());

    List<String> linkage_evidences = new ArrayList<String>();
    linkage_evidences.add("paired-ends");
    // second
    assertEquals("nonconentry", row2.getObject());
    assertEquals("AC0001.1", row2.getObject_acc());
    assertEquals(Long.valueOf(12), row2.getObject_beg());
    assertEquals(Long.valueOf(21), row2.getObject_end());
    assertNull(row2.getComponent_beg());
    assertNull(row2.getComponent_end());
    assertNull(row2.getComponent_acc());
    assertNull(row2.getComponent_id());
    assertEquals("N", row2.getComponent_type_id());
    assertEquals(Long.valueOf(10), row2.getGap_length());
    assertEquals("scaffold", row2.getGap_type());
    assertEquals(linkage_evidences, row2.getLinkageevidence());
    assertTrue(row2.isGap());
    assertNull(row2.getOrientation());
    assertEquals(Integer.valueOf(2), row2.getPart_number());
    assertTrue(row2.isGap());

    // third
    assertEquals("nonconentry", row3.getObject());
    assertEquals("AC0001.1", row3.getObject_acc());
    assertEquals(Long.valueOf(22), row3.getObject_beg());
    assertEquals(Long.valueOf(29), row3.getObject_end());
    assertEquals(Long.valueOf(22), row3.getComponent_beg());
    assertEquals(Long.valueOf(29), row3.getComponent_end());
    assertEquals("AC0001.1", row3.getComponent_acc());
    assertEquals("nonconentry", row3.getComponent_id());
    assertEquals("O", row3.getComponent_type_id());
    assertNull(row3.getGap_length());
    assertNull(row3.getGap_type());
    assertNull(row3.getLinkageevidence());
    assertFalse(row3.isGap());
    assertEquals("+", row3.getOrientation());
    assertEquals(Integer.valueOf(3), row3.getPart_number());
    assertFalse(row3.isGap());
  }

  @Test
  public void testCheck_withMultipleAssemblyGapFeature()
      throws ValidationEngineException, SQLException {
    entry.addFeature(assemblyGapFeature);
    entry.addFeature(assemblyGapFeature1);
    entry
        .getSequence()
        .setSequence(ByteBuffer.wrap("aaaaaaaaaaannnnnnnnnnaaaaaaaannnnnaaaaa".getBytes()));
    check.check(entry);
    assertEquals(5, entry.getSequence().getAgpRows().size());
    AgpRow row1 = entry.getSequence().getAgpRows().get(0);
    AgpRow row2 = entry.getSequence().getAgpRows().get(1);
    AgpRow row3 = entry.getSequence().getAgpRows().get(2);
    AgpRow row4 = entry.getSequence().getAgpRows().get(3);
    AgpRow row5 = entry.getSequence().getAgpRows().get(4);
    assertFalse(row1.isGap());
    assertEquals("nonconentry", row1.getObject());
    assertEquals("AC0001.1", row1.getObject_acc());
    assertEquals(Long.valueOf(1), row1.getObject_beg());
    assertEquals(Long.valueOf(11), row1.getObject_end());
    assertEquals(Long.valueOf(1), row1.getComponent_beg());
    assertEquals(Long.valueOf(11), row1.getComponent_end());
    assertEquals("AC0001.1", row1.getComponent_acc());
    assertEquals("nonconentry", row1.getComponent_id());
    assertEquals("O", row1.getComponent_type_id());
    assertNull(row1.getGap_length());
    assertNull(row1.getGap_type());
    assertNull(row1.getLinkageevidence());
    assertFalse(row1.isGap());
    assertEquals("+", row1.getOrientation());
    assertEquals(Integer.valueOf(1), row1.getPart_number());

    // second
    assertEquals("nonconentry", row2.getObject());
    assertEquals("AC0001.1", row2.getObject_acc());
    assertEquals(Long.valueOf(12), row2.getObject_beg());
    assertEquals(Long.valueOf(21), row2.getObject_end());
    assertNull(row2.getComponent_beg());
    assertNull(row2.getComponent_end());
    assertNull(row2.getComponent_acc());
    assertNull(row2.getComponent_id());
    assertEquals("N", row2.getComponent_type_id());
    assertEquals(Long.valueOf(10), row2.getGap_length());
    assertEquals("scaffold", row2.getGap_type());
    List<String> linkage_evidences = new ArrayList<String>();
    linkage_evidences.add("paired-ends");
    assertEquals(linkage_evidences, row2.getLinkageevidence());
    assertTrue(row2.isGap());
    assertNull(row2.getOrientation());
    assertEquals(Integer.valueOf(2), row2.getPart_number());
    assertTrue(row2.isGap());

    // third
    assertEquals("nonconentry", row3.getObject());
    assertEquals("AC0001.1", row3.getObject_acc());
    assertEquals(Long.valueOf(22), row3.getObject_beg());
    assertEquals(Long.valueOf(28), row3.getObject_end());
    assertEquals(Long.valueOf(22), row3.getComponent_beg());
    assertEquals(Long.valueOf(28), row3.getComponent_end());
    assertEquals("AC0001.1", row3.getComponent_acc());
    assertEquals("nonconentry", row3.getComponent_id());
    assertEquals("O", row3.getComponent_type_id());
    assertNull(row3.getGap_length());
    assertNull(row3.getGap_type());
    assertNull(row3.getLinkageevidence());
    assertFalse(row3.isGap());
    assertEquals("+", row3.getOrientation());
    assertEquals(Integer.valueOf(3), row3.getPart_number());
    assertFalse(row3.isGap());

    // four
    assertEquals("nonconentry", row4.getObject());
    assertEquals("AC0001.1", row4.getObject_acc());
    assertEquals(Long.valueOf(29), row4.getObject_beg());
    assertEquals(Long.valueOf(35), row4.getObject_end());
    assertNull(row4.getComponent_beg());
    assertNull(row4.getComponent_end());
    assertNull(row4.getComponent_acc());
    assertNull(row4.getComponent_id());
    assertEquals("N", row4.getComponent_type_id());
    assertEquals(Long.valueOf(7), row4.getGap_length());
    assertEquals("scaffold", row4.getGap_type());
    assertEquals(linkage_evidences, row4.getLinkageevidence());
    assertNull(row4.getOrientation());
    assertEquals(Integer.valueOf(4), row4.getPart_number());
    assertTrue(row4.isGap());

    // five
    assertEquals("nonconentry", row5.getObject());
    assertEquals("AC0001.1", row5.getObject_acc());
    assertEquals(Long.valueOf(36), row5.getObject_beg());
    assertEquals(Long.valueOf(39), row5.getObject_end());
    assertEquals(Long.valueOf(36), row5.getComponent_beg());
    assertEquals(Long.valueOf(39), row5.getComponent_end());
    assertEquals("AC0001.1", row5.getComponent_acc());
    assertEquals("nonconentry", row5.getComponent_id());
    assertEquals("O", row5.getComponent_type_id());
    assertNull(row5.getGap_length());
    assertNull(row5.getGap_type());
    assertNull(row5.getLinkageevidence());
    assertFalse(row5.isGap());
    assertEquals("+", row5.getOrientation());
    assertEquals(Integer.valueOf(5), row5.getPart_number());
    assertFalse(row5.isGap());
  }
}
