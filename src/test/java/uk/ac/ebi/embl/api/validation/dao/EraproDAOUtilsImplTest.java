package uk.ac.ebi.embl.api.validation.dao;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;

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

    private static final String SAMPLE_ID = "ERS6455303";

    /** This is one of those private SRA samples that have existed before Nov 2022. Which is the point in time after
     * which all samples submitted to ENA were also made available on Biosamples.
     *
     * Private SRA samples before Nov 2022 do not have complete like for like information on Biosamples and as some
     * important details are absent, it is not safe to load such samples from Biosamples. */
    private static final String PRIVATE_SRA_PRE_NOV22_SAMPLE_ID = "ERS7118926";

    @Autowired
    private DataSource dataSource;

    @Test
    public void testGetSourceFeature() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            EraproDAOUtilsImpl eraproDAOUtils = new EraproDAOUtilsImpl(
                conn,
                WEBIN_ACCOUNT_USERNAME,
                WEBIN_ACCOUNT_PASSWORD,
                BIOSAMPLES_WEBIN_ACCOUNT_USERNAME,
                BIOSAMPLES_WEBIN_ACCOUNT_PASSWORD);

            SourceFeature sourceFeature = eraproDAOUtils.getSourceFeature(SAMPLE_ID);

            Assert.assertEquals(new Long(9606), sourceFeature.getTaxId());
            Assert.assertEquals("Homo sapiens", sourceFeature.getScientificName());

            sourceFeature = eraproDAOUtils.getSourceFeature(PRIVATE_SRA_PRE_NOV22_SAMPLE_ID);

            Assert.assertEquals(new Long(9606), sourceFeature.getTaxId());
            Assert.assertEquals("Homo sapiens", sourceFeature.getScientificName());
        }
    }
}