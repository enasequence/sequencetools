package uk.ac.ebi.embl.template;

import org.apache.log4j.Logger;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;

import java.util.*;

public class FieldUniquenessChecker {
    private static final Logger LOGGER = Logger.getLogger(FieldUniquenessChecker.class);

    public void check(TemplateVariablesSet variables, List<TemplateProcessorResultSet> results) {
        /**
         * stick the results in keyed by entry number for easy access
         */
        Map<Integer, TemplateProcessorResultSet> resultsMap = new HashMap<Integer, TemplateProcessorResultSet>();
        for (TemplateProcessorResultSet result : results) {
            resultsMap.put(result.getEntryNumber(), result);
        }

        HashMap<String, Integer> stringsMap = new HashMap<String, Integer>();
        ArrayList<Integer> variableKeys = new ArrayList<Integer>(variables.getEntryNumbers());

        int count = 0;

        /**
         * then make concatenated strings of constants and variables (minus the sequence)
         */
        Collections.sort(variableKeys);
        for (int entryNumber : variableKeys) {

            count++;

            TemplateVariables entryVariables = variables.getEntryValues(entryNumber);
            StringBuilder variablesBuilder = new StringBuilder();
            ArrayList<String> tokenNames = new ArrayList<String>(entryVariables.getTokenNames());
            Collections.sort(tokenNames);//want to ensure a consistent order so the strings will be meaningfully comparable
            for (String tokenName : tokenNames) {
                if (!tokenName.equals(TemplateProcessorConstants.SEQUENCE_TOKEN) && !tokenName.equals(TemplateProcessorConstants.SEQUENCE_LENGTH_TOKEN)) {//dont want to include the sequence
                    String tokenValue = entryVariables.getTokenValue(tokenName);
                    variablesBuilder.append(tokenValue);
                }
            }

            String totalString = variablesBuilder.toString();

            if (stringsMap.containsKey(totalString)) {//there is a duplicate
                TemplateProcessorResultSet resultsSet = resultsMap.get(entryNumber);
                if (resultsSet != null) {
                    Integer duplicateEntryNumber = stringsMap.get(totalString);
                    ValidationMessage<Origin> message = new ValidationMessage<Origin>(
                            Severity.ERROR,
                            "DuplicatedValuesCheck",
                            entryNumber,
                            duplicateEntryNumber);

                    ValidationResult validationResult = new ValidationResult().append(message);
                    resultsSet.getValidationPlanResult().append(validationResult);
                } else {
                    LOGGER.info("result not found in check uniqueness " + entryNumber);
                }
            } else {
                stringsMap.put(totalString, entryNumber);
            }

            if (count > results.size()) {
                break;
            }
        }
    }

}
