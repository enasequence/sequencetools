package uk.ac.ebi.embl.template;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.List;
import java.util.Map;

public class FastaSpreadsheetConverter {
    public static final String FASTA_HEADER_TOKEN = ">";
    public static final String COMMENT_TOKEN = "#";
    private static final Logger LOGGER = Logger.getLogger(FastaSpreadsheetConverter.class);
    /**
     * denotes whether we are in the mode of converting a fasta to a spreadsheet
     */
    private boolean convertFastaMode;
    /**
     * all the tokens being used
     */
    private List<TemplateTokenInfo> variableTokens;
    /**
     * the values of constant tokens (used when converting a large fasta into a spreadsheet)
     */
    private TemplateVariables constantTokens;
    private CSVWriter csvWriter;

    public FastaReaderResult convertFASTAFileToSpreadsheet(String fastaFilePath, String spreadsheetFilePath, FASTAReaderTokenInfo tokenInfo) throws TemplateException {
        this.variableTokens = tokenInfo.getAllSelectedTokens();
        this.constantTokens = tokenInfo.getMegaEntryConstants();
        this.convertFastaMode = true;//will be picked up in the fasta reading and also write an xl line
        this.csvWriter = new CSVWriter();
        csvWriter.prepareWriter(spreadsheetFilePath);
        csvWriter.writeDownloadSpreadsheetHeader(variableTokens);
        FastaReaderResult result = readFASTAFile(fastaFilePath, tokenInfo);
        csvWriter.flushAndCloseWriter();
        return result;
    }

    public FastaReaderResult readFASTAFile(String fastaFilePath, FASTAReaderTokenInfo tokenInfo) throws TemplateException {
        TemplateVariablesSet importedTemplateVariables = new TemplateVariablesSet();
        File inputFile = new File(fastaFilePath);
        BufferedReader reader = new BufferedReader(newFileReader(inputFile));
        String currentLine = readLine(reader);
        if (currentLine == null)
            throw new TemplateException("FASTA file is empty");
        Integer sequenceNumber = 0;
        StringBuilder sequenceBuilder = new StringBuilder();
        TemplateVariables currentTokenVals = new TemplateVariables();
        boolean sequinFasta = isSequinFasta(fastaFilePath);
        while (currentLine != null) {
            currentLine = currentLine.replace("\n", "");
            currentLine = currentLine.replace("\r", "");
            if (currentLine.isEmpty()) {
                currentLine = readLine(reader);
                continue;
            }

            if (currentLine.startsWith(FASTA_HEADER_TOKEN)) {
                currentLine = currentLine.replaceFirst(FASTA_HEADER_TOKEN, "");
                if (sequenceNumber != 0) {
                    String sequence = sequenceBuilder.toString();
                    currentTokenVals.addToken(TemplateProcessorConstants.SEQUENCE_TOKEN, sequence);

                    /**
                     * if we get to more than the entry limit - stop saving (but still convert to spreadsheet if set to)
                     */
                    if (sequenceNumber <= TemplateProcessorConstants.TEMPLATE_MEGA_ENTRY_SIZE) {
                        //leaving the sequence number despite being 1 ahead - we count the sequences from 1 in the templates system
                        importedTemplateVariables.addEntryValues(sequenceNumber, currentTokenVals);
                    }

                    if (convertFastaMode) {
                        this.csvWriter.writeSpreadsheetVariableRow(variableTokens, sequenceNumber, currentTokenVals);
                    }
                    sequenceBuilder = new StringBuilder();//make a new one to clear
                }

                sequenceNumber++;

                currentTokenVals = new TemplateVariables();

                //firstly, put any constants in when in convert mode. These will be overwritten by mappings
                //  specified from the header after this.
                if (convertFastaMode && constantTokens != null) {
                    for (String tokenName : constantTokens.getTokenNames()) {
                        currentTokenVals.addToken(tokenName, constantTokens.getTokenValue(tokenName));
                    }
                }

                FASTAHeaderParser fastaHeaderParser = new FASTAHeaderParser();
                if (!sequinFasta) {
                    Map<Integer, TemplateToken> tokenOrders = tokenInfo.getVariableTokenOrders();
                    fastaHeaderParser.processFastaHeaderWithOrders(tokenOrders, currentLine, currentTokenVals);
                } else {
                    Map<String, TemplateToken> variableTokenDisplayNames = tokenInfo.getVariableTokenDisplayNames();
                    fastaHeaderParser.processFastaHeaderWithKeyValuePairs(variableTokenDisplayNames, currentLine, currentTokenVals);
                }

            } else if (currentLine.startsWith(COMMENT_TOKEN)) {
                currentLine = readLine(reader);
                continue;
            } else {//its a sequence line
                currentLine = currentLine.replace("\n", "");
                sequenceBuilder.append(currentLine);
            }

            currentLine = readLine(reader);
        }

        //one more for the last sequence
        String sequence = sequenceBuilder.toString();
        currentTokenVals.addToken(TemplateProcessorConstants.SEQUENCE_TOKEN, sequence);

        //(if not megabulk, otherwise we have truncated list so not interested in this)
        if (sequenceNumber <= TemplateProcessorConstants.TEMPLATE_MEGA_ENTRY_SIZE) {
            importedTemplateVariables.addEntryValues(sequenceNumber, currentTokenVals);
        }

        if (convertFastaMode) {
            this.csvWriter.writeSpreadsheetVariableRow(variableTokens, sequenceNumber, currentTokenVals);
        }

        LOGGER.info("Imported " + importedTemplateVariables.getEntryCount());

        return new FastaReaderResult(sequenceNumber, importedTemplateVariables);
    }

    private String readLine(BufferedReader reader) throws TemplateException {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new TemplateException(e);
        }
    }

    private FileReader newFileReader(File inputFile) throws TemplateException {
        try {
            return new FileReader(inputFile);
        } catch (FileNotFoundException e) {
            throw new TemplateException(e);
        }
    }

    public boolean isSequinFasta(String filePath) throws TemplateException {
        try {
            return new FASTAUtils().isFASTAFileSequin(filePath);
        } catch (IOException e) {
            throw new TemplateException(e);
        }
    }

    public class FastaReaderResult {

        private Integer totalEntryCount;
        private TemplateVariablesSet importedVariables;

        public FastaReaderResult(Integer totalEntryCount,
                                 TemplateVariablesSet importedVariables) {

            this.totalEntryCount = totalEntryCount;
            this.importedVariables = importedVariables;
        }

        public Integer getTotalEntryCount() {
            return totalEntryCount;
        }

        public TemplateVariablesSet getImportedVariables() {
            return importedVariables;
        }
    }

}
