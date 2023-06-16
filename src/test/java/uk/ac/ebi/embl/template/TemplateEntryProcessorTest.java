package uk.ac.ebi.embl.template;


import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.service.SequenceToolsServices;
import uk.ac.ebi.embl.api.service.WebinSampleRetrievalService;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.ena.taxonomy.taxon.TaxonomyException;
import uk.ac.ebi.ena.webin.cli.validator.reference.Sample;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.*;

import static org.junit.Assert.*;

public class TemplateEntryProcessorTest {

    public static final String WEBIN_REST_URI = "https://wwwdev.ebi.ac.uk/ena/submit/drop-box/";
    public static final String WEBIN_AUTH_URI = "https://wwwdev.ebi.ac.uk/ena/submit/webin/auth/token";
    public static final String BIOSAMPLES_URI = "https://wwwdev.ebi.ac.uk/biosamples/";

    public static final String WEBIN_ACCOUNT_USERNAME = System.getenv("webin-username");
    public static final String WEBIN_ACCOUNT_PASSWORD = System.getenv("webin-password");

    public static final String BIOSAMPLES_WEBIN_ACCOUNT_USERNAME = System.getenv("biosamples-webin-username");
    public static final String BIOSAMPLES_WEBIN_ACCOUNT_PASSWORD = System.getenv("biosamples-webin-password");

    public final static String WEBIN_AUTH_JSON = String.format(
        "{\"authRealms\":[\"ENA\"],\"password\":\"%s\",\"username\":\"%s\"}",
        WEBIN_ACCOUNT_PASSWORD,
        WEBIN_ACCOUNT_USERNAME);

    public final static String BIOSAMPLES_WEBIN_AUTH_JSON = String.format(
        "{\"authRealms\":[\"ENA\"],\"password\":\"%s\",\"username\":\"%s\"}",
        BIOSAMPLES_WEBIN_ACCOUNT_PASSWORD,
        BIOSAMPLES_WEBIN_ACCOUNT_USERNAME);

    private final static String TEST_AUTH_URL="https://wwwdev.ebi.ac.uk/ena/submit/webin/auth/token";
    private final static File ERT000002_templateFile = Paths.get(System.getProperty("user.dir") + "/src/main/resources/templates/ERT000002.xml").toFile();
    private final static File ERT000056_templateFile = Paths.get(System.getProperty("user.dir") + "/src/main/resources/templates/ERT000056.xml").toFile();
    private final static String MOL_TYPE = "/mol_type";
    private static String token="";
    private TemplateEntryProcessor templateEntryProcessor;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Before
    public void setUp() throws Exception {
        templateEntryProcessor = getTemplateEntryProcessor();
        SequenceToolsServices.init(new WebinSampleRetrievalService(
            WEBIN_REST_URI,
            BIOSAMPLES_URI,
            getAuthTokenForTest(WEBIN_AUTH_JSON),
            getAuthTokenForTest(BIOSAMPLES_WEBIN_AUTH_JSON)));
    }
    
