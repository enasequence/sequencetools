package uk.ac.ebi.embl.template.reader;

import org.apache.log4j.Logger;

public class TokenMapConverter {
    private static final Logger LOGGER = Logger.getLogger(TemplateLoader.class);

    public static String templateVariablesToString(TemplateVariables variables) {
        StringBuilder builder = new StringBuilder();
        for (String tokenName : variables.getTokenNames()) {
            builder.append(tokenName);
            builder.append(TemplateProcessorConstants.DELIMITER1);
            if (variables.containsToken(tokenName)) {
                String value = variables.getTokenValue(tokenName);
                appendIfNotNull(builder, value);
            }
            builder.append(TemplateProcessorConstants.DELIMITER2);
        }
        return builder.toString();
    }

    private static void appendIfNotNull(StringBuilder builder, String value) {
        if (value != null) {
            builder.append(value);
        }
    }

    public static String templateVariableSetToString(TemplateVariablesSet variablesSet) {
        StringBuilder builder = new StringBuilder();
        for (Integer entryNumber : variablesSet.getEntryNumbers()) {
            builder.append(entryNumber.toString());
            builder.append(TemplateProcessorConstants.DELIMITER3);
            builder.append(templateVariablesToString(variablesSet.getEntryValues(entryNumber)));
            builder.append(TemplateProcessorConstants.DELIMITER4);
        }
        return builder.toString();
    }

    public static TemplateVariables stringToTemplateVariables(String string) {
        TemplateVariables returnMap = new TemplateVariables();
        try {
            if (string != null && !string.isEmpty()) {
                String[] tokens = string.split(TemplateProcessorConstants.DELIMITER2);
                for (String token : tokens) {
                    String[] values = token.split(TemplateProcessorConstants.DELIMITER1);
                    String key = values[0];
                    String value = "";
                    if (values.length == 2) {// if token not set length will be 1
                        value = values[1];
                    }
                    returnMap.addToken(key, value);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error converting string to token variables: \n".concat(string), e);
        }
        return returnMap;
    }

    public static TemplateVariablesSet stringToTemplateVariablesSet(String string) throws TemplateException {

        TemplateVariablesSet results = new TemplateVariablesSet();

        if (string != null && !string.equals("")) {
            String[] tokens = string.split(TemplateProcessorConstants.DELIMITER4);
            for (String token : tokens) {
                String[] values = token.split(TemplateProcessorConstants.DELIMITER3);
                if (values.length == 2) {
                    Integer key = new Integer(values[0]);
                    String value = values[1];
                    results.addEntryValues(key, stringToTemplateVariables(value));
                } else {
                    throw new TemplateException("Error loading template values - unmatched token");
                }
            }
        }
        return results;
    }

}
