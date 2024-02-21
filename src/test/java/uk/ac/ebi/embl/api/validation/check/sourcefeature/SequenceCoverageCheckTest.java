/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.validation.check.sourcefeature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.sql.SQLException;

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
import uk.ac.ebi.embl.api.validation.helper.TestHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;

public class SequenceCoverageCheckTest {
    private Entry entry;
    private SequenceCoverageCheck check;
    private final FeatureFactory featureFactory = new FeatureFactory();
    private final EntryFactory entryFactory = new EntryFactory();
    private final LocationFactory locationFactory = new LocationFactory();
    private final SequenceFactory sequenceFactory = new SequenceFactory();

    static {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    }

    @Before
    public void setUp() throws SQLException {
        check = new SequenceCoverageCheck();

        EmblEntryValidationPlanProperty planProperty = TestHelper.testEmblEntryValidationPlanProperty();
        planProperty.taxonClient.set(new TaxonomyClient());
        check.setEmblEntryValidationPlanProperty(planProperty);

        entry = entryFactory.createEntry();
        Sequence sequence = sequenceFactory.createSequence();
        sequence.setSequence(
                ByteBuffer.wrap(
                        ("cttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaa"
                                + "cctgcgcacagttagctacgggtctaatggagtgcttgtttaaggtttaacgatagcaaacctgcgcacagttagctacgggtctaatggagtg")
                                .getBytes()));
        entry.setSequence(sequence);
    }

    private SourceFeature addSource(Location... locations) {
        SourceFeature source = featureFactory.createSourceFeature();
        source.addQualifier("organism", "Mus musculus");
        CompoundLocation<Location> compoundLocation = new Join<>();
        for (Location location : locations) {
            compoundLocation.addLocation(location);
        }
        source.setLocations(compoundLocation);
        entry.addFeature(source);
        return source;
    }

    @Test
    public void testCheck_NoEntry() {
        assertTrue(check.check(null).isValid());
    }

    @Test
    public void testCheck_NoSourceFeature() {
        Feature feature = featureFactory.createFeature("gene");
        feature.addQualifier("gene", "10S rRNA");
        entry.addFeature(feature);
        ValidationResult result = check.check(entry);
        assertEquals(0, result.count(Severity.ERROR));
    }

    @Test
    public void testCheck_NoSourceLocation() {
        SourceFeature feature = featureFactory.createSourceFeature();
        feature.addQualifier("organism", "Mus musculus");
        ValidationResult result = check.check(entry);
        assertEquals(0, result.count(Severity.ERROR));
    }

    @Test
    public void testCheck_ValidSingleSourceSingleLocation() {
        addSource(locationFactory.createLocalRange(1L, 180L));

        ValidationResult result = check.check(entry);
        assertEquals(0, result.count(Severity.ERROR));
    }

    @Test
    public void testCheck_ValidSingleSourceMultipleLocations() {
        addSource(locationFactory.createLocalRange(1L, 90L),
                locationFactory.createLocalRange(91L, 179L),
                locationFactory.createLocalBase(180L));

        ValidationResult result = check.check(entry);
        assertEquals(0, result.count(Severity.ERROR));
    }

    @Test
    public void testCheck_ValidMultipleSourceMultipleLocation() {
        addSource(locationFactory.createLocalRange(1L, 90L),
                locationFactory.createLocalRange(91L, 179L));
        addSource(locationFactory.createLocalBase(180L));

        ValidationResult result = check.check(entry);
        assertEquals(0, result.count(Severity.ERROR));
    }

    @Test
    public void testCheck_Overlap() {
        addSource(locationFactory.createLocalRange(1L, 90L),
                locationFactory.createLocalRange(91L, 179L));
        addSource(locationFactory.createLocalRange(91L, 180L));

        ValidationResult result = check.check(entry);
        assertEquals(1, result.count(Severity.ERROR));
        assertEquals(1, result.count("SequenceCoverageCheck-9", Severity.ERROR));
    }

    @Test
    public void testCheck_Gap() {
        addSource(locationFactory.createLocalRange(1L, 90L),
                locationFactory.createLocalRange(91L, 160L));
        addSource(locationFactory.createLocalRange(170L, 180L));

        ValidationResult result = check.check(entry);
        assertEquals(2, result.count(Severity.ERROR));
        assertEquals(1, result.count("SequenceCoverageCheck-5", Severity.ERROR));
        assertEquals(1, result.count("SequenceCoverageCheck-3", Severity.ERROR));
    }

