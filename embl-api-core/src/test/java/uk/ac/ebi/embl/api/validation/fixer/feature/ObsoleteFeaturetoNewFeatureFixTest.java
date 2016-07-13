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

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ObsoleteFeaturetoNewFeatureFixTest
{

	private Feature feature;
	private ObsoleteFeaturetoNewFeatureFix check;
	private QualifierFactory qualifierFactory;

	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
		FeatureFactory featureFactory = new FeatureFactory();
		feature = featureFactory.createFeature("feature");
		DataRow dataRow1 = new DataRow("attenuator","regulatory","regulatory_class","attenuator");
		DataRow dataRow2 = new DataRow("CAAT_signal","regulatory","regulatory_class","CAAT_signal");
		DataRow dataRow3 = new DataRow("enhancer","regulatory","regulatory_class","enhancer");
		DataRow dataRow4 = new DataRow("GC_signal","regulatory","regulatory_class","GC_signal");
		DataRow dataRow5 = new DataRow("-10_signal","regulatory","regulatory_class","minus_10_signal");
		DataRow dataRow6 = new DataRow("LTR","repeat_region","rtp_type","long_terminal_repeat");
		DataSet obsoleteFeaturedataSet = new DataSet();
		obsoleteFeaturedataSet.addRow(dataRow1);
		obsoleteFeaturedataSet.addRow(dataRow2);
		obsoleteFeaturedataSet.addRow(dataRow3);
		obsoleteFeaturedataSet.addRow(dataRow4);
		obsoleteFeaturedataSet.addRow(dataRow5);
		obsoleteFeaturedataSet.addRow(dataRow6);
		qualifierFactory = new QualifierFactory();
		check = new ObsoleteFeaturetoNewFeatureFix(obsoleteFeaturedataSet);
	}

	@Test
	public void testCheck_NoFeature()
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_withOboleteFeature1()
	{
		feature.setName("attenuator");
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count("ObsoleteFeaturetoFeatureFix_1", Severity.FIX));
		assertEquals(Feature.REGULATORY_FEATURE_NAME, feature.getName());
		assertEquals("attenuator", feature.getSingleQualifierValue(Qualifier.REGULATORY_CLASS_QUALIFIER_NAME));
	}

	@Test
	public void testCheck_withOboleteFeature2()
	{
		feature.setName("-10_signal");
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count("ObsoleteFeaturetoFeatureFix_1", Severity.FIX));
		assertEquals(Feature.REGULATORY_FEATURE_NAME, feature.getName());
		assertEquals("minus_10_signal", feature.getSingleQualifierValue(Qualifier.REGULATORY_CLASS_QUALIFIER_NAME));
	}

	@Test
	public void testCheck_withValidFeature()
	{
		feature.setName(Feature.CDS_FEATURE_NAME);
		ValidationResult result = check.check(feature);
		assertEquals(0, result.count("ObsoleteFeaturetoFeatureFix_1", Severity.FIX));
		assertEquals(Feature.CDS_FEATURE_NAME, feature.getName());
	}

	@Test
	public void testCheck_obsoleteFeatureQualifier1()
	{
		feature.setName("attenuator");
		feature.addQualifier("allele");
		feature.addQualifier("bound_moiety");
		feature.addQualifier("citation");
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count("ObsoleteFeaturetoFeatureFix_1", Severity.FIX));
		assertEquals(Feature.REGULATORY_FEATURE_NAME, feature.getName());
		assertEquals("attenuator", feature.getSingleQualifierValue(Qualifier.REGULATORY_CLASS_QUALIFIER_NAME));
		assertEquals(4, feature.getQualifiers().size());
		assertEquals(true, feature.getQualifiers().contains(qualifierFactory.createQualifier("allele")));
		assertEquals(true, feature.getQualifiers().contains(qualifierFactory.createQualifier("bound_moiety")));
		assertEquals(true, feature.getQualifiers().contains(qualifierFactory.createQualifier("citation")));

	}
	
	@Test
	public void testCheck_obsoleteFeatureQualifier2()
	{
		feature.setName("LTR");
		feature.addQualifier("allele");
		feature.addQualifier("bound_moiety");
		feature.addQualifier("citation");
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count("ObsoleteFeaturetoFeatureFix_1", Severity.FIX));
		assertEquals(Feature.REPEAT_REGION, feature.getName());
		assertEquals("long_terminal_repeat", feature.getSingleQualifierValue("rtp_type"));
		assertEquals(4, feature.getQualifiers().size());
		assertEquals(true, feature.getQualifiers().contains(qualifierFactory.createQualifier("allele")));
		assertEquals(true, feature.getQualifiers().contains(qualifierFactory.createQualifier("bound_moiety")));
		assertEquals(true, feature.getQualifiers().contains(qualifierFactory.createQualifier("citation")));

	}

}
