package uk.ac.ebi.embl.api.validation.check.sourcefeature;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.*;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClientImpl;

import java.nio.ByteBuffer;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class SequenceCoverageCheckTest {
    private Entry entry;
    private FeatureFactory featureFactory;
    private SequenceCoverageCheck check;
    private EntryFactory entryFactory;

    @Before
    public void setUp() throws SQLException {
        ValidationMessageManager
                .addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
        entryFactory = new EntryFactory();
        featureFactory = new FeatureFactory();
        check = new SequenceCoverageCheck();

        EmblEntryValidationPlanProperty planProperty=new EmblEntryValidationPlanProperty();
        planProperty.taxonClient.set(new TaxonomyClientImpl());
        check.setEmblEntryValidationPlanProperty(planProperty);

    }

    @Test
    public void testCheck_NoEntry() {
        assertTrue(check.check(null).isValid());
    }

    @Test
    public void testCheck_NoSourceFeature() {
        entry = entryFactory.createEntry();
        Feature feature = featureFactory.createFeature("gene");
        feature.addQualifier("gene", "10S rRNA");
        entry.addFeature(feature);
        ValidationResult result = check.check(entry);
        assertEquals(0, result.count( Severity.ERROR));
    }

    @Test
    public void testCheck_SourceNoLocation() {
        entry = entryFactory.createEntry();
        SourceFeature feature = featureFactory.createSourceFeature();
        feature.addQualifier("organism", "Mus musculus");
        ValidationResult result = check.check(entry);
        assertEquals(0, result.count( Severity.ERROR));
    }

    @Test
    public void testCheck_ValidSingleLocationSingleSource() {
        entry = entryFactory.createEntry();
        SourceFeature feature = featureFactory.createSourceFeature();
        feature.addQualifier("organism", "Mus musculus");

        SequenceFactory sequenceFactory = new SequenceFactory();
        Sequence sequence = sequenceFactory.createSequence();
        sequence.setSequence(ByteBuffer.wrap(("cttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaa" +
                "cctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtg").getBytes()));
        entry.setSequence(sequence);

        LocationFactory locationFactory = new  LocationFactory();
        Location location = locationFactory.createLocalRange(1L,180L);
        CompoundLocation<Location> compoundLocation = new Join<Location>();
        compoundLocation.addLocation(location);
        feature.setLocations(compoundLocation);

        entry.addFeature(feature);
        ValidationResult result = check.check(entry);
        assertEquals(0, result.count( Severity.ERROR));
    }

    @Test
    public void testCheck_ValidMultipleLocationSingleSource() {
        entry = entryFactory.createEntry();
        SourceFeature feature = featureFactory.createSourceFeature();
        feature.addQualifier("organism", "Mus musculus");

        SequenceFactory sequenceFactory = new SequenceFactory();
        Sequence sequence = sequenceFactory.createSequence();
        sequence.setSequence(ByteBuffer.wrap(("cttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaa" +
                "cctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtg").getBytes()));
        entry.setSequence(sequence);

        LocationFactory locationFactory = new  LocationFactory();
        Location location = locationFactory.createLocalRange(1L,180L);
        CompoundLocation<Location> compoundLocation = new Join<Location>();
        compoundLocation.addLocation(location);
        feature.setLocations(compoundLocation);

        entry.addFeature(feature);
        ValidationResult result = check.check(entry);
        assertEquals(0, result.count( Severity.ERROR));
    }

    @Test
    public void testCheck_ValidMultipleLocationMultipleSource() {
        entry = entryFactory.createEntry();
        SourceFeature feature = featureFactory.createSourceFeature();
        feature.addQualifier("organism", "Mus musculus");

        SequenceFactory sequenceFactory = new SequenceFactory();
        Sequence sequence = sequenceFactory.createSequence();
        sequence.setSequence(ByteBuffer.wrap(("cttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaa" +
                "cctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtg").getBytes()));
        entry.setSequence(sequence);

        LocationFactory locationFactory = new  LocationFactory();
        Location location = locationFactory.createLocalRange(1L,180L);
        CompoundLocation<Location> compoundLocation = new Join<Location>();
        compoundLocation.addLocation(location);
        feature.setLocations(compoundLocation);

        entry.addFeature(feature);
        ValidationResult result = check.check(entry);
        assertEquals(0, result.count( Severity.ERROR));
    }

    @Test
    public void testCheck_ValidMultipleLocationTransgenicSource() {
        entry = entryFactory.createEntry();
        SourceFeature feature = featureFactory.createSourceFeature();
        feature.addQualifier("organism", "Mus musculus");

        SequenceFactory sequenceFactory = new SequenceFactory();
        Sequence sequence = sequenceFactory.createSequence();
        sequence.setSequence(ByteBuffer.wrap(("cttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaa" +
                "cctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtg").getBytes()));
        entry.setSequence(sequence);

        LocationFactory locationFactory = new  LocationFactory();
        Location location = locationFactory.createLocalRange(1L,180L);
        CompoundLocation<Location> compoundLocation = new Join<Location>();
        compoundLocation.addLocation(location);
        feature.setLocations(compoundLocation);

        entry.addFeature(feature);
        ValidationResult result = check.check(entry);
        assertEquals(0, result.count( Severity.ERROR));
    }

    @Test
    public void testCheck_ValidTransgenicSource() {
        entry = entryFactory.createEntry();
        SequenceFactory sequenceFactory = new SequenceFactory();
        Sequence sequence = sequenceFactory.createSequence();
        sequence.setSequence(ByteBuffer.wrap(("cttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaa" +
                "cctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtg").getBytes()));
        entry.setSequence(sequence);

        SourceFeature source1 = featureFactory.createSourceFeature();
        source1.addQualifier("organism", "Mus musculus");
        source1.addQualifier("transgenic");

        LocationFactory locationFactory = new  LocationFactory();
        Location location = locationFactory.createLocalRange(1L,180L);
        CompoundLocation<Location> compoundLocation = new Join<Location>();
        compoundLocation.addLocation(location);
        source1.setLocations(compoundLocation);

        entry.addFeature(source1);

        SourceFeature source2 = featureFactory.createSourceFeature();
        source2.addQualifier("organism", "Mus musculus");

        Location location2 = locationFactory.createLocalRange(50L,100L);
        CompoundLocation<Location> compoundLocation2 = new Join<Location>();
        compoundLocation2.addLocation(location2);
        source2.setLocations(compoundLocation2);

        entry.addFeature(source2);

        ValidationResult result = check.check(entry);
        assertEquals(0, result.count( Severity.ERROR));
    }

    @Test
    public void testCheck_TransgenicSingleSource() {
        entry = entryFactory.createEntry();
        SourceFeature feature = featureFactory.createSourceFeature();
        feature.addQualifier("organism", "Mus musculus");
        feature.addQualifier("transgenic");

        SequenceFactory sequenceFactory = new SequenceFactory();
        Sequence sequence = sequenceFactory.createSequence();
        sequence.setSequence(ByteBuffer.wrap(("cttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaa" +
                "cctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtg").getBytes()));
        entry.setSequence(sequence);

        LocationFactory locationFactory = new  LocationFactory();
        Location location = locationFactory.createLocalRange(1L,180L);
        CompoundLocation<Location> compoundLocation = new Join<Location>();
        compoundLocation.addLocation(location);
        feature.setLocations(compoundLocation);

        entry.addFeature(feature);
        ValidationResult result = check.check(entry);
        assertEquals(1, result.count( Severity.ERROR));
        assertEquals(1, result.count("SequenceCoverageCheck-4", Severity.ERROR));
    }

    @Test
    public void testCheck_TransgenicWithOverlap() {
        entry = entryFactory.createEntry();
        SequenceFactory sequenceFactory = new SequenceFactory();
        Sequence sequence = sequenceFactory.createSequence();
        sequence.setSequence(ByteBuffer.wrap(("cttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaa" +
                "cctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtg").getBytes()));
        entry.setSequence(sequence);

        SourceFeature source1 = featureFactory.createSourceFeature();
        source1.addQualifier("organism", "Mus musculus");
        source1.addQualifier("transgenic");

        LocationFactory locationFactory = new  LocationFactory();
        Location location = locationFactory.createLocalRange(1L,180L);
        CompoundLocation<Location> compoundLocation = new Join<Location>();
        compoundLocation.addLocation(location);
        source1.setLocations(compoundLocation);

        entry.addFeature(source1);

        SourceFeature source2 = featureFactory.createSourceFeature();
        source2.addQualifier("organism", "Mus musculus");

        Location location2 = locationFactory.createLocalRange(50L,100L);
        CompoundLocation<Location> compoundLocation2 = new Join<Location>();
        compoundLocation2.addLocation(location2);
        source2.setLocations(compoundLocation2);

        entry.addFeature(source2);

        SourceFeature source3 = featureFactory.createSourceFeature();
        source3.addQualifier("organism", "Mus musculus");

        Location location3 = locationFactory.createLocalRange(70L,170L);
        CompoundLocation<Location> compoundLocation3 = new Join<Location>();
        compoundLocation3.addLocation(location3);
        source3.setLocations(compoundLocation3);

        entry.addFeature(source3);

        ValidationResult result = check.check(entry);
        assertEquals(1, result.count( Severity.ERROR));
        assertEquals(1, result.count("SequenceCoverageCheck-9", Severity.ERROR));
    }

    @Test
    public void testCheck_NonTransgenicWithOverlap() {
        entry = entryFactory.createEntry();
        SequenceFactory sequenceFactory = new SequenceFactory();
        Sequence sequence = sequenceFactory.createSequence();
        sequence.setSequence(ByteBuffer.wrap(("cttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaa" +
                "cctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtg").getBytes()));
        entry.setSequence(sequence);

        SourceFeature source1 = featureFactory.createSourceFeature();
        source1.addQualifier("organism", "Mus musculus");

        LocationFactory locationFactory = new  LocationFactory();
        Location location = locationFactory.createLocalRange(1L,150L);
        CompoundLocation<Location> compoundLocation = new Join<Location>();
        compoundLocation.addLocation(location);
        source1.setLocations(compoundLocation);

        entry.addFeature(source1);

        SourceFeature source2 = featureFactory.createSourceFeature();
        source2.addQualifier("organism", "Mus musculus");

        Location location2 = locationFactory.createLocalRange(150L,180L);
        CompoundLocation<Location> compoundLocation2 = new Join<Location>();
        compoundLocation2.addLocation(location2);
        source2.setLocations(compoundLocation2);

        entry.addFeature(source2);

        ValidationResult result = check.check(entry);
        assertEquals(1, result.count( Severity.ERROR));
        assertEquals(1, result.count("SequenceCoverageCheck-9", Severity.ERROR));
    }

    @Test
    public void testCheck_TransgenicNoCoverage() {
        entry = entryFactory.createEntry();
        SequenceFactory sequenceFactory = new SequenceFactory();
        Sequence sequence = sequenceFactory.createSequence();
        sequence.setSequence(ByteBuffer.wrap(("cttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaa" +
                "cctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtg").getBytes()));
        entry.setSequence(sequence);

        SourceFeature source1 = featureFactory.createSourceFeature();
        source1.addQualifier("organism", "Mus musculus");
        source1.addQualifier("transgenic");

        LocationFactory locationFactory = new  LocationFactory();
        Location location = locationFactory.createLocalRange(1L,175L);
        CompoundLocation<Location> compoundLocation = new Join<Location>();
        compoundLocation.addLocation(location);
        source1.setLocations(compoundLocation);

        entry.addFeature(source1);

        SourceFeature source2 = featureFactory.createSourceFeature();
        source2.addQualifier("organism", "Mus musculus");

        Location location2 = locationFactory.createLocalRange(50L,100L);
        CompoundLocation<Location> compoundLocation2 = new Join<Location>();
        compoundLocation2.addLocation(location2);
        source2.setLocations(compoundLocation2);

        entry.addFeature(source2);

        SourceFeature source3 = featureFactory.createSourceFeature();
        source3.addQualifier("organism", "Mus musculus");

        Location location3 = locationFactory.createLocalRange(70L,170L);
        CompoundLocation<Location> compoundLocation3 = new Join<Location>();
        compoundLocation3.addLocation(location3);
        source3.setLocations(compoundLocation3);

        entry.addFeature(source3);

        ValidationResult result = check.check(entry);
        assertEquals(2, result.count( Severity.ERROR));
        assertEquals(1, result.count("SequenceCoverageCheck-7", Severity.ERROR));
        assertEquals(1, result.count("SequenceCoverageCheck-9", Severity.ERROR));
    }

    @Test
    public void testCheck_NonTransgenicNoCoverage() {
        entry = entryFactory.createEntry();
        SequenceFactory sequenceFactory = new SequenceFactory();
        Sequence sequence = sequenceFactory.createSequence();
        sequence.setSequence(ByteBuffer.wrap(("cttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaa" +
                "cctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtg").getBytes()));
        entry.setSequence(sequence);

        SourceFeature source1 = featureFactory.createSourceFeature();
        source1.addQualifier("organism", "Mus musculus");

        LocationFactory locationFactory = new  LocationFactory();
        Location location = locationFactory.createLocalRange(1L,150L);
        CompoundLocation<Location> compoundLocation = new Join<Location>();
        compoundLocation.addLocation(location);
        source1.setLocations(compoundLocation);

        entry.addFeature(source1);

        SourceFeature source2 = featureFactory.createSourceFeature();
        source2.addQualifier("organism", "Mus musculus");

        Location location2 = locationFactory.createLocalRange(151L,170L);
        CompoundLocation<Location> compoundLocation2 = new Join<Location>();
        compoundLocation2.addLocation(location2);
        source2.setLocations(compoundLocation2);

        entry.addFeature(source2);

        ValidationResult result = check.check(entry);
        assertEquals(1, result.count( Severity.ERROR));
        assertEquals(1, result.count("SequenceCoverageCheck-1", Severity.ERROR));
    }

    @Test
    public void testCheck_MultipleLocationFocusSourceTogether() {
        entry = entryFactory.createEntry();
        SequenceFactory sequenceFactory = new SequenceFactory();
        Sequence sequence = sequenceFactory.createSequence();
        sequence.setSequence(ByteBuffer.wrap(("cttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaa" +
                "cctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtg").getBytes()));
        entry.setSequence(sequence);

        SourceFeature source1 = featureFactory.createSourceFeature();
        source1.addQualifier("organism", "Mus musculus");
        source1.addQualifier("transgenic");

        LocationFactory locationFactory = new  LocationFactory();
        Location location = locationFactory.createLocalRange(1L,180L);
        CompoundLocation<Location> compoundLocation = new Join<Location>();
        compoundLocation.addLocation(location);
        source1.setLocations(compoundLocation);

        entry.addFeature(source1);

        SourceFeature source2 = featureFactory.createSourceFeature();
        source2.addQualifier("organism", "Mus musculus");
        source2.addQualifier("focus");

        Location location2 = locationFactory.createLocalRange(50L,100L);
        CompoundLocation<Location> compoundLocation2 = new Join<Location>();
        compoundLocation2.addLocation(location2);
        source2.setLocations(compoundLocation2);

        entry.addFeature(source2);

        ValidationResult result = check.check(entry);
        assertEquals(2, result.count( Severity.ERROR));
        assertEquals(1, result.count("SequenceCoverageCheck-8", Severity.ERROR));
        assertEquals(1, result.count("SequenceCoverageCheck-4", Severity.ERROR));
    }

    @Test
    public void testCheck_SequenceSizeFromContig() {
        entry = entryFactory.createEntry();
        SequenceFactory sequenceFactory = new SequenceFactory();

        LocationFactory locationFactory = new LocationFactory();
        entry.setSequence(sequenceFactory.createSequence());
        RemoteRange remoteRange1 = locationFactory.createRemoteRange("A00001", 1, 10L, 20L);
        RemoteRange remoteRange2 = locationFactory.createRemoteRange("A00002", 1, 10L, 20L);
        RemoteRange remoteRange3 = locationFactory.createRemoteRange("A00003", 1, 10L, 20L);
        Gap gap1 = locationFactory.createGap(10);
        Gap gap2 = locationFactory.createGap(10);
        entry.getSequence().addContig(remoteRange1);
        entry.getSequence().addContig(gap1);
        entry.getSequence().addContig(remoteRange2);
        entry.getSequence().addContig(gap2);
        entry.getSequence().addContig(remoteRange3);


        SourceFeature source1 = featureFactory.createSourceFeature();
        source1.addQualifier("organism", "Mus musculus");
        source1.addQualifier("transgenic");

        Location location = locationFactory.createLocalRange(1L,53L);
        CompoundLocation<Location> compoundLocation = new Join<Location>();
        compoundLocation.addLocation(location);
        source1.setLocations(compoundLocation);

        entry.addFeature(source1);

        SourceFeature source2 = featureFactory.createSourceFeature();
        source2.addQualifier("organism", "Mus musculus");

        Location location2 = locationFactory.createLocalRange(10L,40L);
        CompoundLocation<Location> compoundLocation2 = new Join<Location>();
        compoundLocation2.addLocation(location2);
        source2.setLocations(compoundLocation2);

        entry.addFeature(source2);

        ValidationResult result = check.check(entry);
        assertEquals(0, result.count( Severity.ERROR));
    }

}