    @Test
    public void  testEntryProcess_ERT000002() throws Exception {

        TemplateInfo templateInfo_ERT000002 = new TemplateLoader().loadTemplateFromFile(ERT000002_templateFile);
        String molType_ERT000002 = getMolTypeFromTemplateForTest(templateInfo_ERT000002);
        
        // Test with biosample Id
        TemplateVariables templateVariables = getTemplateVariables_ERT000002("SAMEA9403245");
        executeEntryProcessTestWithSample("SAMEA9403245",templateInfo_ERT000002 ,molType_ERT000002,templateVariables );
        
        // Test with sample Id
        templateVariables = getTemplateVariables_ERT000002("ERS7118926");
        executeEntryProcessTestWithSample("ERS7118926",templateInfo_ERT000002 ,molType_ERT000002,templateVariables );
        
        // Test with sample alias "test_custom"
         templateVariables = getTemplateVariables_ERT000002("test_custom");
        executeEntryProcessTestWithSample("test_custom",templateInfo_ERT000002 ,molType_ERT000002, templateVariables);

        // Test with valid organism
        templateVariables = getTemplateVariables_ERT000002("test_custom");
        executeEntryProcessTestWithScientificName("Homo sapiens",templateInfo_ERT000002 ,molType_ERT000002,templateVariables );

        // Test with taxId
        templateVariables = getTemplateVariables_ERT000002("test_custom");
        executeEntryProcessTestWithTaxId("9606",templateInfo_ERT000002 ,molType_ERT000002,"Homo sapiens partial 5S rRNA gene",templateVariables );

        // Test with invalid taxid
        expectedException.expect(TaxonomyException.class);
        expectedException.expectMessage("Invalid HTTP response code: 400");
        templateVariables = getTemplateVariables_ERT000002("960600000000");
        executeEntryProcessInvalidTaxId("960600000000",templateInfo_ERT000002 ,molType_ERT000002,templateVariables );
        

        // Test with invalid organism
        templateVariables = getTemplateVariables_ERT000002("JUNK");
        executeEntryProcessInvalidOrganism("JUNK",templateInfo_ERT000002 ,molType_ERT000002,templateVariables );
        
    }

    @Test
    public void  testEntryProcess_ERT000056() throws Exception {
        TemplateInfo templateInfo_ERT000056 = new TemplateLoader().loadTemplateFromFile(ERT000056_templateFile);
        String molType_ERT000056 = getMolTypeFromTemplateForTest(templateInfo_ERT000056);

        // Test with biosample Id
        TemplateVariables templateVariables = getTemplateVariables_ERT000056("SAMEA9403245");
        executeEntryProcessTestWithSample("SAMEA9403245",templateInfo_ERT000056 ,molType_ERT000056,templateVariables );

        // Test with sample Id
        templateVariables = getTemplateVariables_ERT000056("ERS7118926");
        executeEntryProcessTestWithSample("ERS7118926",templateInfo_ERT000056 ,molType_ERT000056,templateVariables );

        // Test with sample alias "test_custom"
        templateVariables = getTemplateVariables_ERT000056("test_custom");
        executeEntryProcessTestWithSample("test_custom",templateInfo_ERT000056 ,molType_ERT000056, templateVariables);

        // Test with valid organism
        templateVariables = getTemplateVariables_ERT000056("test_custom");
        executeEntryProcessTestWithScientificName("Homo sapiens",templateInfo_ERT000056 ,molType_ERT000056,templateVariables );

        // Test with taxId
        templateVariables = getTemplateVariables_ERT000056("test_custom");
        executeEntryProcessTestWithTaxId("9606",templateInfo_ERT000056 ,molType_ERT000056,"Homo sapiens partial LINE-1 LINE",templateVariables );

        // Test with invalid taxid
        expectedException.expect(TaxonomyException.class);
        expectedException.expectMessage("Invalid HTTP response code: 400");
        templateVariables = getTemplateVariables_ERT000056("960600000000");
        executeEntryProcessInvalidTaxId("960600000000",templateInfo_ERT000056 ,molType_ERT000056,templateVariables );

        // Test with invalid organism
        templateVariables = getTemplateVariables_ERT000056("JUNK");
        executeEntryProcessInvalidOrganism("JUNK",templateInfo_ERT000056 ,molType_ERT000056,templateVariables );

    }
    
    public void executeEntryProcessTestWithSample(String organismName, TemplateInfo templateInfo, String molType,TemplateVariables templateVariables) throws Exception{
        
        
        Sample sample=getSampleForTest(templateEntryProcessor,templateVariables);
        
        TemplateProcessorResultSet templateProcessorResultSet = templateEntryProcessor.processEntry(templateInfo, molType, templateVariables,getOptions().getProjectId());

        SourceFeature sourceFeature=templateProcessorResultSet.getEntry().getPrimarySourceFeature();
        assertTrue(templateProcessorResultSet.getValidationResult().isValid());
        assertEquals(sourceFeature.getTaxId(),Long.valueOf(sample.getTaxId()));
        assertEquals(sourceFeature.getScientificName(),sample.getOrganism());
    }

