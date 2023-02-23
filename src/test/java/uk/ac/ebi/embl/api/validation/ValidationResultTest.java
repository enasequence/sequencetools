package uk.ac.ebi.embl.api.validation;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class ValidationResultTest {

    private final static String TEST_MESSAGE = "TEST_MESSAGE";
    private final static String ORIGIN_MESSAGE_1 = "ORIGIN_MESSAGE_1";
    private final static String ORIGIN_MESSAGE_2 = "ORIGIN_MESSAGE_2";
    private final static String CURATOR_MESSAGE = "CURATOR_MESSAGE";
    private final static String REPORT_MESSAGE = "REPORT_MESSAGE";
    private final static String TARGET_ORIGIN = "TARGET_ORIGIN";

    @Test
    public void testWriteTextMessageWithoutOrigin() throws IOException {
        StringWriter str = new StringWriter();
        ValidationMessage validationMessage = new ValidationMessage(Severity.ERROR, TEST_MESSAGE);
        ValidationResult validationResult = new ValidationResult();
        validationResult.append(validationMessage);
        validationResult.writeMessages(str);
        assertEquals("ERROR: Missing message: TEST_MESSAGE\n", str.toString());

        str.getBuffer().setLength(0);
        validationResult.setMessageFormatter(ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END);
        validationResult.writeMessages(str);
        assertEquals("ERROR: Missing message: TEST_MESSAGE\n", str.toString());

        str.getBuffer().setLength(0);
        validationResult.setMessageFormatter(ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END);
        validationResult.writeMessages(str);
        assertEquals("ERROR: Missing message: TEST_MESSAGE\n", str.toString());

        str.getBuffer().setLength(0);
        validationResult.setMessageFormatter(ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END);
        validationResult.writeMessages(str, TARGET_ORIGIN);
        assertEquals("ERROR: Missing message: TEST_MESSAGE [TARGET_ORIGIN]\n", str.toString());
    }

    @Test
    public void testWriteTextMessageWithOneOrigin() throws IOException {
        StringWriter str = new StringWriter();
        ValidationMessage validationMessage = new ValidationMessage(Severity.ERROR, TEST_MESSAGE);
        ValidationResult validationResult = new ValidationResult();
        validationResult.append(validationMessage);
        validationMessage.addOrigin(new DefaultOrigin(ORIGIN_MESSAGE_1));
        validationResult.writeMessages(str);
        assertEquals("ERROR: Missing message: TEST_MESSAGE [ORIGIN_MESSAGE_1]\n", str.toString());

        str.getBuffer().setLength(0);
        validationResult.setMessageFormatter(ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END);
        validationResult.writeMessages(str);
        assertEquals("ERROR: Missing message: TEST_MESSAGE [ORIGIN_MESSAGE_1]\n", str.toString());

        str.getBuffer().setLength(0);
        validationResult.setMessageFormatter(ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END);
        validationResult.writeMessages(str);
        assertEquals("ERROR: Missing message: TEST_MESSAGE [ORIGIN_MESSAGE_1]\n", str.toString());

        str.getBuffer().setLength(0);
        validationResult.setMessageFormatter(ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END);
        validationResult.writeMessages(str, TARGET_ORIGIN);
        assertEquals("ERROR: Missing message: TEST_MESSAGE [TARGET_ORIGIN, ORIGIN_MESSAGE_1]\n", str.toString());
    }

    @Test
    public void testWriteTextMessageWithTwoOrigins() throws IOException {
        StringWriter str = new StringWriter();
        ValidationMessage validationMessage = new ValidationMessage(Severity.ERROR, TEST_MESSAGE);
        validationMessage.addOrigin(new DefaultOrigin(ORIGIN_MESSAGE_1));
        validationMessage.addOrigin(new DefaultOrigin(ORIGIN_MESSAGE_2));
        ValidationResult validationResult = new ValidationResult();
        validationResult.append(validationMessage);
        validationResult.writeMessages(str);
        assertEquals("ERROR: Missing message: TEST_MESSAGE [ORIGIN_MESSAGE_1, ORIGIN_MESSAGE_2]\n", str.toString());

        str.getBuffer().setLength(0);
        validationResult.setMessageFormatter(ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END);
        validationResult.writeMessages(str);
        assertEquals("ERROR: Missing message: TEST_MESSAGE [ORIGIN_MESSAGE_1, ORIGIN_MESSAGE_2]\n", str.toString());

        str.getBuffer().setLength(0);
        validationResult.setMessageFormatter(ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END);
        validationResult.writeMessages(str);
        assertEquals("ERROR: Missing message: TEST_MESSAGE [ORIGIN_MESSAGE_1, ORIGIN_MESSAGE_2]\n", str.toString());

        str.getBuffer().setLength(0);
        validationResult.setMessageFormatter(ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END);
        validationResult.writeMessages(str, TARGET_ORIGIN);
        assertEquals("ERROR: Missing message: TEST_MESSAGE [TARGET_ORIGIN, ORIGIN_MESSAGE_1, ORIGIN_MESSAGE_2]\n", str.toString());
    }

    @Test
    public void testWriteTextMessageWithCuratorMessage() throws IOException {
        StringWriter str = new StringWriter();
        ValidationMessage validationMessage = new ValidationMessage(Severity.ERROR, TEST_MESSAGE);
        validationMessage.setCuratorMessage(CURATOR_MESSAGE);
        ValidationResult validationResult = new ValidationResult();
        validationResult.append(validationMessage);
        validationResult.writeMessages(str);
        assertEquals(
                "ERROR: Missing message: TEST_MESSAGE\n\n" +
                "********\n" +
                "Curator message: CURATOR_MESSAGE\n" +
                "********", str.toString());

        str.getBuffer().setLength(0);
        validationResult.setWriteCuratorMessage(false);
        validationResult.writeMessages(str);
        assertEquals(
                "ERROR: Missing message: TEST_MESSAGE\n", str.toString());
    }


    @Test
    public void testWriteTextMessageWithReportMessage() throws IOException {
        StringWriter str = new StringWriter();
        ValidationMessage validationMessage = new ValidationMessage(Severity.ERROR, TEST_MESSAGE);
        validationMessage.setReportMessage(REPORT_MESSAGE);
        ValidationResult validationResult = new ValidationResult();
        validationResult.append(validationMessage);
        validationResult.writeMessages(str);
        assertEquals("ERROR: Missing message: TEST_MESSAGE\n", str.toString());

        str.getBuffer().setLength(0);
        validationResult.setWriteReportMessage(true);
        validationResult.writeMessages(str);
        assertEquals(
                "ERROR: Missing message: TEST_MESSAGE\n\n" +
                "********\n" +
                "Report message:\n" +
                "\n" +
                "REPORT_MESSAGE\n" +
                "********", str.toString());
    }
    
    @Test
    public void testWriteMessagestats() throws IOException {
        StringWriter str = new StringWriter();
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
        ValidationMessage validationMessage1= new ValidationMessage(Severity.ERROR, "QualifierCheck-1");
        ValidationMessage validationMessage2 = new ValidationMessage(Severity.ERROR, "QualifierCheck-1");
        ValidationResult validationResult = new ValidationResult();
        validationResult.append(validationMessage1);
        validationResult.append(validationMessage2);
        validationResult.writeMessageStats(str);
        assertEquals("2\tERROR\tQualifierCheck-1\tFeature qualifier \"{0}\" is not recognized.\n", str.toString());
    }

    @Test
    public void testMinSeverity() {
        Severity originalMinSeverity = ValidationResult.getMinSeverity();
        try {
            ValidationResult result = new ValidationResult();

            ValidationResult.setMinSeverity(Severity.ERROR);
            result.append(ValidationMessage.message(Severity.ERROR, ""));
            result.append(ValidationMessage.message(Severity.WARNING, ""));
            result.append(ValidationMessage.message(Severity.INFO, ""));
            result.append(ValidationMessage.message(Severity.FIX, ""));
            assertEquals(result.getMessages(Severity.ERROR).size(), 1);
            assertEquals(result.getMessages(Severity.WARNING).size(), 0);
            assertEquals(result.getMessages(Severity.INFO).size(), 0);
            assertEquals(result.getMessages(Severity.FIX).size(), 0);

            ValidationResult.setMinSeverity(Severity.WARNING);
            result.append(ValidationMessage.message(Severity.ERROR, ""));
            result.append(ValidationMessage.message(Severity.WARNING, ""));
            result.append(ValidationMessage.message(Severity.INFO, ""));
            result.append(ValidationMessage.message(Severity.FIX, ""));
            assertEquals(result.getMessages(Severity.ERROR).size(), 2);
            assertEquals(result.getMessages(Severity.WARNING).size(), 1);
            assertEquals(result.getMessages(Severity.INFO).size(), 0);
            assertEquals(result.getMessages(Severity.FIX).size(), 0);

            ValidationResult.setMinSeverity(Severity.INFO);
            result.append(ValidationMessage.message(Severity.ERROR, ""));
            result.append(ValidationMessage.message(Severity.WARNING, ""));
            result.append(ValidationMessage.message(Severity.INFO, ""));
            result.append(ValidationMessage.message(Severity.FIX, ""));
            assertEquals(result.getMessages(Severity.ERROR).size(), 3);
            assertEquals(result.getMessages(Severity.WARNING).size(), 2);
            assertEquals(result.getMessages(Severity.INFO).size(), 1);
            assertEquals(result.getMessages(Severity.FIX).size(), 0);

            ValidationResult.setMinSeverity(Severity.FIX);
            result.append(ValidationMessage.message(Severity.ERROR, ""));
            result.append(ValidationMessage.message(Severity.WARNING, ""));
            result.append(ValidationMessage.message(Severity.INFO, ""));
            result.append(ValidationMessage.message(Severity.FIX, ""));
            assertEquals(result.getMessages(Severity.ERROR).size(), 4);
            assertEquals(result.getMessages(Severity.WARNING).size(), 3);
            assertEquals(result.getMessages(Severity.INFO).size(), 2);
            assertEquals(result.getMessages(Severity.FIX).size(), 1);
        }
        finally {
            ValidationResult.setMinSeverity(originalMinSeverity);
        }
    }
}
