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
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExperimentQualifierFixTest
{
	
	private Feature feature;
	private Qualifier experimentQualifier1;
	private Qualifier experimentQualifier2;
	private Qualifier qualifier2;
	private ExperimentQualifierFix check;
	
	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
		FeatureFactory featureFactory = new FeatureFactory();
		QualifierFactory qualifierFactory = new QualifierFactory();
		feature = featureFactory.createFeature("feature");
		experimentQualifier1 = qualifierFactory.createQualifier(Qualifier.EXPERIMENT_QUALIFIER_NAME);
		experimentQualifier2 = qualifierFactory.createQualifier(Qualifier.EXPERIMENT_QUALIFIER_NAME);
		qualifier2 = qualifierFactory.createQualifier(Qualifier.EC_NUMBER_QUALIFIER_NAME);
		check = new ExperimentQualifierFix();
	}
	
	@Test
	public void testCheck_NoFeature()
	{
		assertTrue(check.check(null).isValid());
	}
	
	@Test
	public void testCheck_FeaturewithNoQualifiers()
	{
		assertTrue(check.check(feature).isValid());
	}
	
	@Test
	public void testCheck_FeaturewithNoExperiment()
	{
		feature.addQualifier(qualifier2);
		assertTrue(check.check(feature).isValid());
	}
	
	@Test
	public void testCheck_FeaturewithExperimentvalidValue()
	{
		experimentQualifier1.setValue("RNA sequencing");
		feature.addQualifier(experimentQualifier1);
		ValidationResult result = check.check(feature);
		assertEquals(0, result.count("ExperimentQualifierFix-1", Severity.FIX));
	}
	
	@Test
	public void testCheck_FeaturewithExperimentoldValue()
	{
		experimentQualifier1.setValue("experimental evidence, no additional details recorded");
		feature.addQualifier(experimentQualifier1);
		ValidationResult result = check.check(feature);
		assertEquals(0, result.count("ExperimentQualifierFix-1", Severity.FIX));
	}
	
	@Test
	public void testCheck_FeaturewithExperimentsoldValue()
	{
		experimentQualifier1.setValue("experimental evidence, no additional details recorded");
		experimentQualifier2.setValue("experimental evidence, no additional details recorded");
		feature.addQualifier(experimentQualifier1);
		feature.addQualifier(experimentQualifier2);
		ValidationResult result = check.check(feature);
		assertEquals(1, result.count("ExperimentQualifierFix-1", Severity.FIX));
	}
	
	@Test
	public void testCheck_FeaturewithExperiments()
	{
		experimentQualifier1.setValue("experimental evidence, no additional details recorded");
		experimentQualifier2.setValue("RNA sequencing");
		feature.addQualifier(experimentQualifier1);
		feature.addQualifier(experimentQualifier2);
		ValidationResult result = check.check(feature);
		assertEquals(1, result.count("ExperimentQualifierFix-1", Severity.FIX));
		assertEquals(1,feature.getQualifiers(Qualifier.EXPERIMENT_QUALIFIER_NAME).size());
	}
	
}
