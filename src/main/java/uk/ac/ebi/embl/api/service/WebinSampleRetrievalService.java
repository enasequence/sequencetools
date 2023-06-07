package uk.ac.ebi.embl.api.service;

import uk.ac.ebi.ena.webin.cli.service.SampleService;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

public class WebinSampleRetrievalService implements SampleRetrievalService {

    private final String webinAuthToken;
    private final String biosamplesWebinAuthToken;
    private final boolean webinCliTestMode;

    public WebinSampleRetrievalService(
        String webinAuthToken,
        String biosamplesWebinAuthToken,
        boolean webinCliTestMode) {
        this.webinAuthToken=webinAuthToken;
        this.biosamplesWebinAuthToken = biosamplesWebinAuthToken;
        this.webinCliTestMode=webinCliTestMode;
    }
    
    @Override
    public Sample getSample(String sampleId) {
        // Retrieve sampleObj, sampleXml and return sampleObj with attributes.
        SampleService sampleService=getSampleService( webinAuthToken, webinCliTestMode);
        
        return sampleService.getSample(sampleId);
    }

    private SampleService getSampleService(String authToken, boolean webinCliTestMode){
        return new SampleService.Builder()
                .setAuthToken(authToken)
                .setBiosamplesWebinAuthToken(authToken)
                .setTest(webinCliTestMode)
                .build();
    }
}
