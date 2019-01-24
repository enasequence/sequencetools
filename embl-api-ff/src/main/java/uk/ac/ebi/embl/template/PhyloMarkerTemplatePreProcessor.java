package uk.ac.ebi.embl.template;

import uk.ac.ebi.embl.api.validation.ValidationResult;

public class PhyloMarkerTemplatePreProcessor implements TemplatePreProcessor {
    public ValidationResult process(TemplateVariables variablesMap) {
        ValidationResult validationResult = new ValidationResult();
        String markerToken = variablesMap.getTokenValue("MARKER");
        if (markerToken != null && !markerToken.isEmpty()) {
            if (markerToken.equals("actin"))
                variablesMap.addToken("PP_GENE", "act");
            else if (markerToken.equals("alpha tubulin"))
                variablesMap.addToken("PP_GENE", "tuba");
            else if (markerToken.equals("beta tubulin"))
                variablesMap.addToken("PP_GENE", "tubb");
            else if (markerToken.equals("translation elongation factor 1 alpha"))
                variablesMap.addToken("PP_GENE", "tef1a");
            else if (markerToken.equals("calmodulin"))
                variablesMap.addToken("PP_GENE", "CaM");
            else if (markerToken.equals("RNA polymerase II large subunit 1"))
                variablesMap.addToken("PP_GENE", "RPB1");
            else if (markerToken.equals("RNA polymerase II large subunit 2"))
                variablesMap.addToken("PP_GENE", "RPB2");
            else if (markerToken.equals("Glyceraldehyde 3-phosphate dehydrogenase"))
                variablesMap.addToken("PP_GENE", "GAPDH");
            else if (markerToken.equals("Histone H3"))
                variablesMap.addToken("PP_GENE", "H3");
        }
        return validationResult;
    }
}
