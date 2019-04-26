/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.api.validation.fixer.entry;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.ContigSequenceInfo;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Gap;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryContigsCheck;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ContoAGPFixTest {

	private Entry entry,entry1;
	private ContoAGPFix check;
	public EntryFactory entryFactory;
	public LocationFactory locationFactory;
	public SequenceFactory sequenceFactory;
	public FeatureFactory featureFactory;
	public EntryDAOUtils entryDAOUtils;
	public EmblEntryValidationPlanProperty planProperty;

	@Before
	public void setUp() {
		ValidationMessageManager
				.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
		entryFactory = new EntryFactory();
		featureFactory=new FeatureFactory();
		locationFactory=new LocationFactory();
		entry = entryFactory.createEntry();
		entry1= entryFactory.createEntry();
		SequenceFactory sequenceFactory=new SequenceFactory();
		entry.setSequence(sequenceFactory.createSequence());
		check = new ContoAGPFix();
		entryDAOUtils = createMock(EntryDAOUtils.class);
		planProperty=new EmblEntryValidationPlanProperty();
		entry = entryFactory.createEntry();
		Feature assemblyGapFeature=featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
		Order<Location> order = new Order<Location>();
        order.addLocation(locationFactory.createLocalRange(12l,21l));
		assemblyGapFeature.setLocations(order);
		assemblyGapFeature.addQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME,"10");
		assemblyGapFeature.addQualifier(Qualifier.GAP_TYPE_QUALIFIER_NAME, "within scaffold");
		assemblyGapFeature.addQualifier(Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME, "paired-ends");
		locationFactory = new LocationFactory();
		sequenceFactory=new SequenceFactory();
		entry.setSequence(sequenceFactory.createSequence());
		RemoteRange remoteRange1 = locationFactory.createRemoteRange("A00001", 1, 10L, 20L);
		RemoteRange remoteRange2 = locationFactory.createRemoteRange("A00002", 1, 10L, 20L);
		Gap gap1 = locationFactory.createGap(10);
		entry.getSequence().addContig(remoteRange1);
		entry.getSequence().addContig(gap1);
		entry.getSequence().addContig(remoteRange2);
		entry.addFeature(assemblyGapFeature);
		entry.setSubmitterAccession("conentry");
		entry.setPrimaryAccession("AC0001");
		entry.getSequence().setVersion(1);
	}

	@Test
	public void testCheck_NoEntry() throws ValidationEngineException {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoContigs() throws ValidationEngineException {
		ValidationResult validationResult = check.check(entry1);
		assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
	}
	
	@Test
	public void testCheck_NoCONdataclass() throws ValidationEngineException {
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
	}

	@Test
	public void testCheck_withcomponentAccession() throws ValidationEngineException, SQLException 
	{
		Entry firstEntry = entryFactory.createEntry();
		Entry secondEntry= entryFactory.createEntry();
		firstEntry.setDataClass(Entry.WGS_DATACLASS);
		firstEntry.setSubmitterAccession("first");
		secondEntry.setDataClass(Entry.WGS_DATACLASS);
		secondEntry.setSubmitterAccession("second");
		planProperty.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
		check.setEmblEntryValidationPlanProperty(planProperty);
		expect(entryDAOUtils.getEntryInfo("A00001")).andReturn(firstEntry);
		expect(entryDAOUtils.getEntryInfo("A00002")).andReturn(secondEntry);
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
		check.check(entry);
		assertEquals(3, entry.getSequence().getAgpRows().size());
		List<AgpRow> agpRows=entry.getSequence().getAgpRows();
		AgpRow row1=agpRows.get(0);
		AgpRow row2=agpRows.get(1);
		AgpRow row3=agpRows.get(2);
		//first
		assertEquals("conentry",row1.getObject());
		assertEquals("AC0001.1",row1.getObject_acc());
		assertEquals(new Long(1),row1.getObject_beg());
		assertEquals(new Long(11),row1.getObject_end());
		assertEquals(new Long(10),row1.getComponent_beg());
		assertEquals(new Long(20),row1.getComponent_end());
		assertEquals("A00001.1",row1.getComponent_acc());
		assertEquals("first",row1.getComponent_id());
		assertEquals("W",row1.getComponent_type_id());
		assertEquals(null,row1.getGap_length());
		assertEquals(null,row1.getGap_type());
		assertEquals(null,row1.getLinkageevidence());
		assertEquals(false,row1.isGap());
		assertEquals("+",row1.getOrientation());
		assertEquals(new Integer(1),row1.getPart_number());
		//second
		assertEquals("conentry",row2.getObject());
		assertEquals("AC0001.1",row2.getObject_acc());
		assertEquals(new Long(12),row2.getObject_beg());
		assertEquals(new Long(21),row2.getObject_end());
		assertEquals(null,row2.getComponent_beg());
		assertEquals(null,row2.getComponent_end());
		assertEquals(null,row2.getComponent_acc());
		assertEquals(null,row2.getComponent_id());
		assertEquals("N",row2.getComponent_type_id());
		assertEquals(new Long(10),row2.getGap_length());
		assertEquals("scaffold",row2.getGap_type());
		List<String> linkage_evidences= new ArrayList<String>();
		linkage_evidences.add("paired-ends");
		assertEquals(linkage_evidences,row2.getLinkageevidence());
		assertEquals(true,row2.isGap());
		assertEquals(null,row2.getOrientation());
		assertEquals(new Integer(2),row2.getPart_number());
		//third
		assertEquals("conentry",row3.getObject());
		assertEquals("AC0001.1",row3.getObject_acc());
		assertEquals(new Long(22),row3.getObject_beg());
		assertEquals(new Long(32),row3.getObject_end());
		assertEquals(new Long(10),row3.getComponent_beg());
		assertEquals(new Long(20),row3.getComponent_end());
		assertEquals("A00002.1",row3.getComponent_acc());
		assertEquals("second",row3.getComponent_id());
		assertEquals("W",row3.getComponent_type_id());
		assertEquals(null,row3.getGap_length());
		assertEquals(null,row3.getGap_type());
		assertEquals(null,row3.getLinkageevidence());
		assertEquals(false,row3.isGap());
		assertEquals("+",row3.getOrientation());
		assertEquals(new Integer(3),row3.getPart_number());
   }

	@Test(expected=ValidationEngineException.class)
	public void testCheck_withinvalidComponent() throws ValidationEngineException, SQLException {
		planProperty.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
		check.setEmblEntryValidationPlanProperty(planProperty);
		expect(entryDAOUtils.getEntryInfo("A00001")).andReturn(null);
		expect(entryDAOUtils.getEntryInfo("A00002")).andReturn(null);
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
		check.check(entry);
	}
	@Test
	public void testCheck_featureOrder() throws ValidationEngineException {
		Order<Location> order1 = new Order<Location>();
	    order1.addLocation(locationFactory.createLocalRange(14l,20l));
	    Order<Location> order2 = new Order<Location>();
	    order2.addLocation(locationFactory.createLocalRange(13l,15l));
	    List<Feature> features= new ArrayList<Feature>();
		Feature feature1= (new FeatureFactory()).createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
		feature1.setLocations(order1);
		Feature feature2= (new FeatureFactory()).createFeature(Feature.CDS_FEATURE_NAME);
		feature2.setLocations(order2);
		features.add(feature1);
		features.add(feature2);
		assertEquals(Feature.ASSEMBLY_GAP_FEATURE_NAME,features.get(0).getName());
		assertEquals(Feature.CDS_FEATURE_NAME,features.get(1).getName());
		features=  check.getSortedAssemblyGapFeatures(features);
		assertEquals(Feature.CDS_FEATURE_NAME,features.get(0).getName());
		assertEquals(Feature.ASSEMBLY_GAP_FEATURE_NAME,features.get(1).getName());

	}

	
}
