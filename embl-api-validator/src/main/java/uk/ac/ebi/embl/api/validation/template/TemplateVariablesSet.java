package uk.ac.ebi.embl.api.validation.template;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class TemplateVariablesSet {
    private static final long serialVersionUID = 1L;
    private Map<Integer, TemplateVariables> variables;

    public TemplateVariablesSet() {
        this.variables = new TreeMap<Integer, TemplateVariables>();
    }

    public TemplateVariablesSet(Map<Integer, Map<String, String>> variablesSetParam) {
        this.variables = new HashMap<Integer, TemplateVariables>();
        for (Integer entryNumber : variablesSetParam.keySet()) {
            Map<String, String> templateVariablesParam = variablesSetParam.get(entryNumber);
            TemplateVariables templateVariables = new TemplateVariables(templateVariablesParam);
            addEntryValues(entryNumber, templateVariables);
        }
    }

    public Set<Integer> getEntryNumbers() {
        return variables.keySet();
    }

    public TemplateVariables getEntryValues(Integer entryNumber) {
        return variables.get(entryNumber);
    }

    public int getEntryCount() {
        return variables.size();
    }

    public void addEntryValues(int entryNumber, TemplateVariables entryVariables) {
        variables.put(entryNumber, entryVariables);
    }

    public void clear() {
        variables.clear();
    }

    public boolean isEmpty() {
        return variables.isEmpty();
    }

    public boolean containsEntry(Integer entryNo) {
        return variables.containsKey(entryNo);
    }

    public void removeEntryVariables(Integer entryNumber) {
        variables.remove(entryNumber);
    }
}
