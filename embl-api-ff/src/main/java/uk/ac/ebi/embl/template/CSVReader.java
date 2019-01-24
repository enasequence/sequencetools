package uk.ac.ebi.embl.template;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    private String currentLine;
    private final BufferedReader lineReader;
    private int entryNumber;
    private List<String> headerKeys;

    public CSVReader(final InputStream inputReader,final List<TemplateTokenInfo> allTokens, final int expectedMatchNumber) throws Exception {
        lineReader = new BufferedReader(new InputStreamReader(inputReader));
        readHeader(expectedMatchNumber, allTokens);
    }

    public CSVLine readTemplateSpreadsheetLine() throws Exception {
        CSVLine csvLine = null;
        if (currentLine != null) {
            if (currentLine.isEmpty()) {
                currentLine = readLine();
                return readTemplateSpreadsheetLine();//skip empty lines
            }
            prepareLineForParsing();
            if (currentLine.startsWith(CSVWriter.HEADER_TOKEN)) {
                currentLine = readLine();
                return readTemplateSpreadsheetLine();//as this is the first line which is the header
            }
            if (currentLine.startsWith(FastaSpreadsheetConverter.COMMENT_TOKEN)) {
                currentLine = readLine();
                return readTemplateSpreadsheetLine();
            }
            final TemplateVariables entryTokensMap = new TemplateVariables();
            final String[] currentTokenLine = StringUtils.splitPreserveAllTokens(currentLine, CSVWriter.UPLOAD_DELIMITER);
            if ((currentTokenLine.length) != headerKeys.size()) {
                String lineSummary = currentLine;
                if (currentLine.length() > 10)
                    lineSummary = currentLine.substring(0, 10);
                throw new TemplateUserError("There are " + headerKeys.size() + " tokens specified in the header but " + currentTokenLine.length + " values for entry on line " + lineSummary + "..., please check your import file data is properly delimited with a 'tab'.");
            }
            Integer entryNumberCurr;
            try {
                entryNumberCurr = new Integer(currentTokenLine[0]);
                if (entryNumber == 0 && entryNumberCurr != 1)
                    throw new TemplateUserError("Numeric value for first entry " + CSVWriter.HEADER_TOKEN + " " + currentTokenLine[0] + " must start at 1 and increment sequentially by 1 for each entry.");
                if (entryNumberCurr - entryNumber != 1)
                    throw new TemplateUserError(CSVWriter.HEADER_TOKEN + " " + currentTokenLine[0] + " must sequentially be incremnted by 1.");
                entryNumber = entryNumberCurr;
            } catch (final NumberFormatException e) {
                throw new TemplateUserError(CSVWriter.HEADER_TOKEN + " " + currentTokenLine[0] + " must be nummeric and in ascending order.");
            }
            for (int i = 1; i < currentTokenLine.length; i++) {
                String tokenValue = currentTokenLine[i];
                checkTokenForBannedCharacters(tokenValue);
                tokenValue = tokenValue.replaceAll("<br>", "\n");
                tokenValue = tokenValue.replaceAll(";", ",");
                if (tokenValue.startsWith("\"") && tokenValue.endsWith("\"")) {
                    tokenValue = StringUtils.stripStart(tokenValue, "\"");
                    tokenValue = StringUtils.stripEnd(tokenValue, "\"");
                }
                entryTokensMap.addToken(headerKeys.get(i), tokenValue);
            }
            csvLine = new CSVLine(entryNumberCurr, entryTokensMap);
        }
        currentLine = readLine();
        return csvLine;
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
        currentLine = currentLine.trim();
        if (currentLine.startsWith("\""))
            currentLine = currentLine.replaceFirst("\"", "");//get rid of starting " if present - open office puts these in for strings and they need to be removed
        if(currentLine.endsWith(";"))
            currentLine = StringUtils.removeEnd(currentLine, ";");
    }

    private void readHeader(final int expectedMatchNumber, final List<TemplateTokenInfo> allTokens) throws Exception {
        currentLine = readLine();
        if (currentLine == null)
            throw new TemplateException("Template file is empty");
        String header = null;
        boolean headerFound = false;
        while (currentLine != null) {
            currentLine = currentLine.replaceFirst("\"", "");//get rid of starting " if present - open office puts these in for strings and they need to be removed
            if (currentLine.startsWith(CSVWriter.HEADER_TOKEN)) {
                header = currentLine;
                headerFound = true;
                break;
            }
            currentLine = readLine();
        }
        if (!headerFound)
            throw new TemplateUserError("Template header line not found, starts with : " + CSVWriter.HEADER_TOKEN);
        header = header.replaceAll("\"", "");//remove all speech marks - open office puts these in
        final String[] headerTokens = header.split(CSVWriter.UPLOAD_DELIMITER);
        final List<String> recognizedKeys = new ArrayList<String>();
        headerKeys = new ArrayList<String>();
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

    private String readLine() throws TemplateException {
        try {
            return lineReader.readLine();
        } catch (final IOException e) {
            throw new TemplateException(e);
        }
    }
}
