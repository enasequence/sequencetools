package uk.ac.ebi.embl.api.validation.template;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TemplateVariables {
    private static final long serialVersionUID = 1L;
    private int sequenceNumber;
    private Map<String, String> variables;

    public TemplateVariables() {
        variables = new HashMap<String, String>();
    }

    public TemplateVariables(int sequenceNumber, Map<String, String> variables) {
        this.sequenceNumber = sequenceNumber;
        this.variables = variables;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public TemplateVariables(Map<String, String> entryTokenMap) {
        this.variables = entryTokenMap;
    }

    public void setTokenValue(String key, String value) {
        if (variables.containsKey(key))
            variables.put(key, value);
    }

    public boolean containsToken(String name) {
        return variables.containsKey(name);
    }

    public String getTokenValue(String tokenName) {
        return variables.get(tokenName);
    }

    public void addToken(String tokenName, String value) {
        variables.put(tokenName, value);
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Set<String> getTokenNames() {
        return variables.keySet();
    }

    public boolean isEmpty() {
        return variables.isEmpty();
    }

    public void removeToken(String name) {
        variables.remove(name);
    }
}
