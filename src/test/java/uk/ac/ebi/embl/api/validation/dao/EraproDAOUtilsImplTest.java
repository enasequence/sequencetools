package uk.ac.ebi.embl.api.validation.dao;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.template.TemplateEntryProcessorTest;

import javax.sql.DataSource;
import java.sql.Connection;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application.properties")
@EnableAutoConfiguration
public class EraproDAOUtilsImplTest {

    private static final String WEBIN_ACCOUNT_USERNAME = System.getenv("webin-username");
    private static final String WEBIN_ACCOUNT_PASSWORD = System.getenv("webin-password");

    private static final String BIOSAMPLES_WEBIN_ACCOUNT_USERNAME = System.getenv("biosamples-webin-username");
    private static final String BIOSAMPLES_WEBIN_ACCOUNT_PASSWORD = System.getenv("biosamples-webin-password");

    @Autowired
    private DataSource dataSource;

    @Test
    public void testGetSourceFeature() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            EraproDAOUtilsImpl eraproDAOUtils = new EraproDAOUtilsImpl(
                conn,
                TemplateEntryProcessorTest.WEBIN_REST_URI,
                WEBIN_ACCOUNT_USERNAME,
                WEBIN_ACCOUNT_PASSWORD,
                TemplateEntryProcessorTest.WEBIN_AUTH_URI,
                TemplateEntryProcessorTest.BIOSAMPLES_URI,
                BIOSAMPLES_WEBIN_ACCOUNT_USERNAME,
                BIOSAMPLES_WEBIN_ACCOUNT_PASSWORD);

            SourceFeature sourceFeature = eraproDAOUtils.getSourceFeature("ERS6455303");
            Assert.assertEquals(new Long(9606), sourceFeature.getTaxId());
            Assert.assertEquals("Homo sapiens", sourceFeature.getScientificName());

            // This ID represents a sample which is private and does not contain full information on Biosamples. It offers
            // a nice opportunity to test ENA fallback i.e. if sample cannot be retrieved from Biosamples then it
            // will be retrieved from ENA instead.
            sourceFeature = eraproDAOUtils.getSourceFeature("ERS7118926");
            Assert.assertEquals(new Long(9606), sourceFeature.getTaxId());
            Assert.assertEquals("Homo sapiens", sourceFeature.getScientificName());

            sourceFeature = eraproDAOUtils.getSourceFeature("ERS14884990");
            Assert.assertEquals(new Long(498747), sourceFeature.getTaxId());
            Assert.assertEquals("uncultured Dehalococcoidia bacterium", sourceFeature.getScientificName());
            Assert.assertEquals("730a7d48-5078-4d70-a38b-47abe603ff41-asg-odAgeOroi1_bin.circ.25",
                sourceFeature.getSingleQualifierValue(Qualifier.ISOLATE_QUALIFIER_NAME));
        }
    }
}
