package uk.ac.ebi.embl.template;

import java.util.ArrayList;
import java.util.List;

public class TemplateSectionInfo {
    private String name;
    private List<TemplateTokenInfo> depends;

    public TemplateSectionInfo() {
        this("", new ArrayList<TemplateTokenInfo>());
    }

    public TemplateSectionInfo(final String name, final List<TemplateTokenInfo> depends) {
        this.name = name;
        this.depends = depends;
    }

    public List<TemplateTokenInfo> getDepends() {
        return depends;
    }

    public String getName() {
        return name;
    }
}
