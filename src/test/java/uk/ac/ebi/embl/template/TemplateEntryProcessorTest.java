package uk.ac.ebi.embl.template;


import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorException;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.flatfile.reader.genomeassembly.ChromosomeListFileReaderTest;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class TemplateEntryProcessorTest {
    
    String AUTH_JSON="{\"authRealms\":[\"ENA\"],\"password\":\"sausages\",\"username\":\"Webin-256\"}";
    String token="";
    
    @Test
    public void  testValidateAndGetSampleWithBioSampleId() throws Exception {

        testValidateSample("SAMEA9403245");
        testValidateSample("ERS7118926");
        testValidateSample("test_custom");
    }
    
    public void testValidateSample(String organism) throws Exception {
        Map<String, String> variablesMap=new HashMap<>();
        variablesMap.put("ORGANISM_NAME",organism);
        TemplateVariables templateVariables=new TemplateVariables(1,variablesMap);
        TemplateProcessorResultSet tpResult=new TemplateProcessorResultSet();
        SubmissionOptions options=getOptions();


        File templateFile = Paths.get(System.getProperty("user.dir") + "/src/test/resources/templates/ERT000002.xml").toFile();
        TemplateLoader templateLoader = new TemplateLoader();
        TemplateInfo templateInfo = templateLoader.loadTemplateFromFile(templateFile);
        Connection con=null;
        TemplateEntryProcessor templateEntryProcessor = new TemplateEntryProcessor(con);

        Sample sample=templateEntryProcessor.validateAndGetSample(templateVariables,tpResult,options);
        assertNotNull(sample);
        assertNotNull(sample.getBioSampleId());
        assertNotNull(sample.getId());
        assertNotNull(sample.getName());
    }
    
    private SubmissionOptions getOptions(){
        SubmissionOptions options=new SubmissionOptions();
        options.isTestMode=true;
        options.authToken= Optional.of(getAuthToken());
        return options;
    }
    
    private String getAuthToken(){

        if(StringUtils.isNotEmpty(token)){
            return token;
        }
        RestTemplate restTemplate=new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request =
                new HttpEntity<String>(AUTH_JSON, headers);
        ResponseEntity<String> response =
                restTemplate.postForEntity("https://wwwdev.ebi.ac.uk/ena/submit/webin/auth/token",request, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        token = response.getBody();
        return token;
    }
}
