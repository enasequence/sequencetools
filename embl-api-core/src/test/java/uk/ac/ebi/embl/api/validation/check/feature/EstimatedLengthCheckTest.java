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
package uk.ac.ebi.embl.api.validation.check.feature;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.*;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: lbower
 * Date: 26-Jan-2009
 * Time: 10:50:52
 * To change this template use File | Settings | File Templates.
 */
public class EstimatedLengthCheckTest {

    private Feature feature,feature1;
    private EstimatedLengthCheck check;

    @Before
    public void setUp() {
        FeatureFactory featureFactory = new FeatureFactory();
        feature = featureFactory.createFeature(Feature.GAP_FEATURE_NAME);
        feature1 = featureFactory.createFeature(Feature.ASSEMBLY_GAP_FEATURE_NAME);
        LocationFactory locationFactory = new LocationFactory();
        Order<Location> locationOrder = new Order<Location>();
        locationOrder.addLocation(locationFactory.createLocalRange(1l, 25l));
        feature.setLocations(locationOrder);
        feature1.setLocations(locationOrder);

        check = new EstimatedLengthCheck();
    }

    @Test
    public void testCheck_NoFeature() {
        assertTrue(check.check(null).isValid());
    }

    @Test
    public void testCheck_NoQualifiers() {
        assertTrue(check.check(feature).isValid());
    }

    @Test
    public void testCheck_InvalidFormat() {
        QualifierFactory qualifierFactory = new QualifierFactory();
        feature.addQualifier(
                qualifierFactory.createQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME, "not a number"));
        feature1.addQualifier(
                qualifierFactory.createQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME, "not a number"));
        ValidationResult validationResult = check.check(feature);
        assertEquals(1, validationResult.count("EstimatedLengthCheck-3", Severity.ERROR));
        ValidationResult validationResult1= check.check(feature1);
        assertEquals(1, validationResult1.count("EstimatedLengthCheck-3", Severity.ERROR));
    }

    @Test
    public void testCheck_WrongLength() {
        QualifierFactory qualifierFactory = new QualifierFactory();
        feature.addQualifier(qualifierFactory.createQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME, "150"));//the actual length of this feature is 25
        ValidationResult validationResult = check.check(feature);
        assertTrue(!validationResult.isValid());
        assertEquals(1, validationResult.count("EstimatedLengthCheck-1", Severity.ERROR));
        feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME, "150"));//the actual length of this feature is 25
        ValidationResult validationResult1 = check.check(feature1);
        assertTrue(!validationResult1.isValid());
        assertEquals(1, validationResult1.count("EstimatedLengthCheck-1", Severity.ERROR));
    }

    @Test
    public void testCheck_LengthFine() {
        QualifierFactory qualifierFactory = new QualifierFactory();
        feature.addQualifier(qualifierFactory.createQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME, "25"));//the actual length of this feature is 25
        ValidationResult validationResult = check.check(feature);
        assertTrue(validationResult.isValid());
        feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME, "25"));//the actual length of this feature is 25
        ValidationResult validationResult1 = check.check(feature1);
        assertTrue(validationResult1.isValid());
    }

    @Test
    public void testCheck_UnknownLength() {
        QualifierFactory qualifierFactory = new QualifierFactory();
        feature.addQualifier(qualifierFactory.createQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME, "unknown"));
        ValidationResult validationResult = check.check(feature);
        assertTrue(!validationResult.isValid());
        assertEquals(1, validationResult.count("EstimatedLengthCheck-2", Severity.ERROR));
        feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME, "unknown"));
        ValidationResult validationResult1 = check.check(feature1);
        assertTrue(!validationResult1.isValid());
        assertEquals(1, validationResult1.count("EstimatedLengthCheck-2", Severity.ERROR));
    }

    @Test
    public void testCheck_UnknownLengthFine() {
        QualifierFactory qualifierFactory = new QualifierFactory();
        feature.addQualifier(qualifierFactory.createQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME, "unknown"));
        LocationFactory locationFactory = new LocationFactory();
        Order<Location> locationOrder = new Order<Location>();
        locationOrder.addLocation(locationFactory.createLocalRange(1l, 100l));//should be 100 if unknown
        feature.setLocations(locationOrder);
        ValidationResult validationResult = check.check(feature);
        assertTrue(validationResult.isValid());
        feature1.addQualifier(qualifierFactory.createQualifier(Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME, "unknown"));
        LocationFactory locationFactory1 = new LocationFactory();
        Order<Location> locationOrder1 = new Order<Location>();
        locationOrder1.addLocation(locationFactory1.createLocalRange(1l, 100l));//should be 100 if unknown
        feature1.setLocations(locationOrder1);
        ValidationResult validationResult1 = check.check(feature1);
        assertTrue(validationResult1.isValid());
    }
}
