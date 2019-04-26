package uk.ac.ebi.embl.template;

import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OrganelleDETemplatePreProcessor implements TemplatePreProcessor {
    public static final String PP_ORGANELLE_TOKEN = "PP_ORGANELLE";
    public static final String ORGANELLE_TOKEN = "ORGANELLE";
    protected static final String MISSING_ORGANELLE_TOKEN_MESSAGE = "missing organelle token";
    protected static final String INVALID_ORGANELLE_TOKEN_MESSAGE = "invalid organelle token";
    private Map<String, String> tokenMap = new HashMap<String, String>();

    public OrganelleDETemplatePreProcessor() {
        super();
        initTokenMap();
    }

    protected String lookupTokenValue(String key) {
        return tokenMap.get(key);
    }

    private void initTokenMap() {
        tokenMap.clear();
        tokenMap.put("chromatophore", "chromatophore");
        tokenMap.put("hydrogenosome", "hydrogenosome");
        tokenMap.put("mitochondrion", "mitochondrial");
        tokenMap.put("nucleomorph", "nucleomorph");
        tokenMap.put("plastid", "plastid");
        tokenMap.put("mitochondrion:kinetoplast", "kinetoplast");
        tokenMap.put("plastid:chloroplast", "chloroplast");
        tokenMap.put("plastid:apicoplast", "apicoplast");
        tokenMap.put("plastid:chromoplast", "chromoplast");
        tokenMap.put("plastid:cyanelle", "cyanelle");
        tokenMap.put("plastid:leucoplast", "leucoplast");
        tokenMap.put("plastid:proplastid", "proplastid");
    }

    @Override
    public ValidationResult process(TemplateVariables variablesMap) {
        final ValidationResult validationResult = new ValidationResult();
        String organelleTokenValue = variablesMap.getTokenValue(ORGANELLE_TOKEN);
        if (organelleTokenValue != null && !organelleTokenValue.isEmpty()) {
            final String ppOrganelleTokenValue = lookupTokenValue(organelleTokenValue);
            if(ppOrganelleTokenValue!=null)
                variablesMap.addToken(PP_ORGANELLE_TOKEN, ppOrganelleTokenValue);
            else {
                ValidationMessage<Origin> message = ValidationMessage.message(Severity.ERROR, INVALID_ORGANELLE_TOKEN_MESSAGE);
                validationResult.append(message);
            }
        }
        return validationResult;
    }

    protected Set<String> getTokenKeys() {
        return tokenMap.keySet();
    }
}
