package uk.ac.ebi.embl.template.reader;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TemplateVariables {
    private static final long serialVersionUID = 1L;
    private Map<String, String> variables;

    public TemplateVariables() {
        variables = new HashMap<String, String>();
    }

    public TemplateVariables(Map<String, String> entryTokenMap) {
        this.variables = entryTokenMap;
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