    public void executeEntryProcessTestWithScientificName(String sceientificName, TemplateInfo templateInfo, String molType, TemplateVariables templateVariables) throws Exception{
        
        TemplateProcessorResultSet templateProcessorResultSet = templateEntryProcessor.processEntry(templateInfo, molType, templateVariables,getOptions().getProjectId());
        
        SourceFeature sourceFeature=templateProcessorResultSet.getEntry().getPrimarySourceFeature();
        assertTrue(templateProcessorResultSet.getValidationResult().isValid());
        assertEquals(sourceFeature.getScientificName(),sceientificName);
    }

    public void executeEntryProcessTestWithTaxId(String taxId, TemplateInfo templateInfo, String molType,String description, TemplateVariables templateVariables) throws Exception{

        TemplateProcessorResultSet templateProcessorResultSet = templateEntryProcessor.processEntry(templateInfo, molType, templateVariables,getOptions().getProjectId());

        SourceFeature sourceFeature=templateProcessorResultSet.getEntry().getPrimarySourceFeature();
        assertTrue(templateProcessorResultSet.getValidationResult().isValid());
        assertEquals(sourceFeature.getScientificName(),"Homo sapiens");
        assertEquals(templateProcessorResultSet.getEntry().getDescription().getText(),description);
        
    }
    
    public void executeEntryProcessInvalidOrganism(String scientificName, TemplateInfo templateInfo, String molType,TemplateVariables templateVariables) throws Exception{

        TemplateProcessorResultSet templateProcessorResultSet = templateEntryProcessor.processEntry(templateInfo, molType, templateVariables,getOptions().getProjectId());

        Collection<ValidationMessage<Origin>> messages=templateProcessorResultSet.getValidationResult().getMessages();
        assertFalse(templateProcessorResultSet.getValidationResult().isValid());
        assertTrue(messages.toString().contains("Organism name \""+scientificName+"\" is not submittable"));
    }

    public void executeEntryProcessInvalidTaxId(String taxId, TemplateInfo templateInfo, String molType,TemplateVariables templateVariables) throws Exception{

        TemplateProcessorResultSet templateProcessorResultSet = templateEntryProcessor.processEntry(templateInfo, molType, templateVariables,getOptions().getProjectId());

        Collection<ValidationMessage<Origin>> messages=templateProcessorResultSet.getValidationResult().getMessages();
        assertFalse(templateProcessorResultSet.getValidationResult().isValid());
        assertTrue(messages.toString().contains("Organism name \""+taxId+"\" is not submittable"));
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

    private TemplateVariables getTemplateVariables_ERT000056(String organismName){
        Map<String, String> variablesMap=new HashMap<>();
        variablesMap.put("ORGANISM",organismName);
        variablesMap.put("SEQUENCE","ACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACATACTACGACAT");
        variablesMap.put("ENVSAM","no");
        variablesMap.put("MOBTYPE","LINE");
        variablesMap.put("MOBNAME","LINE-1");
        variablesMap.put("5MOB","1");
        variablesMap.put("3MOB","60");
        variablesMap.put("5PARTIAL","yes");
        variablesMap.put("3PARTIAL","no");
        TemplateVariables templateVariables=new TemplateVariables(1,variablesMap);
        return templateVariables;
    }
    
    private SubmissionOptions getOptions(){
        SubmissionOptions options=new SubmissionOptions();
        options.webinCliTestMode=true;
        options.webinAuthToken = Optional.of(getAuthTokenForTest(WEBIN_AUTH_JSON));
        return options;
    }
    
    public static String getAuthTokenForTest(String authJson){

        if(StringUtils.isNotEmpty(token)){
            return token;
        }
        RestTemplate restTemplate=new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request =
                new HttpEntity<String>(authJson, headers);
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
