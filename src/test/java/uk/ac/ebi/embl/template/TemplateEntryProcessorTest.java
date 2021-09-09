package uk.ac.ebi.embl.template;


import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorException;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.flatfile.reader.genomeassembly.ChromosomeListFileReaderTest;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.*;

import static org.junit.Assert.*;

public class TemplateEntryProcessorTest {

    private final static String AUTH_JSON="{\"authRealms\":[\"ENA\"],\"password\":\"sausages\",\"username\":\"Webin-256\"}";
    private final static String TEST_AUTH_URL="https://wwwdev.ebi.ac.uk/ena/submit/webin/auth/token";
    private final static File templateFile = Paths.get(System.getProperty("user.dir") + "/src/test/resources/templates/ERT000002.xml").toFile();
    private final static String MOL_TYPE = "/mol_type";
    private String token="";
    private TemplateInfo templateInfo ;
    private TemplateEntryProcessor templateEntryProcessor;
    private TemplateVariables templateVariables;
    private String molType;
    
    
    @Before
    public void setUp() throws Exception {
        templateInfo = new TemplateLoader().loadTemplateFromFile(templateFile);
        templateEntryProcessor = getTemplateEntryProcessor();
        molType = getMolTypeFromTemplateForTest(templateInfo);
    }
    

    @Test
    public void  testEntryProcess() throws Exception {

        // Test with biosample Id
        executeEntryProcessTest("SAMEA9403245");
        
        // Test with sample Id
        executeEntryProcessTest("ERS7118926");
        
        // Test with sample alias "test_custom"
        executeEntryProcessTest("test_custom");

        // Test with valid organism
        executeEntryProcessTestWithScientificName("Homo sapiens");

        // Test with tacId
        executeEntryProcessTestWithTaxId("9606");

        // Test with invalid organizm
        executeEntryProcessInvalidOrganism("JUNK");

    }
    
    public void executeEntryProcessTest(String organismName) throws Exception{
        
        TemplateVariables templateVariables = getTemplateVariables(organismName);
        Sample sample=getSampleForTest(templateEntryProcessor,templateVariables);
        
        TemplateProcessorResultSet templateProcessorResultSet = templateEntryProcessor.processEntry(templateInfo, molType, templateVariables,getOptions());

        SourceFeature sourceFeature=templateProcessorResultSet.getEntry().getPrimarySourceFeature();
        assertTrue(templateProcessorResultSet.getValidationResult().isValid());
        assertEquals(sourceFeature.getTaxId(),Long.valueOf(sample.getTaxId()));
        assertEquals(sourceFeature.getScientificName(),sample.getOrganism());
    }

    public void executeEntryProcessTestWithScientificName(String taxId) throws Exception{
        
        TemplateVariables templateVariables = getTemplateVariables(taxId);
        TemplateProcessorResultSet templateProcessorResultSet = templateEntryProcessor.processEntry(templateInfo, molType, templateVariables,getOptions());
        
        SourceFeature sourceFeature=templateProcessorResultSet.getEntry().getPrimarySourceFeature();
        assertTrue(templateProcessorResultSet.getValidationResult().isValid());
        assertEquals(sourceFeature.getScientificName(),"Homo sapiens");
    }

    public void executeEntryProcessTestWithTaxId(String taxId) throws Exception{

        TemplateVariables templateVariables = getTemplateVariables(taxId);
        TemplateProcessorResultSet templateProcessorResultSet = templateEntryProcessor.processEntry(templateInfo, molType, templateVariables,getOptions());

        SourceFeature sourceFeature=templateProcessorResultSet.getEntry().getPrimarySourceFeature();
        assertTrue(templateProcessorResultSet.getValidationResult().isValid());
        assertEquals(sourceFeature.getTaxId(),Long.valueOf(taxId));
        assertEquals(sourceFeature.getScientificName(),"Homo sapiens");
    }
    
    public void executeEntryProcessInvalidOrganism(String invalidOrganism) throws Exception{

        TemplateVariables templateVariables = getTemplateVariables(invalidOrganism);
        TemplateProcessorResultSet templateProcessorResultSet = templateEntryProcessor.processEntry(templateInfo, molType, templateVariables,getOptions());

        Collection<ValidationMessage<Origin>> messages=templateProcessorResultSet.getValidationResult().getMessages();
        //assertTrue(messages..equals("The given Organism: \"JUNK\" is not a sample. If is added as sample please check if the sample is associated with the passed user."));
        //Organism is not Submittable: "JUNK".
        assertFalse(templateProcessorResultSet.getValidationResult().isValid());
    }
    
    private TemplateEntryProcessor getTemplateEntryProcessor() {
        Connection con=null;
        return new TemplateEntryProcessor(con);
    } 
    
    private TemplateVariables getTemplateVariables(String organismName){
        Map<String, String> variablesMap=new HashMap<>();
        variablesMap.put("ORGANISM_NAME",organismName);
        variablesMap.put("SEQUENCE","ACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACAT");
        variablesMap.put("SEDIMENT","5S");
        variablesMap.put("ENV_SAMPLE","no");
        TemplateVariables templateVariables=new TemplateVariables(1,variablesMap);
        return templateVariables;
    }
    
    private SubmissionOptions getOptions(){
        SubmissionOptions options=new SubmissionOptions();
        options.isDevMode=true;
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
                restTemplate.postForEntity(TEST_AUTH_URL,request, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        token = response.getBody();
        return token;
    }
    
    private String getMolTypeFromTemplateForTest(TemplateInfo templateInfo) throws Exception {
        String template = templateInfo.getTemplateString();
        String molType = "";
        if (template.toString().contains(MOL_TYPE)) {
            String start = template.toString().substring(template.toString().indexOf(MOL_TYPE) + MOL_TYPE.length());
            if (start.contains("\"")) {
                start = start.substring(start.indexOf("\"") + 1);
                if (start.contains("\""))
                    molType = start.substring(0, start.indexOf("\""));
                else
                    throw new Exception("Found MOL TYPE in template but there is no ending '\"' (double quotes).");
            } else
                throw new Exception("Found MOL TYPE in template but there is no starting '\"' (double quotes).");
        } else
            throw new Exception("Template is missing MOL TYPE.");
        return molType;
    }
    
    private Sample getSampleForTest(TemplateEntryProcessor templateEntryProcessor,TemplateVariables templateVariables) throws Exception {
        
        return templateEntryProcessor.validateAndGetSample(templateVariables,new TemplateProcessorResultSet(),getOptions());
    }
}
