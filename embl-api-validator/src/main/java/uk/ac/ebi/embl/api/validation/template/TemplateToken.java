package uk.ac.ebi.embl.api.validation.template;

public class TemplateToken {
    private TemplateTokenInfo tokenInfo;
    private String value;
    private boolean removed;

    public TemplateToken() {
        this(new TemplateTokenInfo());
    }

    public TemplateToken(final TemplateTokenInfo tokenInfo) {
        this.tokenInfo = tokenInfo;
    }

    public String getCvName() {
        return tokenInfo.getCvName();
    }

    public String getDescription() {
        return tokenInfo.getDescription();
    }

    public String getDisplayName() {
        return tokenInfo.getDisplayName();
    }

    public String getName() {
        return tokenInfo.getName();
    }

    public Integer getOrder() {
        return tokenInfo.getOrder();
    }

    public TemplateTokenGroupInfo getParentGroup() {
        return tokenInfo.getParentGroup();
    }

    public String getTip() {
        return tokenInfo.getTip();
    }

    public TemplateTokenInfo getTokenInfo() {
        return tokenInfo;
    }

    public TemplateTokenType getType() {
        return tokenInfo.getType();
    }

    public String getValue() {
        return value;
    }

    /**
     * this method needs to be used in conjunction with getType() to check if it is of type yesno - otherwise this is just
     * checking to see if the value is "yes"
     *
     * @return
     */
    public boolean getYesNoValue() {
        return value != null && value.equalsIgnoreCase(TemplateTokenInfo.YES_VALUE);
    }

    public boolean hasValue() {
        return value != null && !value.equals("");
    }

    public boolean ishasDescription() {
        return tokenInfo.isHasDescription();
    }

    public boolean ishasTip() {
        return tokenInfo.isHasTip();
    }

    public boolean isMandatory() {
        return tokenInfo.isMandatory();
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(final boolean removed) {
        this.removed = removed;
    }

    public void setTokenInfo(final TemplateTokenInfo tokenInfo) {
        this.tokenInfo = tokenInfo;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
