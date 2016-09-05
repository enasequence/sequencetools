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
import uk.ac.ebi.embl.api.entry.location.Gap;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
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

public class AgptoConFixTest {

	private Entry entry;
	private AgptoConFix check;
	public EntryFactory entryFactory;
	public EntryDAOUtils entryDAOUtils;
	public EmblEntryValidationPlanProperty planProperty;
	public List<String> linkageEvidences;

	@Before
	public void setUp() {
		ValidationMessageManager
				.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
		entryFactory = new EntryFactory();
		entry = entryFactory.createEntry();
		SequenceFactory sequenceFactory=new SequenceFactory();
		 entry.setSequence(sequenceFactory.createSequence());
		check = new AgptoConFix();
		entryDAOUtils = createMock(EntryDAOUtils.class);
		planProperty=new EmblEntryValidationPlanProperty();
		linkageEvidences= new ArrayList<String>();
		linkageEvidences.add("paired-ends");
		
	}

	@Test
	public void testCheck_NoEntry() throws ValidationEngineException {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoAgpRows() throws ValidationEngineException {
		ValidationResult validationResult = check.check(entry);
		assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
	}

	@Test
	public void testCheck_withnocomponentAccession() throws ValidationEngineException, SQLException {

		AgpRow Componentrow=new AgpRow();
		AgpRow gaprow=new AgpRow();
		Componentrow.setObject("IWGSC_CSS_6DL_scaff_3330716");
		Componentrow.setObject_beg(1l);
		Componentrow.setObject_end(330l);
		Componentrow.setPart_number(1);
		Componentrow.setComponent_type_id("W");
		Componentrow.setComponent_beg(1l);
		Componentrow.setComponent_end(330l);
		Componentrow.setOrientation("+");
		Componentrow.setComponent_id("IWGSC_CSS_6DL_scaff_3330715");
		gaprow.setObject("IWGSC_CSS_6DL_scaff_3330716");
		gaprow.setObject_beg(331);
		gaprow.setObject_end(354l);
		gaprow.setPart_number(2);
		gaprow.setComponent_type_id("N");
		gaprow.setGap_length(24l);
		gaprow.setGap_type("scaffold");
		gaprow.setLinkageevidence(linkageEvidences);
		entry.addAgpRow(Componentrow);
		entry.addAgpRow(gaprow);
		planProperty.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
		planProperty.fileType.set(FileType.AGP);
		planProperty.analysis_id.set("ERZ00001");
		check.setEmblEntryValidationPlanProperty(planProperty);
		expect(entryDAOUtils.getSequenceInfoBasedOnEntryName("IWGSC_CSS_6DL_scaff_3330715", "ERZ00001",2)).andReturn(null);
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
		check.check(entry);
		List<Feature> assemblyGap_features=SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry);
		assertEquals(0, assemblyGap_features.size());
		assertEquals(0,entry.getSequence().getContigs().size());
		//sequenceLength check
		assertEquals(0, entry.getSequence().getLength());
	}

