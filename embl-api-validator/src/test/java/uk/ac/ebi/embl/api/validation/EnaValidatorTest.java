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
package uk.ac.ebi.embl.api.validation;

import static org.junit.Assert.*;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationPlanResult;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;

import java.io.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;


/**
 * Created by IntelliJ IDEA.
 * User: Lawrence
 * Date: 12-Jan-2009
 * Time: 11:23:15
 * To change this template use File | Settings | File Templates.
 */
public class EnaValidatorTest {

    private EnaValidator validator;

    @Before
    public void setUp() throws Exception {
        validator = new EnaValidator();
        EnaValidator.log_level = EnaValidator.LOG_LEVEL_ALL;
        EnaValidator.testMode = false;
        EnaValidator.remote = true;

        validator.initValidator();
        EnaValidator.fileType = FileType.EMBL;
    }

  //  @Ignore
    @Test
    public void testFile1() {
        /**
         * Tests that if there is no organism qualifier, the translating of the cds feature does not prevent the lask of
         * this qualifier from being checked. cds translations add an empty organism qualifier if not present. This checks
         * that the absence of the organism qualifier gets noticed before the cds check is run 
         */
        InputStream stream = this.getClass().getResourceAsStream("/no_source_organism_and_cds.txt");
        InputStreamReader reader = new InputStreamReader(stream);
        validator.reader = new EmblEntryReader(new BufferedReader(reader));
        Writer fileErrorWriter = new StringWriter();
        List<ValidationPlanResult> validationResult = validator.validateEntriesInReader(fileErrorWriter, null,null);
//        String formatErrorString = fileErrorWriter.toString();
        assertFalse(validationResult.isEmpty());
        List<ValidationMessage<Origin>> messages  = validationResult.get(0).getMessages("FeatureKeyCheck-2");
        assertFalse(messages.isEmpty());
        assertEquals("Mandatory qualifier \"organism\" not present in feature \"source\".", messages.get(0).getMessage());
    }

    @Test
    public void testFile2() {
        InputStream stream = this.getClass().getResourceAsStream("/test_file.txt");
        InputStreamReader reader = new InputStreamReader(stream);
        validator.reader = new EmblEntryReader(new BufferedReader(reader));
        Writer fileErrorWriter = new StringWriter();
        List<ValidationPlanResult> validationResult = validator.validateEntriesInReader(fileErrorWriter, null,null);
        assertFalse(validationResult.isEmpty());
        List<ValidationMessage<Origin>> messages = validationResult.get(0).getMessages("FeatureLocationCheck-3");
        assertFalse(messages.isEmpty());
        assertEquals("The begin and end position of a sequence span are in the wrong order.", messages.get(0).getMessage());
        assertTrue(messages.get(0).getSeverity().equals(Severity.ERROR));
    }

    @Test
    public void testSuppress() {
        InputStream stream = this.getClass().getResourceAsStream("/test_file.txt");
        InputStreamReader reader = new InputStreamReader(stream);
        validator.suppressedErrorCodes = Arrays.asList("FeatureLocationCheck-3");
        validator.reader = new EmblEntryReader(new BufferedReader(reader));
        Writer fileErrorWriter = new StringWriter();
        List<ValidationPlanResult> validationResult = validator.validateEntriesInReader(fileErrorWriter, null,null);
        assertFalse(validationResult.isEmpty());
        List<ValidationMessage<Origin>> messages = validationResult.get(0).getMessages("FeatureLocationCheck-3");
        assertTrue(messages.isEmpty());
    }
}
