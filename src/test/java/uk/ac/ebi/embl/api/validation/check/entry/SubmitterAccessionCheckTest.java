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
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.embl.api.validation.plan.ValidationPlan;

import static org.junit.Assert.*;

public class SubmitterAccessionCheckTest {

    private static TestValidationPlan testValidationPlan(ValidationScope validationScope) {
        EmblEntryValidationPlanProperty property = new EmblEntryValidationPlanProperty();
        property.validationScope.set(validationScope);
        return new TestValidationPlan(property);
    }

    private static class TestValidationPlan extends ValidationPlan {

        public TestValidationPlan(EmblEntryValidationPlanProperty property) {
            super(property);
        }

        @Override
        public ValidationResult execute(Object entry) throws ValidationEngineException {
            validationResult = new ValidationResult();
            SubmitterAccessionCheck check = new SubmitterAccessionCheck();
            execute(check, entry);
            return validationResult;
        }
        
        public EmblEntryValidationPlanProperty getProperty(){
            return planProperty;
        }
    }

    @Before
    public void setUp() {
        ValidationMessageManager
                .addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    }

    /**
     * Test that check fails if the submitter accession has not been set.
     */
    @Test
    public void testCheck_WithoutSubmitterAccession() throws Exception {
        for (ValidationScope validationScope : ValidationScope.values()) {
            TestValidationPlan validationPlan = testValidationPlan(validationScope);
            EntryFactory entryFactory = new EntryFactory();
            Entry entry = entryFactory.createEntry();
            ValidationResult result = validationPlan.execute(entry);
            if (validationScope == ValidationScope.ASSEMBLY_CHROMOSOME ||
                    validationScope == ValidationScope.ASSEMBLY_SCAFFOLD ||
                    validationScope == ValidationScope.ASSEMBLY_CONTIG ||
                    validationScope == ValidationScope.ASSEMBLY_TRANSCRIPTOME) {
                assertTrue(!result.isValid());
                assertEquals(1, result.count("SubmitterAccessionCheck_1", Severity.ERROR));
            } else {
                assertTrue("Validation scope: " + validationScope.name(), result.isValid());
            }
        }
    }

    /**
     * Test that check does not fail if the submitter accession has maximum length.
     */
    @Test
    public void testCheck_WithMaximumLengthSubmitterAccession() throws Exception {
        for (ValidationScope validationScope : ValidationScope.values()) {
            TestValidationPlan validationPlan = testValidationPlan(validationScope);
            EntryFactory entryFactory = new EntryFactory();
            Entry entry = entryFactory.createEntry();
            entry.setSubmitterAccession("01234567890123456789012345678901234567890123456789");
            ValidationResult result = validationPlan.execute(entry);
            assertTrue(result.isValid());
        }
    }

    /**
     * Test that check fails if the submitter accession is over maximum length.
     */
    @Test
    public void testCheck_OverMaximumLengthSubmitterAccession() throws Exception {
        for (ValidationScope validationScope : ValidationScope.values()) {
            TestValidationPlan validationPlan = testValidationPlan(validationScope);
            EntryFactory entryFactory = new EntryFactory();
            Entry entry = entryFactory.createEntry();
            entry.setSubmitterAccession("012345678901234567890123456789012345678901234567891");
            ValidationResult result = validationPlan.execute(entry);
            if (validationScope == ValidationScope.ASSEMBLY_CHROMOSOME ||
                    validationScope == ValidationScope.ASSEMBLY_SCAFFOLD ||
                    validationScope == ValidationScope.ASSEMBLY_CONTIG ||
                    validationScope == ValidationScope.ASSEMBLY_TRANSCRIPTOME) {
                assertTrue(!result.isValid());
                assertEquals(1, result.count("SubmitterAccessionCheck_2", Severity.ERROR));
            } else {
                assertTrue(result.isValid());
            }
        }
    }

    /**
     * Test that check fails if the submitter accession is over maximum length.
     */
    @Test
    public void testCheck_OverMaximumLengthSubmitterAccessionAndIgnoreError() throws Exception {
        for (ValidationScope validationScope : ValidationScope.values()) {
            TestValidationPlan validationPlan = testValidationPlan(validationScope);
            EntryFactory entryFactory = new EntryFactory();
            Entry entry = entryFactory.createEntry();
            entry.setSubmitterAccession("012345678901234567890123456789012345678901234567891");
            validationPlan.getProperty().ignore_errors.set(true);
            
            ValidationResult result = validationPlan.execute(entry);
            if (validationScope == ValidationScope.ASSEMBLY_CHROMOSOME ||
                    validationScope == ValidationScope.ASSEMBLY_SCAFFOLD ||
                    validationScope == ValidationScope.ASSEMBLY_CONTIG ||
                    validationScope == ValidationScope.ASSEMBLY_TRANSCRIPTOME) {
                assertTrue(result.isValid());
            } else {
                assertTrue(result.isValid());
            }
        }
    }
}