	@Test
	public void testCheck_withComponentAccession() throws ValidationEngineException, SQLException {

		AgpRow Componentrow=new AgpRow();
		AgpRow gaprow=new AgpRow();
		Componentrow.setObject("IWGSC_CSS_6DL_scaff_3330716");
		Componentrow.setObject_beg(1l);
		Componentrow.setObject_end(330l);
		Componentrow.setPart_number(1);
		Componentrow.setComponent_type_id("W");
		Componentrow.setComponent_beg(1l);
		Componentrow.setComponent_end(330l);
		Componentrow.setComponent_acc("CDRE01000271.1");
		Componentrow.setOrientation("+");
		Componentrow.setComponent_id("IWGSC_CSS_6DL_scaff_3330715");

		gaprow.setObject("IWGSC_CSS_6DL_scaff_3330716");
		gaprow.setObject_beg(331);
		gaprow.setObject_end(354l);
		gaprow.setPart_number(2);
		gaprow.setComponent_type_id("N");
		gaprow.setGap_length(24l);
		gaprow.setGap_type("scaffold");
		gaprow.setLinkageevidence(linkageEvidences);
		
		entry.addAgpRow(Componentrow);
		entry.addAgpRow(gaprow);
		planProperty.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
		planProperty.fileType.set(FileType.AGP);
		planProperty.analysis_id.set("ERZ00001");
		check.setEmblEntryValidationPlanProperty(planProperty);
		ContigSequenceInfo contigSequenceInfo=new ContigSequenceInfo();
		contigSequenceInfo.setPrimaryAccession("CDRE01000271");
		contigSequenceInfo.setSequenceVersion(1);
		contigSequenceInfo.setSequenceLength(331);
		expect(entryDAOUtils.getSequenceInfoBasedOnEntryName("IWGSC_CSS_6DL_scaff_3330715", "ERZ00001",2)).andReturn(contigSequenceInfo);
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
	    check.check(entry);
		assertEquals(2,entry.getSequence().getContigs().size());

		//contig1
		assertTrue(entry.getSequence().getContigs().get(0) instanceof RemoteLocation);
		assertTrue(!entry.getSequence().getContigs().get(0).isComplement());
		Long actualBeginPosition1=entry.getSequence().getContigs().get(0).getBeginPosition();
		Long expectedBeginPosition1=1l;
		Long actualEndPosition1=entry.getSequence().getContigs().get(0).getEndPosition();
		Long expectedEndPosition1=330l;
		RemoteLocation remoteLocation=(RemoteLocation) entry.getSequence().getContigs().get(0);
		String actualAccessionwithVersion=remoteLocation.getAccession()+"."+remoteLocation.getVersion();
		String expectedAccessionwithVersion="CDRE01000271.1";
		assertEquals(expectedBeginPosition1,actualBeginPosition1);
		assertEquals(expectedEndPosition1,actualEndPosition1);
		assertEquals(expectedAccessionwithVersion,actualAccessionwithVersion);

        //contig2
		assertTrue(entry.getSequence().getContigs().get(1) instanceof Gap);
		assertEquals(24, entry.getSequence().getContigs().get(1).getLength());
		List<Feature> assemblyGap_features=SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry);
		assertEquals(1, assemblyGap_features.size());
		Long beginPosition=assemblyGap_features.get(0).getLocations().getMinPosition();
		Long expectedBegin=331l;
		Long endPosition=assemblyGap_features.get(0).getLocations().getMaxPosition();
		Long expectedEnd=354l;
		String actualGapType=assemblyGap_features.get(0).getSingleQualifierValue(Qualifier.GAP_TYPE_QUALIFIER_NAME);
		String expectedGapType="within scaffold";
		String actualLinkedEvidence=assemblyGap_features.get(0).getSingleQualifierValue(Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME);
		String expectedLinkageEvidence="paired-ends";
		String actualEstimatedLength=assemblyGap_features.get(0).getSingleQualifierValue(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME);
		String expectedEstimatesLength="24";
		assertEquals(expectedBegin,beginPosition);
		assertEquals(expectedEnd,endPosition);
		assertEquals(expectedGapType, actualGapType);
		assertEquals(expectedLinkageEvidence, actualLinkedEvidence);
		assertEquals(expectedEstimatesLength, actualEstimatedLength);
       //entry sequence length check
		assertEquals(354, entry.getSequence().getLength());

	}
	
	@Test
	public void testCheck_withComponentAccessionNegOrientation() throws ValidationEngineException, SQLException {

		AgpRow Componentrow=new AgpRow();
		AgpRow gaprow=new AgpRow();
		Componentrow.setObject("IWGSC_CSS_6DL_scaff_3330716");
		Componentrow.setObject_beg(1l);
		Componentrow.setObject_end(330l);
		Componentrow.setPart_number(1);
		Componentrow.setComponent_type_id("W");
		Componentrow.setComponent_beg(1l);
		Componentrow.setComponent_end(330l);
		Componentrow.setComponent_id("IWGSC_CSS_6DL_scaff_3330715");
		Componentrow.setComponent_acc("CDRE01000271.1");
		Componentrow.setOrientation("-");

		gaprow.setObject("IWGSC_CSS_6DL_scaff_3330716");
		gaprow.setObject_beg(331);
		gaprow.setObject_end(354l);
		gaprow.setPart_number(2);
		gaprow.setComponent_type_id("N");
		gaprow.setGap_length(24l);
		gaprow.setGap_type("scaffold");
		gaprow.setLinkageevidence(linkageEvidences);
		
		entry.addAgpRow(Componentrow);
		entry.addAgpRow(gaprow);
		planProperty.validationScope.set(ValidationScope.ASSEMBLY_CHROMOSOME);
		planProperty.fileType.set(FileType.AGP);
		planProperty.analysis_id.set("ERZ00001");
		check.setEmblEntryValidationPlanProperty(planProperty);
		ContigSequenceInfo contigSequenceInfo=new ContigSequenceInfo();
		contigSequenceInfo.setPrimaryAccession("CDRE01000271");
		contigSequenceInfo.setSequenceVersion(1);
		contigSequenceInfo.setSequenceLength(331);
		expect(entryDAOUtils.getSequenceInfoBasedOnEntryName("IWGSC_CSS_6DL_scaff_3330715", "ERZ00001",2)).andReturn(contigSequenceInfo);
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
	    check.check(entry);
		assertEquals(2,entry.getSequence().getContigs().size());

		//contig1
		assertTrue(entry.getSequence().getContigs().get(0) instanceof RemoteLocation);
		assertTrue(entry.getSequence().getContigs().get(0).isComplement());
		Long actualBeginPosition1=entry.getSequence().getContigs().get(0).getBeginPosition();
		Long expectedBeginPosition1=1l;
		Long actualEndPosition1=entry.getSequence().getContigs().get(0).getEndPosition();
		Long expectedEndPosition1=330l;
		RemoteLocation remoteLocation=(RemoteLocation) entry.getSequence().getContigs().get(0);
		String actualAccessionwithVersion=remoteLocation.getAccession()+"."+remoteLocation.getVersion();
		String expectedAccessionwithVersion="CDRE01000271.1";
		assertEquals(expectedBeginPosition1,actualBeginPosition1);
		assertEquals(expectedEndPosition1,actualEndPosition1);
		assertEquals(expectedAccessionwithVersion,actualAccessionwithVersion);

        //contig2
		assertTrue(entry.getSequence().getContigs().get(1) instanceof Gap);
		assertEquals(24, entry.getSequence().getContigs().get(1).getLength());
		List<Feature> assemblyGap_features=SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry);
		assertEquals(1, assemblyGap_features.size());
		Long beginPosition=assemblyGap_features.get(0).getLocations().getMinPosition();
		Long expectedBegin=331l;
		Long endPosition=assemblyGap_features.get(0).getLocations().getMaxPosition();
		Long expectedEnd=354l;
		String actualGapType=assemblyGap_features.get(0).getSingleQualifierValue(Qualifier.GAP_TYPE_QUALIFIER_NAME);
		String expectedGapType="within scaffold";
		String actualLinkedEvidence=assemblyGap_features.get(0).getSingleQualifierValue(Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME);
		String expectedLinkageEvidence="paired-ends";
		String actualEstimatedLength=assemblyGap_features.get(0).getSingleQualifierValue(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME);
		String expectedEstimatesLength="24";
		assertEquals(expectedBegin,beginPosition);
		assertEquals(expectedEnd,endPosition);
		assertEquals(expectedGapType, actualGapType);
		assertEquals(expectedLinkageEvidence, actualLinkedEvidence);
		assertEquals(expectedEstimatesLength, actualEstimatedLength);
		 //entry sequence length check
		assertEquals(354, entry.getSequence().getLength());
	}
}
