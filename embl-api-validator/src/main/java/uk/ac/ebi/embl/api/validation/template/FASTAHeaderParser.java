package uk.ac.ebi.embl.api.validation.template;

import java.util.Map;

public class FASTAHeaderParser {
    public static final String FASTA_DELIMITER = ";";
    public static final String FASTA_KEY_VALUE_DELIMITER = "\\[";

    public void processFastaHeaderWithOrders(Map<Integer, TemplateToken> tokenOrders, String currentLine, TemplateVariables currentTokenVals) {
        String[] headerTokens = currentLine.split(FASTA_DELIMITER);
        for (int i = 1; i < headerTokens.length + 1; i++) {
            String tokenValue = headerTokens[i - 1];
            tokenValue = tokenValue.replaceAll("<br>", "\n");
            if (tokenOrders.containsKey(i)) {
                TemplateToken token = tokenOrders.get(i);
                String tokenName = token.getName();
                currentTokenVals.addToken(tokenName, tokenValue);
            }
        }
    }

    public void processFastaHeaderWithKeyValuePairs(Map<String, TemplateToken> variableTokenDisplayNames, String currentLine, TemplateVariables currentTokenVals) throws TemplateException {
        String[] headerTokens = currentLine.split(FASTA_KEY_VALUE_DELIMITER);
        for (int i = 1; i < headerTokens.length + 1; i++) {
            String keyTokenValue = headerTokens[i - 1];
            keyTokenValue = keyTokenValue.trim();
            if (keyTokenValue.endsWith("]")) {
                keyTokenValue = keyTokenValue.replaceAll("\\]", "");
            }
            keyTokenValue = keyTokenValue.replaceAll("<br>", "\n");
            if (keyTokenValue.contains("=")) {
                String[] tokens = keyTokenValue.split("=");
                if (tokens.length != 2) {
                    throw new TemplateException("Token does not have value before/following '=' " + keyTokenValue);
                }
                String tokenDisplayName = tokens[0];
                String tokenValue = tokens[1];
                if (variableTokenDisplayNames.containsKey(tokenDisplayName)) {
                    String tokenName = variableTokenDisplayNames.get(tokenDisplayName).getName();
                    currentTokenVals.addToken(tokenName, tokenValue);
                }
            }
        }
    }
}
