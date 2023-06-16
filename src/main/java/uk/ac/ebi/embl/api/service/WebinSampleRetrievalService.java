package uk.ac.ebi.embl.api.service;

import uk.ac.ebi.ena.webin.cli.service.SampleService;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

public class WebinSampleRetrievalService implements SampleRetrievalService {

    private String webinRestUri;
    private String biosamplesUri;
    private final String webinAuthToken;
    private final String biosamplesWebinAuthToken;

    public WebinSampleRetrievalService(
        String webinRestUri,
        String biosamplesUri,
        String webinAuthToken,
        String biosamplesWebinAuthToken) {

        this.webinRestUri = webinRestUri;
        this.biosamplesUri = biosamplesUri;
        this.webinAuthToken=webinAuthToken;
        this.biosamplesWebinAuthToken = biosamplesWebinAuthToken;
    }
    
    @Override
    public Sample getSample(String sampleId) {
        // Retrieve sampleObj, sampleXml and return sampleObj with attributes.
        SampleService sampleService=getSampleService();
        
        return sampleService.getSample(sampleId);
    }

    private SampleService getSampleService(){
        return new SampleService.Builder()
            .setWebinRestUri(webinRestUri)
            .setAuthToken(webinAuthToken)
            .setBiosamplesUri(biosamplesUri)
            .setBiosamplesWebinAuthToken(webinAuthToken)
            .build();
    }
}
