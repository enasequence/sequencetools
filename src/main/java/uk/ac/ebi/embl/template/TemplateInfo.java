package uk.ac.ebi.embl.template;

import java.util.*;

public class TemplateInfo {
    private String analysisId;
    private String id;
    private Integer version;
    private boolean included;
    private boolean newTemplate;
    private String name;
    private String templateString;
    private String description;
    private String example;
    private String exampleURL;
    private String filePath;
    private String comment;
    private List<TemplateTokenInfo> tokenInfos = new ArrayList<TemplateTokenInfo>();
    private List<TemplateSectionInfo> sectionInfos = new ArrayList<TemplateSectionInfo>();
    private List<TemplateTokenGroupInfo> groupInfo = new ArrayList<TemplateTokenGroupInfo>();

    public TemplateInfo() {
        //Null argument constructor for GWT
    }

    public TemplateInfo(MutableTemplateInfo mutableTemplateInfo) {
        this.id = mutableTemplateInfo.id;
        this.version = mutableTemplateInfo.version;
        this.included = true;
        this.newTemplate = mutableTemplateInfo.newTemplate;
        this.name = mutableTemplateInfo.name;
        this.templateString = mutableTemplateInfo.templateString;
        this.description = mutableTemplateInfo.description;
        this.example = mutableTemplateInfo.example;
        this.filePath = mutableTemplateInfo.filePath;
        this.comment = mutableTemplateInfo.comment;
        this.tokenInfos = new ArrayList<TemplateTokenInfo>(mutableTemplateInfo.tokenInfos);
        this.sectionInfos = new ArrayList<TemplateSectionInfo>(mutableTemplateInfo.sectionInfos);
        this.groupInfo = new ArrayList<TemplateTokenGroupInfo>(mutableTemplateInfo.groupInfo);
    }

    public TemplateInfo(String alternateName, String alternateDescription, TemplateInfo templateInfo) {
        this.name = alternateName;
        this.description = alternateDescription;
        this.id = templateInfo.id;
        this.version = templateInfo.version;
        this.included = templateInfo.included;
        this.templateString = templateInfo.templateString;
        this.example = templateInfo.example;
        this.filePath = templateInfo.filePath;
        this.comment = templateInfo.comment;
        this.tokenInfos = new ArrayList<TemplateTokenInfo>(templateInfo.tokenInfos);
        this.sectionInfos = new ArrayList<TemplateSectionInfo>(templateInfo.sectionInfos);
        this.groupInfo = new ArrayList<TemplateTokenGroupInfo>(templateInfo.groupInfo);
    }

    public String getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }

    public String getComment() {
        return comment;
    }

    public String getDescription() {
        return description;
    }

    public String getExample() {
        return example;
    }

    public boolean getHasExample() {
        return example != null && !example.isEmpty();
    }

    public boolean getIsNew() {
        return newTemplate;
    }

    public String getExampleURL() {
        return exampleURL;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getTemplateString() {
        return templateString;
    }

    public void setTemplateString(String templateString) {
        this.templateString = templateString;
    }

    public Integer getVersion() {
        return version;
    }

    public boolean isIncluded() {
        return included;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof TemplateInfo) {
            return ((TemplateInfo) obj).getId().equals(this.id);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + this.id.hashCode();
        return hash;
    }

    public List<TemplateSectionInfo> getSections() {
        return Collections.unmodifiableList(sectionInfos);
    }

    public List<TemplateTokenGroupInfo> getTokenGroups() {
        return Collections.unmodifiableList(groupInfo);
    }

    public List<TemplateTokenInfo> getTokens() {
        return Collections.unmodifiableList(tokenInfos);
    }

    /**
     * returns a map keyed with the token name
     *
     * @return
     */
    public Map<String, TemplateTokenInfo> getTokensAsMap() {
        HashMap<String, TemplateTokenInfo> tokens = new HashMap<String, TemplateTokenInfo>();
        for (TemplateTokenInfo tokenInfo : tokenInfos)
            tokens.put(tokenInfo.getName(), tokenInfo);
        return Collections.unmodifiableMap(tokens);
    }

    List<String> getMandatoryFields() {
        List<String> mandotaryFieldsList = new ArrayList<>();
        for (TemplateTokenInfo tokenInfo : tokenInfos) {
            if (tokenInfo.isMandatory())
                mandotaryFieldsList.add(tokenInfo.getName());
        }
        return mandotaryFieldsList;
    }
}
