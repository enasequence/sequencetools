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
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.helper.DataSetHelper;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.check.feature.QualifierCheck;
import uk.ac.ebi.embl.api.validation.fixer.feature.ObsoleteFeatureFix;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FeatureRenameFixTest {

	  private Feature feature;
	    private FeatureRenameFix check;

	    @Before
	    public void setUp() {
	    	ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
	        FeatureFactory featureFactory = new FeatureFactory();

	        feature = featureFactory.createFeature("feature");
	    	DataRow dataRow1 = new DataRow("misc_feature","feature1");
	        DataRow dataRow2 = new DataRow("conflict","feature2");
	        DataRow dataRow3 = new DataRow("repeat_region","feature3");

	        DataSetHelper.createAndAdd(FileName.FEATURE_RENAME, dataRow1,dataRow2,dataRow3);
	        check = new FeatureRenameFix();
	    }
	
	    
	@Test
	public void testCheck_NoFeature() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_validFeatureName() {
		feature.setName("misc_feature");
		 ValidationResult validationResult = check.check(feature);
       	assertEquals(1, validationResult.count("FeatureRenameFix", Severity.FIX));
	}
	
	@Test
	public void testCheck_invalidFeatureName() {
		feature.setName("david");
		assertTrue(check.check(feature).isValid());
		}
	
	@Test
	public void testCheck_repeat_region_with_mobile_element() {
		feature.setName("repeat_region");
		feature.addQualifier(Qualifier.MOBILE_ELEMENT_NAME);
			 ValidationResult validationResult = check.check(feature);
	     assertEquals(1, validationResult.count("FeatureRenameFix", Severity.FIX));
	     
		}
	@Test
	public void testCheck_repeat_region_without_mobile_element() {
		feature.setName("repeat_region");
		feature.addQualifier("david");
		 ValidationResult validationResult = check.check(feature);
	     assertEquals(0, validationResult.count("FeatureRenameFix", Severity.FIX));
	     assertTrue(validationResult.isValid());
		}
	
	@Test
	public void testCheck_conflictFeature() {
		 feature.setName(Feature.CONFLICT_FEATURE_NAME);
		 ValidationResult validationResult = check.check(feature);
	     assertEquals(1, validationResult.count("FeatureRenameFix", Severity.FIX));
	     
		}
	
	
}
