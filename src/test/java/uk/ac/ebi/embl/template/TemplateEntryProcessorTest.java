package uk.ac.ebi.embl.template;


import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.service.SequenceToolsServices;
import uk.ac.ebi.embl.api.service.WebinSampleRetrievalService;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.*;

import static org.junit.Assert.*;

public class TemplateEntryProcessorTest {

    private final static String AUTH_JSON="{\"authRealms\":[\"ENA\"],\"password\":\"sausages\",\"username\":\"Webin-256\"}";
    private final static String TEST_AUTH_URL="https://wwwdev.ebi.ac.uk/ena/submit/webin/auth/token";
    private final static File templateFile = Paths.get(System.getProperty("user.dir") + "/src/main/resources/templates/ERT000002.xml").toFile();
    private final static String MOL_TYPE = "/mol_type";
    private static String token="";
    private TemplateEntryProcessor templateEntryProcessor;
    private TemplateVariables templateVariables;
    
    @Before
    public void setUp() throws Exception {
        templateEntryProcessor = getTemplateEntryProcessor();
        SequenceToolsServices.init(new WebinSampleRetrievalService(getAuthTokenForTest(),true));
    }
    
    @Test
    public void  testEntryProcess_ERT000002() throws Exception {

        TemplateInfo templateInfo_ERT000002 = new TemplateLoader().loadTemplateFromFile(templateFile);
        String molType_ERT000002 = getMolTypeFromTemplateForTest(templateInfo_ERT000002);

        // Test with biosample Id
        executeEntryProcessTestWithSample("SAMEA9403245",templateInfo_ERT000002 ,molType_ERT000002 );
        
        // Test with sample Id
        executeEntryProcessTestWithSample("ERS7118926",templateInfo_ERT000002 ,molType_ERT000002 );
        
        // Test with sample alias "test_custom"
        executeEntryProcessTestWithSample("test_custom",templateInfo_ERT000002 ,molType_ERT000002 );

        // Test with valid organism
        executeEntryProcessTestWithScientificName("Homo sapiens",templateInfo_ERT000002 ,molType_ERT000002 );

        // Test with taxId
        executeEntryProcessTestWithTaxId("9606",templateInfo_ERT000002 ,molType_ERT000002 );

        // Test with invalid taxid
        executeEntryProcessInvalidTaxId("960600000000",templateInfo_ERT000002 ,molType_ERT000002 );

        // Test with invalid organizm
        executeEntryProcessInvalidOrganism("JUNK",templateInfo_ERT000002 ,molType_ERT000002 );

    }
    
    public void executeEntryProcessTestWithSample(String organismName, TemplateInfo templateInfo, String molType) throws Exception{
        
        TemplateVariables templateVariables = getTemplateVariables_ERT000002(organismName);
        Sample sample=getSampleForTest(templateEntryProcessor,templateVariables);
        
        TemplateProcessorResultSet templateProcessorResultSet = templateEntryProcessor.processEntry(templateInfo, molType, templateVariables,getOptions().getProjectId());

        SourceFeature sourceFeature=templateProcessorResultSet.getEntry().getPrimarySourceFeature();
        assertTrue(templateProcessorResultSet.getValidationResult().isValid());
        assertEquals(sourceFeature.getTaxId(),Long.valueOf(sample.getTaxId()));
        assertEquals(sourceFeature.getScientificName(),sample.getOrganism());
    }

    public void executeEntryProcessTestWithScientificName(String sceientificName, TemplateInfo templateInfo, String molType) throws Exception{
        
        TemplateVariables templateVariables = getTemplateVariables_ERT000002(sceientificName);
        TemplateProcessorResultSet templateProcessorResultSet = templateEntryProcessor.processEntry(templateInfo, molType, templateVariables,getOptions().getProjectId());
        
        SourceFeature sourceFeature=templateProcessorResultSet.getEntry().getPrimarySourceFeature();
        assertTrue(templateProcessorResultSet.getValidationResult().isValid());
        assertEquals(sourceFeature.getScientificName(),sceientificName);
    }

    public void executeEntryProcessTestWithTaxId(String taxId, TemplateInfo templateInfo, String molType) throws Exception{

        TemplateVariables templateVariables = getTemplateVariables_ERT000002(taxId);
        TemplateProcessorResultSet templateProcessorResultSet = templateEntryProcessor.processEntry(templateInfo, molType, templateVariables,getOptions().getProjectId());

        SourceFeature sourceFeature=templateProcessorResultSet.getEntry().getPrimarySourceFeature();
        assertTrue(templateProcessorResultSet.getValidationResult().isValid());
        assertEquals(sourceFeature.getScientificName(),"Homo sapiens");
        assertEquals(templateProcessorResultSet.getEntry().getDescription().getText(),"Homo sapiens partial 5S rRNA gene");
        
    }
    
    public void executeEntryProcessInvalidOrganism(String scientificName, TemplateInfo templateInfo, String molType) throws Exception{

        TemplateVariables templateVariables = getTemplateVariables_ERT000002(scientificName);
        TemplateProcessorResultSet templateProcessorResultSet = templateEntryProcessor.processEntry(templateInfo, molType, templateVariables,getOptions().getProjectId());

        Collection<ValidationMessage<Origin>> messages=templateProcessorResultSet.getValidationResult().getMessages();
        assertFalse(templateProcessorResultSet.getValidationResult().isValid());
        assertTrue(messages.toString().contains("Scientific_name \""+scientificName+"\" is not submittable"));
    }

    public void executeEntryProcessInvalidTaxId(String taxId, TemplateInfo templateInfo, String molType) throws Exception{

        TemplateVariables templateVariables = getTemplateVariables_ERT000002(taxId);
        TemplateProcessorResultSet templateProcessorResultSet = templateEntryProcessor.processEntry(templateInfo, molType, templateVariables,getOptions().getProjectId());

        Collection<ValidationMessage<Origin>> messages=templateProcessorResultSet.getValidationResult().getMessages();
        assertFalse(templateProcessorResultSet.getValidationResult().isValid());
        assertTrue(messages.toString().contains("Scientific_name \""+taxId+"\" is not submittable"));
    }
    
    private TemplateEntryProcessor getTemplateEntryProcessor() {
        Connection con=null;
        return new TemplateEntryProcessor(con);
    } 
    
    private TemplateVariables getTemplateVariables_ERT000002(String organismName){
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
        options.webinCliTestMode=true;
        options.webinAuthToken = Optional.of(getAuthTokenForTest());
        return options;
    }
    
    public static String getAuthTokenForTest(){

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
        
        return templateEntryProcessor.validateAndGetSample(templateVariables);
    }
}
