package uk.ac.ebi.embl.template;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static uk.ac.ebi.embl.template.TemplateProcessorConstants.ORGANISM_NAME_TOKEN;
import static uk.ac.ebi.embl.template.TemplateProcessorConstants.ORGANISM_TOKEN;

public class CSVReader {
    private String currentLine;
    private final BufferedReader lineReader;
    private List<String> headerKeys;
    private int lineNumber = 0;
    private final Set<String> sequenceNames = new HashSet<>();
    // Sequence name can be given using ENTRYNUMBER or SEQUENCENAME. If not provided
    // then the sequence number will be used as the sequence name.
    private final List<String> sequenceNameHeaderKeys = Arrays.asList("ENTRYNUMBER", "SEQUENCENAME");
    private int sequenceNumber = 0;

    public CSVReader(final InputStream inputReader,final List<TemplateTokenInfo> allTokens, final int expectedMatchNumber) throws Exception {
        lineReader = new BufferedReader(new InputStreamReader(inputReader));
        readHeader(expectedMatchNumber, allTokens);
    }

    public CSVLine readTemplateSpreadsheetLine() throws Exception {
        skipEmptyLinesAndComments();
        if (currentLine != null) {
            return processTemplateSpreadsheetLine();
        }
        return null;
    }

    public void skipEmptyLinesAndComments() throws Exception {
        // Skip empty lines and comment lines.
        while(currentLine != null && (
              currentLine.isEmpty() ||
              currentLine.startsWith(FastaSpreadsheetConverter.COMMENT_TOKEN))) {
            readLine();
        }
    }

    public CSVLine processTemplateSpreadsheetLine() throws Exception {
        final TemplateVariables templateVariables = new TemplateVariables();
        final String[] tokenValues = StringUtils.splitPreserveAllTokens(currentLine, CSVWriter.UPLOAD_DELIMITER);
        if ((tokenValues.length) != headerKeys.size()) {
            String lineSummary = currentLine;
            if (currentLine.length() > 10) {
                lineSummary = currentLine.substring(0, 10);
            }
            throw new TemplateUserError("There are " + headerKeys.size() + " tokens specified in the header but " + tokenValues.length + " values for entry on line " + lineSummary + "..., please check your import file data is properly delimited with a 'tab'.");
        }

        String sequenceName = null;
        for (int i = 0; i < tokenValues.length; i++) {
            String tokenValue = tokenValues[i];
            checkTokenForBannedCharacters(tokenValue);
            tokenValue = tokenValue.replaceAll("<br>", "\n");
            tokenValue = tokenValue.replaceAll(";", ",");
            if (tokenValue.startsWith("\"") && tokenValue.endsWith("\"")) {
                tokenValue = StringUtils.stripStart(tokenValue, "\"");
                tokenValue = StringUtils.stripEnd(tokenValue, "\"");
            }

            if (!sequenceNameHeaderKeys.contains(headerKeys.get(i))) {
                templateVariables.addToken(headerKeys.get(i), tokenValue);
            }
            else {
                sequenceName = tokenValue;
            }
        }

        // If the submitter does not provide a sequence name then
        // use the sequence number as the sequence name.
        ++sequenceNumber;
        if (sequenceName == null) {
            sequenceName = String.valueOf(sequenceNumber);
        }
        // The sequence names must be unique.
        if (sequenceNames.contains(sequenceName)) {
            throw new TemplateUserError("Non-unique sequence name: " + sequenceName);
        }
        else {
            sequenceNames.add(sequenceName);
        }

        templateVariables.setSequenceName( sequenceName );

        readLine();
        return new CSVLine(++lineNumber, templateVariables);
    }

    private void checkTokenForBannedCharacters(final String tokenValue) throws Exception {
        if (!StringUtils.isBlank(tokenValue)) {
            if (tokenValue.contains(TemplateProcessorConstants.DELIMITER1) ||
                    tokenValue.contains(TemplateProcessorConstants.DELIMITER2) ||
                    tokenValue.contains(TemplateProcessorConstants.DELIMITER3) ||
                    tokenValue.contains(TemplateProcessorConstants.DELIMITER4)) {
                final String message = "Contains illegal characters <d1>, <d2>, <d3> or <d4>";
                throw new TemplateUserError(message);
            }
        }
    }

    private void prepareLineForParsing() {
        if (currentLine == null) {
            return;
        }
        currentLine = currentLine.trim();
        if (currentLine.startsWith("\""))
            currentLine = currentLine.replaceFirst("\"", "");//get rid of starting " if present - open office puts these in for strings and they need to be removed
        if(currentLine.endsWith(";"))
            currentLine = StringUtils.removeEnd(currentLine, ";");
    }

    private void readHeader(final int expectedMatchNumber, final List<TemplateTokenInfo> allTokens) throws Exception {
        readLine();
        if (currentLine == null) {
            throw new TemplateException("Template file is empty");
        }
        String header = null;
        skipEmptyLinesAndComments();
        if (currentLine != null) {
            header = currentLine;
            readLine();
        }
        if (header == null) {
            throw new TemplateUserError("Template file has no data");
        }
        if (!header.contains(ORGANISM_NAME_TOKEN) &&
            !header.contains(ORGANISM_TOKEN)) {
            throw new TemplateUserError("Template file has no header line");
        }
        header = header.replaceAll("\"", "");//remove all speech marks - open office puts these in
        final String[] headerTokens = header.split(CSVWriter.UPLOAD_DELIMITER);
        final List<String> recognizedKeys = new ArrayList<>();
        headerKeys = new ArrayList<>();
        /**
         * try to match the incoming header names with the token display names of the template. If not recognized, still
         * accept them with the value given as we accept additional fields.
         */
        for (final String headerName : headerTokens) {
            boolean tokenRecognized = false;
            for (final TemplateTokenInfo token : allTokens) {
                if (token.getDisplayName().equals(headerName)) {//does it match a template token name?
                    recognizedKeys.add(token.getName());
                    headerKeys.add(token.getName());
                    tokenRecognized = true;
                    break;
                }
            }
            if (!tokenRecognized) {//add as it comes
                headerKeys.add(headerName);
            }
        }
        final int recognizedTokenNumber = recognizedKeys.size();
        if ((expectedMatchNumber != 0) && (recognizedTokenNumber != expectedMatchNumber)) {
            throw new TemplateUserError(
                    "Not all variables have been recognized from the column headers. Have you removed fields from the variables since " +
                            "creating the spreadsheet? Check for spelling errors in your column names - names must match " +
                            "the token names you have selected. Check there are no additional characters at the ends of the header line such as ; or ," +
                            "Download a new sample spreadsheet to see what we are expecting you to load." +
                            "Additional columns not corresponding to variables are permitted.");
        }
    }

    private void readLine() throws TemplateException {
        try {
            currentLine = lineReader.readLine();
            prepareLineForParsing();
        } catch (final IOException e) {
            throw new TemplateException(e);
        }
    }
}
