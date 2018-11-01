package uk.ac.ebi.embl.api.validation.template;

import java.util.List;
import java.util.Map;

public class FASTAReaderTokenInfo {
    private Map<Integer, TemplateToken> variableTokenOrders;
    private Map<String, TemplateToken> variableTokenDisplayNames;
    private TemplateVariablesSet templateVariables;
    private TemplateVariables megaEntryConstants;
    private List<TemplateTokenInfo> allSelectedTokens;

    public FASTAReaderTokenInfo() {
    }

    public void setVariableTokenOrders(Map<Integer, TemplateToken> variableTokenOrders) {
        this.variableTokenOrders = variableTokenOrders;
    }

    public void setVariableTokenDisplayNames(Map<String, TemplateToken> variableTokenDisplayNames) {
        this.variableTokenDisplayNames = variableTokenDisplayNames;
    }

    public Map<Integer, TemplateToken> getVariableTokenOrders() {
        return variableTokenOrders;
    }

    public Map<String, TemplateToken> getVariableTokenDisplayNames() {
        return variableTokenDisplayNames;
    }

    public void setTemplateVariables(TemplateVariablesSet templateVariables) {
        this.templateVariables = templateVariables;
    }

    public TemplateVariablesSet getTemplateVariables() {
        return templateVariables;
    }

    public void setMegaEntryConstants(TemplateVariables megaEntryConstants) {
        this.megaEntryConstants = megaEntryConstants;
    }

    public TemplateVariables getMegaEntryConstants() {
        return megaEntryConstants;
    }

    public void setAllSelectedTokens(List<TemplateTokenInfo> allSelectedTokens) {
        this.allSelectedTokens = allSelectedTokens;
    }

    public List<TemplateTokenInfo> getAllSelectedTokens() {
        return allSelectedTokens;
    }
}
