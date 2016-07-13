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
package uk.ac.ebi.embl.api.validation.fixer.feature;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.cvtable.cv_fqual_value_fix_table;
import uk.ac.ebi.embl.api.validation.cvtable.cv_fqual_value_fix_table.cv_fqual_value_fix_record;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.fixer.feature.QualifierValueFix;

import java.sql.SQLException;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QualifierValueFixTest
{

	private Entry entry;
	private Feature feature;
	private FeatureFactory featureFactory;
	private QualifierValueFix check;
	private EntryDAOUtils entryDAOUtils;
	cv_fqual_value_fix_table cv_fqual_value_fix_table1;

	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		featureFactory = new FeatureFactory();
		entry = entryFactory.createEntry();
		entryDAOUtils=createMock(EntryDAOUtils.class);
		cv_fqual_value_fix_table1=new cv_fqual_value_fix_table();
		feature = featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
		check = new QualifierValueFix();
		check.setEntryDAOUtils(entryDAOUtils);
	}

	@Test
	public void testCheck_no_cv_fqual_fix_values()
	{
		ValidationResult result = check.check(feature);
		assertTrue(result.isValid());
	}

	@Test
	public void testCheck_no_feaure()
	{
		ValidationResult result = check.check(null);
		assertTrue(result.isValid());
	}

	@Test
	public void testCheck_with_cv_fqual_value_Qnamevalue_match() throws SQLException
	{
		cv_fqual_value_fix_record record1=cv_fqual_value_fix_table1.create_cv_fqual_value_fix_record();
		cv_fqual_value_fix_record record2=cv_fqual_value_fix_table1.create_cv_fqual_value_fix_record();
		feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, "3.4.99.14");
		record1.setFqualName(Qualifier.EC_NUMBER_QUALIFIER_NAME);
		record1.setRegex("3\\.4\\.99\\.14");
		record1.setValue("3\\.5\\.34\\.23");
		cv_fqual_value_fix_table1.add(record1);
		record2.setFqualName(Qualifier.EC_NUMBER_QUALIFIER_NAME);
		record2.setRegex("3\\.4\\.99\\.15");
		record2.setValue("3\\.5\\.34\\.23");
		cv_fqual_value_fix_table1.add(record2);
		expect(entryDAOUtils.get_cv_fqual_value_fix()).andReturn(cv_fqual_value_fix_table1);
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
		ValidationResult result = check.check(feature);
		assertTrue(result.isValid());
		assertEquals(1, result.getMessages().size());
	}

	@Test
	public void testCheck_with_cv_fqual_value_Qnamevalue_match_deleted() throws SQLException
	{
		cv_fqual_value_fix_record record1=cv_fqual_value_fix_table1.create_cv_fqual_value_fix_record();
		cv_fqual_value_fix_record record2=cv_fqual_value_fix_table1.create_cv_fqual_value_fix_record();
		feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, "3.4.99.14");
		record1.setFqualName(Qualifier.EC_NUMBER_QUALIFIER_NAME);
		record1.setRegex("3\\.4\\.99\\.14");
		record1.setValue("DELETED");
		cv_fqual_value_fix_table1.add(record1);
		record2.setFqualName(Qualifier.EC_NUMBER_QUALIFIER_NAME);
		record2.setRegex("3\\.4\\.99\\.15");
		record2.setValue("3\\.5\\.34\\.23");
		cv_fqual_value_fix_table1.add(record2);
		expect(entryDAOUtils.get_cv_fqual_value_fix()).andReturn(cv_fqual_value_fix_table1);
		replay(entryDAOUtils);
		ValidationResult result = check.check(feature);
		assertTrue(result.isValid());
		assertEquals(2, result.getMessages().size());
	}

	@Test
	public void testCheck_with_cv_fqual_value_fix_no_QValue_match() throws SQLException
	{
		cv_fqual_value_fix_record record1=cv_fqual_value_fix_table1.create_cv_fqual_value_fix_record();
		cv_fqual_value_fix_record record2=cv_fqual_value_fix_table1.create_cv_fqual_value_fix_record();
		feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, "3.4.99.14");
		record1.setFqualName(Qualifier.EC_NUMBER_QUALIFIER_NAME);
		record1.setRegex("3\\.4\\.9\\.14");
		record1.setValue("DELETED");
		cv_fqual_value_fix_table1.add(record1);
		record2.setFqualName(Qualifier.EC_NUMBER_QUALIFIER_NAME);
		record2.setRegex("3\\.4\\.9\\.15");
		record2.setValue("3\\.5\\.34\\.23");
		cv_fqual_value_fix_table1.add(record2);
		expect(entryDAOUtils.get_cv_fqual_value_fix()).andReturn(cv_fqual_value_fix_table1);
		replay(entryDAOUtils);
		ValidationResult result = check.check(feature);
		assertTrue(result.isValid());
		assertEquals(0, result.getMessages().size());
	}

	@Test
	public void testCheck_with_cv_fqual_value_fix_no_Qname_match() throws SQLException
	{
		cv_fqual_value_fix_record record1=cv_fqual_value_fix_table1.create_cv_fqual_value_fix_record();
		cv_fqual_value_fix_record record2=cv_fqual_value_fix_table1.create_cv_fqual_value_fix_record();
		feature.addQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME, "3.4.99.14");
		record1.setFqualName(Qualifier.ANTICODON_QUALIFIER_NAME);
		record1.setRegex("3\\.4\\.9\\.14");
		record1.setValue("DELETED");
		cv_fqual_value_fix_table1.add(record1);
		record2.setFqualName(Qualifier.ANTICODON_QUALIFIER_NAME);
		record2.setRegex("3\\.4\\.9\\.15");
		record2.setValue("3\\.5\\.34\\.23");
		cv_fqual_value_fix_table1.add(record2);
		expect(entryDAOUtils.get_cv_fqual_value_fix()).andReturn(cv_fqual_value_fix_table1);
		replay(entryDAOUtils);
		ValidationResult result = check.check(feature);
		assertTrue(result.isValid());
		assertEquals(0, result.getMessages().size());
	}

	
	@Test
	public void testCheck_ValueWithDoubleQuotes()
	{
		feature.addQualifier(Qualifier.NOTE_QUALIFIER_NAME,"Salmonel \"las\" enterica subsp.");
		ValidationResult result = check.check(feature);
		 assertEquals(1, result.count("QualifierValueFix_3", Severity.FIX));
		
	}
	@Test
	public void testCheck_altitudeValue()
	{
		feature.addQualifier(Qualifier.ALTITUDE_QUALIFIER_NAME,"-3283m.");
		ValidationResult result = check.check(feature);
		 assertEquals(1, result.count("QualifierValueFix_1", Severity.FIX));
		 assertEquals("-3283m", feature.getSingleQualifierValue(Qualifier.ALTITUDE_QUALIFIER_NAME));

		
	}

}
