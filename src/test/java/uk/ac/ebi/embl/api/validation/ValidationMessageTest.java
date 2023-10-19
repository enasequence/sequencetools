/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import org.junit.Test;
import uk.ac.ebi.embl.api.validation.ValidationMessage.MessageFormatter;

public class ValidationMessageTest {

  private static final String TEST_MESSAGE = "TEST_MESSAGE";
  private static final String ORIGIN_MESSAGE_1 = "ORIGIN_MESSAGE_1";
  private static final String ORIGIN_MESSAGE_2 = "ORIGIN_MESSAGE_2";

  @Test
  public void testWriteTextMessageWithoutOrigin() throws IOException {
    StringWriter str = new StringWriter();
    ValidationMessage validationMessage = new ValidationMessage(Severity.ERROR, TEST_MESSAGE);
    validationMessage.writeMessage(str);
    assertEquals("ERROR: Missing message: TEST_MESSAGE\n", str.toString());

    str.getBuffer().setLength(0);
    validationMessage.setMessageFormatter(
        ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END);
    validationMessage.writeMessage(str);
    assertEquals("ERROR: Missing message: TEST_MESSAGE\n", str.toString());

    str.getBuffer().setLength(0);
    validationMessage.setMessageFormatter(
        ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END);
    validationMessage.writeMessage(str);
    assertEquals("ERROR: Missing message: TEST_MESSAGE\n", str.toString());
  }

  @Test
  public void testWriteTextMessageWithOneOrigin() throws IOException {
    StringWriter str = new StringWriter();
    ValidationMessage validationMessage = new ValidationMessage(Severity.ERROR, TEST_MESSAGE);
    validationMessage.addOrigin(new DefaultOrigin(ORIGIN_MESSAGE_1));
    validationMessage.writeMessage(str);
    assertEquals("ERROR: Missing message: TEST_MESSAGE [ORIGIN_MESSAGE_1]\n", str.toString());

    str.getBuffer().setLength(0);
    validationMessage.setMessageFormatter(
        ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END);
    validationMessage.writeMessage(str);
    assertEquals("ERROR: Missing message: TEST_MESSAGE [ORIGIN_MESSAGE_1]\n", str.toString());

    str.getBuffer().setLength(0);
    validationMessage.setMessageFormatter(
        ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END);
    validationMessage.writeMessage(str);
    assertEquals("ERROR: Missing message: TEST_MESSAGE [ORIGIN_MESSAGE_1]\n", str.toString());
  }

  @Test
  public void testWriteTextMessageWithTwoOrigins() throws IOException {
    StringWriter str = new StringWriter();
    ValidationMessage validationMessage = new ValidationMessage(Severity.ERROR, TEST_MESSAGE);
    validationMessage.addOrigin(new DefaultOrigin(ORIGIN_MESSAGE_1));
    validationMessage.addOrigin(new DefaultOrigin(ORIGIN_MESSAGE_2));
    validationMessage.writeMessage(str);
    assertEquals(
        "ERROR: Missing message: TEST_MESSAGE [ORIGIN_MESSAGE_1, ORIGIN_MESSAGE_2]\n",
        str.toString());

    str.getBuffer().setLength(0);
    validationMessage.setMessageFormatter(
        ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END);
    validationMessage.writeMessage(str);
    assertEquals(
        "ERROR: Missing message: TEST_MESSAGE [ORIGIN_MESSAGE_1, ORIGIN_MESSAGE_2]\n",
        str.toString());

    str.getBuffer().setLength(0);
    validationMessage.setMessageFormatter(
        ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END);
    validationMessage.writeMessage(str);
    assertEquals(
        "ERROR: Missing message: TEST_MESSAGE [ORIGIN_MESSAGE_1, ORIGIN_MESSAGE_2]\n",
        str.toString());
  }

  @Test
  public void testWriteMessageNoKey() throws IOException {
    StringWriter str = new StringWriter();
    ValidationMessage validationMessage =
        new ValidationMessage(Severity.ERROR, ValidationMessage.NO_KEY);
    validationMessage.setMessage("TEST");
    assertEquals("TEST", validationMessage.getMessage());
    validationMessage.writeMessage(str);
    assertEquals("ERROR: TEST\n", str.toString());
  }

  @Test
  public void testWriteTextMessageChangedFormatter() throws IOException {
    MessageFormatter mf = ValidationMessage.getDefaultMessageFormatter();
    try {
      ValidationMessage.setDefaultMessageFormatter(
          ValidationMessage.TEXT_TIME_MESSAGE_FORMATTER_TRAILING_LINE_END);

      StringWriter str = new StringWriter();
      ValidationMessage validationMessage =
          new ValidationMessage(Severity.ERROR, ValidationMessage.NO_KEY);
      validationMessage.setMessage("Missing message: TEST_MESSAGE");
      validationMessage.writeMessage(str);
      assertEquals(
          "****-**-**T**:**:** ERROR: Missing message: TEST_MESSAGE\n",
          str.toString().replaceAll("[\\d]", "*"));
    } finally {
      ValidationMessage.setDefaultMessageFormatter(mf);
    }
  }
  
 /* @Test
  public void testValidationScope(){
    assertTrue(ValidationScope.isPipelineScope(ValidationScope.ASSEMBLY_MASTER));
    assertTrue(ValidationScope.isPipelineScope(ValidationScope.ASSEMBLY_CONTIG));
    assertTrue(ValidationScope.isPipelineScope(ValidationScope.ASSEMBLY_SCAFFOLD));
    assertTrue(ValidationScope.isPipelineScope(ValidationScope.ASSEMBLY_CHROMOSOME));
    assertTrue(ValidationScope.isPipelineScope(ValidationScope.ASSEMBLY_TRANSCRIPTOME));


    assertTrue(ValidationScope.isPutffScope(ValidationScope.NCBI));
    assertTrue(ValidationScope.isPutffScope(ValidationScope.NCBI_MASTER));
    assertTrue(ValidationScope.isPutffScope(ValidationScope.EPO));
    assertTrue(ValidationScope.isPutffScope(ValidationScope.EPO_PEPTIDE));
    assertTrue(ValidationScope.isPutffScope(ValidationScope.EMBL));

    assertTrue(ValidationScope.isNcbiScope(ValidationScope.NCBI));
    assertTrue(ValidationScope.isNcbiScope(ValidationScope.NCBI_MASTER));
  }*/

  @Test
  public void testValidationScopeWithGroup(){
    assertTrue(ValidationScope.NCBI.isInGroup(ValidationScope.Group.NCBI));
    assertTrue(ValidationScope.NCBI.isInGroup(ValidationScope.Group.PUTFF));

    assertTrue(ValidationScope.ASSEMBLY_MASTER.isInGroup(ValidationScope.Group.ASSEMBLY));
    assertTrue(ValidationScope.ASSEMBLY_MASTER.isInGroup(ValidationScope.Group.PIPELINE));
    
  }
}
