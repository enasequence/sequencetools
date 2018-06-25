package uk.ac.ebi.embl.api.validation.fixer.sourcefeature;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.helper.DataSetHelper;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelperImpl;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class ExclusiveSourceQualifiersFixTest {

    private Entry entry;
    private ExclusiveSourceQualifiersFix fix;
    public EntryFactory entryFactory;
    public FeatureFactory featureFactory;
    public QualifierFactory qualifierFactory;
    public EmblEntryValidationPlanProperty planProperty;
    @Before
    public void setUp() throws SQLException
    {
        ValidationMessageManager
                .addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);

        DataRow srcExclusiveQual1 = new DataRow("rearranged","germline");
        DataRow srcExclusiveQual2 = new DataRow("environmental_sample","strain");
        DataRow srcExclusiveQual3 = new DataRow("environmental_sample","culture_collection");

        DataSetHelper.createAndAdd(FileName.SOURCE_EXCLUSIVE_QUALIFIERS, srcExclusiveQual1,srcExclusiveQual2, srcExclusiveQual3 );

        entryFactory = new EntryFactory();
        qualifierFactory = new QualifierFactory();
        featureFactory = new FeatureFactory();
        entry = entryFactory.createEntry();
        planProperty=new EmblEntryValidationPlanProperty();
        planProperty.taxonHelper.set(new TaxonHelperImpl());
        fix = new ExclusiveSourceQualifiersFix();
        fix.setEmblEntryValidationPlanProperty(planProperty);
    }

    @Test
    public void testNoEntry()
    {
        assertTrue(fix.check(null).isValid());
    }

    @Test
    public void testNoFeatures()
    {
        ValidationResult validationResult = fix.check(entry);
        assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
    }

    @Test
    public void testNoEnvironmentQualifier()
    {
        Feature feature = featureFactory.createFeature("source");
        feature.addQualifier(Qualifier.ORGANISM_QUALIFIER_NAME);
        entry.addFeature(feature);
        assertTrue(fix.check(entry).getMessages().size() == 0);
    }

    @Test
    public void testEnvironmentQualifierButNoExclusion()
    {
        Feature feature = featureFactory.createFeature("source");
        feature.addQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME);
        entry.addFeature(feature);
        assertTrue(fix.check(entry).getMessages().size() == 0);
    }
    @Test
    public void testEnvironmentQualifierWithExclusion()
    {
        Feature feature = featureFactory.createFeature("source");
        feature.addQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME);
        feature.addQualifier(Qualifier.STRAIN_QUALIFIER_NAME);
        entry.addFeature(feature);
        ValidationResult result = fix.check(entry);
        assertTrue(result.getMessages().size() == 1);
        assertEquals(1, result.count("SourceQualifierRemovalFix", Severity.FIX));
        List<Qualifier> quals = entry.getFeatures().get(0).getQualifiers();
        assertEquals(1,quals.size());
        assertEquals(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME,quals.get(0).getName());
    }

    @Test
    public void testEnvironmentQualifierWithNonExclusion()
    {
        Feature feature = featureFactory.createFeature("source");
        feature.addQualifier(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME);
        feature.addQualifier(Qualifier.NOTE_QUALIFIER_NAME);
        feature.addQualifier(Qualifier.GERMLINE_QUALIFIER_NAME);
        feature.addQualifier(Qualifier.REARRANGED_QUALIFIER_NAME);
        entry.addFeature(feature);
        ValidationResult result = fix.check(entry);
        assertTrue(result.getMessages().size() == 0);
        assertEquals(0, result.count("SourceQualifierRemovalFix", Severity.FIX));
        List<Qualifier> quals = entry.getFeatures().get(0).getQualifiers();
        assertEquals(4,quals.size());
        boolean containsAllQuals = true;
        for(Qualifier q: quals) {
            if(!q.getName().equals(Qualifier.ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME) && !q.getName().equals(Qualifier.NOTE_QUALIFIER_NAME)
             && !q.getName().equals(Qualifier.GERMLINE_QUALIFIER_NAME) && !q.getName().equals(Qualifier.REARRANGED_QUALIFIER_NAME) ){
                containsAllQuals = false;
            }
        }
        assertTrue(containsAllQuals);
    }

}