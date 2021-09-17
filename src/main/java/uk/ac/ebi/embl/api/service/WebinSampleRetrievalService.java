package uk.ac.ebi.embl.api.service;

import uk.ac.ebi.ena.webin.cli.service.SampleService;
import uk.ac.ebi.ena.webin.cli.service.SampleXmlService;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

public class WebinSampleRetrievalService implements SampleRetrievalService {

    private String webinAuthToken;
    private boolean webinCliTestMode;
    public WebinSampleRetrievalService(String webinAuthToken, boolean webinCliTestMode){
        this.webinAuthToken=webinAuthToken;
        this.webinCliTestMode=webinCliTestMode;
    }
    
    @Override
    public Sample getSample(String sampleValue) {
        
        // Retrieve sampleObj, sampleXml and return sampleObj with attributes.
        
        SampleService sampleService=getSampleService( webinAuthToken, webinCliTestMode);
        SampleXmlService sampleXmlService=getSampleXmlService( webinAuthToken, webinCliTestMode);
        
        Sample sampleObj=sampleService.getSample(sampleValue);
        Sample sampleXmlObj=sampleXmlService.getSample(sampleObj.getSraSampleIdId());
        sampleObj.setAttributes(sampleXmlObj.getAttributes());
        return sampleObj;
    }

    private SampleService getSampleService(String authToken, boolean webinCliTestMode){
        return new SampleService.Builder()
                .setAuthToken(authToken)
                .setTest(webinCliTestMode)
                .build();
    }

    private SampleXmlService getSampleXmlService(String authToken, boolean webinCliTestMode){
        return new SampleXmlService.Builder()
                .setAuthToken(authToken)
                .setTest(webinCliTestMode)
                .build();
    }

    public String getWebinAuthToken() {
        return webinAuthToken;
    }

    public void setWebinAuthToken(String webinAuthToken) {
        this.webinAuthToken = webinAuthToken;
    }

    public boolean isWebinCliTestMode() {
        return webinCliTestMode;
    }

    public void setWebinCliTestMode(boolean webinCliTestMode) {
        this.webinCliTestMode = webinCliTestMode;
    }
}
