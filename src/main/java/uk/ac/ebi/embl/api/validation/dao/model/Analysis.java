package uk.ac.ebi.embl.api.validation.dao.model;

public class Analysis {
    private String submissionAccountId;
    private String uniqueAlias;

    public String getSubmissionAccountId() {
        return submissionAccountId;
    }

    public void setSubmissionAccountId(String submissionAccountId) {
        this.submissionAccountId = submissionAccountId;
    }

    public String getUniqueAlias() {
        return uniqueAlias;
    }

    public void setUniqueAlias(String uniqueAlias) {
        this.uniqueAlias = uniqueAlias;
    }
}