    @Test
    public void testCheck_PartialCoverage() {
        addSource(locationFactory.createLocalRange(1L, 150L));
        addSource(locationFactory.createLocalRange(151L, 170L));

        ValidationResult result = check.check(entry);
        assertEquals(2, result.count(Severity.ERROR));
        assertEquals(1, result.count("SequenceCoverageCheck-1", Severity.ERROR));
        assertEquals(1, result.count("SequenceCoverageCheck-3", Severity.ERROR));

    }

    @Test
    public void testCheck_ValidTransgenic() {
        SourceFeature transgenicSource = addSource(locationFactory.createLocalRange(1L, 90L),
                locationFactory.createLocalRange(91L, 180L));
        transgenicSource.addQualifier("transgenic");
        // Other source.
        addSource(locationFactory.createLocalRange(1L, 90L));

        ValidationResult result = check.check(entry);
        assertEquals(0, result.count(Severity.ERROR));
    }

    @Test
    public void testCheck_OnlyTransgenic() {
        SourceFeature transgenicSource = addSource(locationFactory.createLocalRange(1L, 90L),
                locationFactory.createLocalRange(91L, 180L));
        transgenicSource.addQualifier("transgenic");

        ValidationResult result = check.check(entry);
        assertEquals(1, result.count(Severity.ERROR));
        assertEquals(1, result.count("SequenceCoverageCheck-4", Severity.ERROR));
    }

    @Test
    public void testCheck_TransgenicWithOverlap() {
        SourceFeature transgenicSource = addSource(locationFactory.createLocalRange(1L, 90L),
                locationFactory.createLocalRange(91L, 180L));
        transgenicSource.addQualifier("transgenic");
        addSource(locationFactory.createLocalRange(91L, 179L));
        addSource(locationFactory.createLocalRange(91L, 179L));

        ValidationResult result = check.check(entry);
        assertEquals(1, result.count(Severity.ERROR));
        assertEquals(1, result.count("SequenceCoverageCheck-9", Severity.ERROR));
    }

    @Test
    public void testCheck_TransgenicWithGap() {
        SourceFeature transgenicSource = addSource(locationFactory.createLocalRange(1L, 90L),
                locationFactory.createLocalRange(91L, 180L));
        transgenicSource.addQualifier("transgenic");
        addSource(locationFactory.createLocalRange(91L, 110L));
        addSource(locationFactory.createLocalRange(150L, 179L));

        ValidationResult result = check.check(entry);
        assertEquals(0, result.count(Severity.ERROR));
    }

    @Test
    public void testCheck_TransgenicNotFullCoverage() {
        SourceFeature transgenicSource = addSource(locationFactory.createLocalRange(1L, 90L),
                locationFactory.createLocalRange(91L, 179L));
        transgenicSource.addQualifier("transgenic");
        addSource(locationFactory.createLocalRange(91L, 179L));

        ValidationResult result = check.check(entry);
        assertEquals(2, result.count(Severity.ERROR));
        assertEquals(1, result.count("SequenceCoverageCheck-1", Severity.ERROR));
        assertEquals(1, result.count("SequenceCoverageCheck-7", Severity.ERROR));
    }

    @Test
    public void testCheck_TransgenicAndFocus() {
        SourceFeature transgenicSource = addSource(
                locationFactory.createLocalRange(1L, 180L));
        transgenicSource.addQualifier("transgenic");
        SourceFeature focusSource = addSource(
                locationFactory.createLocalRange(50L, 100L));
        focusSource.addQualifier("focus");

        ValidationResult result = check.check(entry);
        assertEquals(1, result.count(Severity.ERROR));
        assertEquals(1, result.count("SequenceCoverageCheck-8", Severity.ERROR));
    }

    @Test
    public void testCheck_ValidContigCoverage() {
        Sequence sequence = sequenceFactory.createSequence();
        entry.setSequence(sequence);
        RemoteRange remoteRange1 = locationFactory.createRemoteRange("A00001", 1, 10L, 20L);
        RemoteRange remoteRange2 = locationFactory.createRemoteRange("A00002", 1, 10L, 20L);
        RemoteRange remoteRange3 = locationFactory.createRemoteRange("A00003", 1, 10L, 20L);
        Gap gap1 = locationFactory.createGap(10);
        Gap gap2 = locationFactory.createGap(10);
        sequence.addContig(remoteRange1);
        sequence.addContig(gap1);
        sequence.addContig(remoteRange2);
        sequence.addContig(gap2);
        sequence.addContig(remoteRange3);

        addSource(locationFactory.createLocalRange(1L, sequence.getLength()));

        ValidationResult result = check.check(entry);
        assertEquals(0, result.count(Severity.ERROR));
    }
}
