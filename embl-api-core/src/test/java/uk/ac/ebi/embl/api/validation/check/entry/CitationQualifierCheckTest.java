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
package uk.ac.ebi.embl.api.validation.check.entry;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.entry.reference.Submission;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CitationQualifierCheckTest {

	private Entry entry;
	private CitationQualifierCheck check;
    public FeatureFactory featureFactory;
    public LocationFactory locationFactory;
    public QualifierFactory qualifierFactory;
    public Feature feature;
    public Reference reference;

    @Before
	public void setUp() {
		EntryFactory entryFactory = new EntryFactory();
        featureFactory = new FeatureFactory();
        locationFactory = new LocationFactory();
        qualifierFactory = new QualifierFactory();
        feature=featureFactory.createFeature("feature");
        Qualifier citationQualifier=qualifierFactory.createQualifier(Qualifier.CITATION_QUALIFIER_NAME,"1");
        feature.addQualifier(citationQualifier);
        entry = entryFactory.createEntry();
        ReferenceFactory referenceFactory = new ReferenceFactory();
		reference = referenceFactory.createReference();
		reference.setReferenceNumber(1);
		Submission submission = (new ReferenceFactory()).createSubmission(referenceFactory.createPublication());
		reference.setPublication(submission);
        check = new CitationQualifierCheck();
    }


    @Test
    public void testCheck_NoEntry() {
        assertTrue(check.check(null).isValid());
    }

    @Test
    public void testCheck_NoFeatures() {
        entry.clearFeatures();
        ValidationResult result = check.check(entry);
        assertTrue(result.isValid());
    }

    @Test
	public void testChecknoReferenceswithCitationQualifier() {
    
    	   entry.addFeature(feature);
    	   ValidationResult validationResult = check.check(entry);
           assertTrue(!validationResult.isValid());
           assertEquals(1, validationResult.count(CitationQualifierCheck.CITATION_QUALIFIER_MESSAGE_ID, Severity.ERROR));
    }
    
    @Test
	public void testChecknoCitationQualifierwithReference() {
    
    	   entry.addReference(reference);
    	   ValidationResult validationResult = check.check(entry);
           assertTrue(validationResult.isValid());
           assertEquals(0, validationResult.count(CitationQualifierCheck.CITATION_QUALIFIER_MESSAGE_ID, Severity.ERROR));
    }

    @Test
	public void testCheckInvalidCitationQualifier() {
    	
    	   entry.addReference(reference);
           Qualifier citationQualifier=qualifierFactory.createQualifier(Qualifier.CITATION_QUALIFIER_NAME,"2");
           feature.addQualifier(citationQualifier);
           entry.addFeature(feature);
    	   ValidationResult validationResult = check.check(entry);
           assertTrue(!validationResult.isValid());
           assertEquals(1, validationResult.count(CitationQualifierCheck.CITATION_QUALIFIER_MESSAGE_ID, Severity.ERROR));

     }
    
    @Test
	public void testCheckvalidCitationQualifier() {
    	   entry.addReference(reference);
    	   entry.addFeature(feature);
    	   ValidationResult validationResult = check.check(entry);
           assertTrue(validationResult.isValid());
           assertEquals(0, validationResult.count(CitationQualifierCheck.CITATION_QUALIFIER_MESSAGE_ID, Severity.ERROR));

     }

 }
