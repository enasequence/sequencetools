package uk.ac.ebi.embl.api.validation;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class ValidationMessageTest {

    private final static String TEST_MESSAGE = "TEST_MESSAGE";
    private final static String ORIGIN_MESSAGE_1 = "ORIGIN_MESSAGE_1";
    private final static String ORIGIN_MESSAGE_2 = "ORIGIN_MESSAGE_2";

    @Test
    public void testWriteTextMessageWithoutOrigin() throws IOException {
        StringWriter str = new StringWriter();
        ValidationMessage validationMessage = new ValidationMessage(Severity.ERROR, TEST_MESSAGE);
        validationMessage.writeMessage(str);
        assertEquals("ERROR: Missing message: TEST_MESSAGE\n", str.toString());

        str.getBuffer().setLength(0);
        validationMessage.setMessageFormatter(ValidationMessage.TEXT_MESSAGE_FORMATTER_PRECEDING_LINE_END);
        validationMessage.writeMessage(str);
        assertEquals("\nERROR: Missing message: TEST_MESSAGE", str.toString());

        str.getBuffer().setLength(0);
        validationMessage.setMessageFormatter(ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END);
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
        validationMessage.setMessageFormatter(ValidationMessage.TEXT_MESSAGE_FORMATTER_PRECEDING_LINE_END);
        validationMessage.writeMessage(str);
        assertEquals("\nERROR: Missing message: TEST_MESSAGE [ORIGIN_MESSAGE_1]", str.toString());

        str.getBuffer().setLength(0);
        validationMessage.setMessageFormatter(ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END);
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
        assertEquals("ERROR: Missing message: TEST_MESSAGE [ORIGIN_MESSAGE_1, ORIGIN_MESSAGE_2]\n", str.toString());

        str.getBuffer().setLength(0);
        validationMessage.setMessageFormatter(ValidationMessage.TEXT_MESSAGE_FORMATTER_PRECEDING_LINE_END);
        validationMessage.writeMessage(str);
        assertEquals("\nERROR: Missing message: TEST_MESSAGE [ORIGIN_MESSAGE_1, ORIGIN_MESSAGE_2]", str.toString());

        str.getBuffer().setLength(0);
        validationMessage.setMessageFormatter(ValidationMessage.TEXT_MESSAGE_FORMATTER_TRAILING_LINE_END);
        validationMessage.writeMessage(str);
        assertEquals("ERROR: Missing message: TEST_MESSAGE [ORIGIN_MESSAGE_1, ORIGIN_MESSAGE_2]\n", str.toString());
    }
}
