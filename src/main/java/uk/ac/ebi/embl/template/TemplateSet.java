package uk.ac.ebi.embl.template;

import java.util.HashMap;
import java.util.Set;

public class TemplateSet {
    private HashMap<String, TemplateVersions> templates = new HashMap<String, TemplateVersions>();

    public TemplateSet() {
    }

    public boolean containsTemplateId(String templateId) {
        return templates.containsKey(templateId);
    }

    public TemplateVersions getTemplateVersions(String templateId) {
        return templates.get(templateId);
    }

    public void addTemplateVersions(String id, TemplateVersions versionMap) {
        templates.put(id, versionMap);
    }

    public Set<String> getTemplateIds() {
        return templates.keySet();
    }

    public TemplateVersions getTemplate(String templateId) {
        return templates.get(templateId);
    }

    public TemplateInfo getLatestTemplateInfo(String templateId) throws TemplateException {

        if (containsTemplateId(templateId)) {
            TemplateVersions versions = getTemplate(templateId);
            return versions.getLatestTemplate();
        }

        throw new TemplateException("No template found with ID : " + templateId);
    }

}
