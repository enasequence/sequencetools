package uk.ac.ebi.embl.template;

import java.util.*;

public class TemplateVersions {
    private Map<Integer, TemplateInfo> versions;

    public TemplateVersions() {
        this.versions = new TreeMap();
    }

    public boolean containsVersion(Integer version) {
        return versions.containsKey(version);
    }

    public TemplateInfo getTemplate(Integer version) {
        return versions.get(version);
    }

    public void addTemplate(Integer version, TemplateInfo templateInfo) {
        versions.put(version, templateInfo);
    }

    public Set<Integer> getVersions() {
        return versions.keySet();
    }

    public TemplateInfo getLatestTemplate() {
        List<Integer> versionList = new ArrayList<Integer>(versions.keySet());
        return versions.get(versionList.get(versionList.size() - 1));//the last element - will be the largest version no.
    }

}
