package uk.ac.ebi.embl.api.service;

import uk.ac.ebi.ena.webin.cli.service.SampleService;
import uk.ac.ebi.ena.webin.cli.service.SampleXmlService;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

public class WebinSampleRetrievalService implements SampleRetrievalService {

    private final String webinAuthToken;
    private final boolean webinCliTestMode;
    public WebinSampleRetrievalService(String webinAuthToken, boolean webinCliTestMode){
        this.webinAuthToken=webinAuthToken;
        this.webinCliTestMode=webinCliTestMode;
    }
    
    @Override
    public Sample getSample(String sampleId) {
        
        // Retrieve sampleObj, sampleXml and return sampleObj with attributes.
        SampleService sampleService=getSampleService( webinAuthToken, webinCliTestMode);
        SampleXmlService sampleXmlService=getSampleXmlService( webinAuthToken, webinCliTestMode);
        
        Sample sampleOb=sampleService.getSample(sampleId);
        Sample sampleFromXml=sampleXmlService.getSample(sampleOb.getSraSampleIdId());
        sampleOb.setAttributes(sampleFromXml.getAttributes());
        return sampleOb;
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
}
