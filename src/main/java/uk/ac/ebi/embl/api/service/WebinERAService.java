package uk.ac.ebi.embl.api.service;

import org.springframework.web.client.RestClientException;
import webin.era.serivce.client.model.AnalysisEntity;
import webin.era.service.sdk.SequenceProcessApi;
import webin.era.service.sdk.client.ApiClient;
import webin.era.service.sdk.client.auth.HttpBasicAuth;


public class WebinERAService {

    private final SequenceProcessApi apiInstance;
    private static WebinERAService webinERAService;

    public static WebinERAService getWebinERAService(String url, String userName, String password) {
        synchronized (WebinERAService.class) {
            if (webinERAService == null) {
                webinERAService = new WebinERAService(url, userName, password);
            }
        }
        return webinERAService;
    }

    private WebinERAService() {
        apiInstance = new SequenceProcessApi(null);
    }

    private WebinERAService(String url, String userName, String password) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(url);
        // Configure HTTP basic authorization: basicScheme
        HttpBasicAuth basicScheme = (HttpBasicAuth) apiClient.getAuthentication("basicScheme");
        basicScheme.setUsername(userName);
        basicScheme.setPassword(password);
        apiInstance = new SequenceProcessApi(apiClient);
    }


    public void saveTemplateId(String analysisId, String templateId) {
        try {
            AnalysisEntity analysis = new AnalysisEntity();
            analysis.setAnalysisId(analysisId);
            analysis.setTemplateId(templateId);
            apiInstance.updateAnalysis(analysis);
        } catch (RestClientException e) {
            System.err.println("Exception when calling SequenceProcessApi#updateAnalysis");
            System.err.println("Status code: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
