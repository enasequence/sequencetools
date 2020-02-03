package uk.ac.ebi.embl.api.validation;

public class SampleInfo {
    private String sampleId;
    private String scientificName;
    private String uniqueName;
    private Long taxId;
    private String molType;

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public Long getTaxId() {
        return taxId;
    }

    public void setTaxId(Long taxId) {
        this.taxId = taxId;
    }

    public String getMolType() {
        return molType;
    }

    public void setMolType(String molType) {
        this.molType = molType;
    }
}
