package uk.ac.ebi.embl.template;

import java.util.Map;

public interface LoadStrategyI {
    void loadTemplates() throws TemplateException;
    Map<String, Integer> getOrders();
    TemplateSet getTemplates();
}
