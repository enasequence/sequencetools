package uk.ac.ebi.embl.api.validation.submission;

public class ServiceConfig {

    private String eraServiceUrl;
    private String eraServiceUser;
    private String eraServicePassword;

    public String getEraServiceUrl() {
        return eraServiceUrl;
    }

    public void setEraServiceUrl(String eraServiceUrl) {
        this.eraServiceUrl = eraServiceUrl;
    }

    public String getEraServiceUser() {
        return eraServiceUser;
    }

    public void setEraServiceUser(String eraServiceUser) {
        this.eraServiceUser = eraServiceUser;
    }

    public String getEraServicePassword() {
        return eraServicePassword;
    }

    public void setEraServicePassword(String eraServicePassword) {
        this.eraServicePassword = eraServicePassword;
    }
}
