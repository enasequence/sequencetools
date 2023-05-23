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

    private static final String SAMPLE_ID = "ERS6455303";

    @Autowired
    private DataSource dataSource;

    @Test
    public void testGetSourceFeature() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            EraproDAOUtilsImpl eraproDAOUtils = new EraproDAOUtilsImpl(conn);

            SourceFeature sourceFeature = eraproDAOUtils.getSourceFeature(SAMPLE_ID);

            Assert.assertEquals("Homo sapiens", sourceFeature.getScientificName());
            Assert.assertEquals(9606L, sourceFeature.getTaxId().longValue());
        }
    }
}
