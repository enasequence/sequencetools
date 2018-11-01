package uk.ac.ebi.embl.api.validation.template;

import java.util.ArrayList;
import java.util.List;

public class MutableTemplateInfo {
    public String id;
    public Integer version;
    public boolean newTemplate;
    public String name;
    public String templateString;
    public String description;
    public String example;
    public String filePath;
    public String comment;
    public List<TemplateTokenInfo> tokenInfos = new ArrayList<TemplateTokenInfo>();
    public List<TemplateSectionInfo> sectionInfos = new ArrayList<TemplateSectionInfo>();
    public List<TemplateTokenGroupInfo> groupInfo = new ArrayList<TemplateTokenGroupInfo>();
}
