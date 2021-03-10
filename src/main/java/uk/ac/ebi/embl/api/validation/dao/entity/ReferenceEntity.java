package uk.ac.ebi.embl.api.validation.dao.entity;

public class ReferenceEntity {

    String brokerName;
    String submissionAccountId;
    String consortium;
    String surname;
    String middleInitials;
    String firstName;
    String centerName;
    String firstCreated;
    String laboratoryName;
    String address;
    String country;

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public String getSubmissionAccountId() {
        return submissionAccountId;
    }

    public void setSubmissionAccountId(String submissionAccountId) {
        this.submissionAccountId = submissionAccountId;
    }

    public String getConsortium() {
        return consortium;
    }

    public void setConsortium(String consortium) {
        this.consortium = consortium;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getMiddleInitials() {
        return middleInitials;
    }

    public void setMiddleInitials(String middleInitials) {
        this.middleInitials = middleInitials;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getCenterName() {
        return centerName;
    }

    public void setCenterName(String centerName) {
        this.centerName = centerName;
    }

    public String getFirstCreated() {
        return firstCreated;
    }

    public void setFirstCreated(String firstCreated) {
        this.firstCreated = firstCreated;
    }

    public String getLaboratoryName() {
        return laboratoryName;
    }

    public void setLaboratoryName(String laboratoryName) {
        this.laboratoryName = laboratoryName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isBroker() {
        return brokerName != null;
    }
}
