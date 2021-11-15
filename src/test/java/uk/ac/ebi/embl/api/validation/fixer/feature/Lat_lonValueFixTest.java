/*
 * # Copyright 2012-2012 EMBL-EBI, Hinxton outstation
 *
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
 *
# http://www.apache.org/licenses/LICENSE-2.0
 *
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
 */

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

public class Lat_lonValueFixTest
{

	private Feature feature;
	private Qualifier qualifier;
	private Lat_lonValueFix check;

	@Before
	public void setUp()
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
		FeatureFactory featureFactory = new FeatureFactory();
		QualifierFactory qualifierFactory = new QualifierFactory();
		feature = featureFactory.createFeature(Feature.SOURCE_FEATURE_NAME);
		qualifier = qualifierFactory.createQualifier(Qualifier.LAT_LON_QUALIFIER_NAME);
		check = new Lat_lonValueFix();
	}

	@Test
	public void testCheck_NoFeature()
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_noLat_lon()
	{
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count("Lat_lonValueFix", Severity.FIX));
	}

	@Test
	public void testCheck_Lat_lon_withInvalidValue()
	{
		qualifier.setValue("32.0103N 35.297 E");
		feature.addQualifier(qualifier);
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count("Lat_lonValueFix", Severity.FIX));
	}

	@Test
	public void testCheck_Lat_lon_withValidValueinLimit()
	{
		qualifier.setValue("32.0103 N 35.297 E");
		feature.addQualifier(qualifier);
		ValidationResult validationResult = check.check(feature);
		assertEquals(0, validationResult.count("Lat_lonValueFix", Severity.FIX));
	}

	@Test
	public void testCheck_lat_lonValidValueExceedLimit()
	{
		qualifier.setValue("32.010334545 N 35.297566774 E");
		feature.addQualifier(qualifier);
		ValidationResult validationResult = check.check(feature);
		assertEquals(1, validationResult.count("Lat_lonValueFix", Severity.FIX));
		assertEquals(qualifier.getValue(), "32.01033454 N 35.29756677 E");
	}


	@Test
	public void testLatLonValue()
	{
		assertEquals(new Double("1"), Lat_lonValueFix.getLatLonValue("1"));
		assertEquals(new Double("1.5"), Lat_lonValueFix.getLatLonValue("1.5"));
		assertEquals(new Double("1.55"), Lat_lonValueFix.getLatLonValue("1.55"));
		assertEquals(new Double("1.555"), Lat_lonValueFix.getLatLonValue("1.555"));
		assertEquals(new Double("1.5555"), Lat_lonValueFix.getLatLonValue("1.5555"));
		assertEquals(new Double("1.55555"), Lat_lonValueFix.getLatLonValue("1.55555"));
		assertEquals(new Double("1.555555"), Lat_lonValueFix.getLatLonValue("1.555555"));
		assertEquals(new Double("1.5555555"), Lat_lonValueFix.getLatLonValue("1.5555555"));
		assertEquals(new Double("1.55555555"), Lat_lonValueFix.getLatLonValue("1.55555555"));
		// Round down
		assertEquals(new Double("1.55555555"), Lat_lonValueFix.getLatLonValue("1.555555551"));
		assertEquals(new Double("1.55555555"), Lat_lonValueFix.getLatLonValue("1.555555555"));
		assertEquals(new Double("1.55555555"), Lat_lonValueFix.getLatLonValue("1.555555559"));
	}
}
