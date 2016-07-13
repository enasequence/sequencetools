/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.api.validation.fixer.entry;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.fixer.entry.CDS_RNA_LocusFix;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CDS_RNA_LocusFixTest {

	private Entry entry;
	private CDS_RNA_LocusFix check;
    public EntryFactory entryFactory;
    public FeatureFactory featureFactory;
    public LocationFactory locationFactory;
    public QualifierFactory qualifierFactory;

    @Before
	public void setUp() {
        ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);

        entryFactory = new EntryFactory();
        featureFactory = new FeatureFactory();
        locationFactory = new LocationFactory();
        qualifierFactory = new QualifierFactory();

        entry = entryFactory.createEntry();

        Sequence sequence = new SequenceFactory().createSequenceByte("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes());
        sequence.setTopology(Sequence.Topology.LINEAR);
        entry.setSequence(sequence);

        check = new CDS_RNA_LocusFix();
    }

	public void testCheck_Empty() {
        entry.setSequence(null);
        ValidationResult result = check.check(entry);
        assertTrue(result.getMessages(Severity.FIX).isEmpty());//dont make a fuss, other checks for that
    }

	@Test
	public void testCheck_NoEntry() {
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoGenes() {
        Feature feature = featureFactory.createFeature("CDS");
        Order<Location> order = new Order<Location>();
        order.addLocation(locationFactory.createLocalRange(5l,10l));
        feature.setLocations(order);
        entry.addFeature(feature);
        ValidationResult validationResult = check.check(entry);
        assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
    }

	@Test
	public void testCheck_GenesNoOverlap() {
        Feature cdsFeature = featureFactory.createFeature("CDS");
        Order<Location> order = new Order<Location>();
        order.addLocation(locationFactory.createLocalRange(5l,10l));
        cdsFeature.setLocations(order);
        Feature geneFeature = featureFactory.createFeature("gene");
        Order<Location> order2 = new Order<Location>();
        order2.addLocation(locationFactory.createLocalRange(15l,20l));
        geneFeature.setLocations(order2);

        entry.addFeature(cdsFeature);
        entry.addFeature(geneFeature);
        ValidationResult validationResult = check.check(entry);
        assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
    }

	@Test
	public void testCheck_GeneOverlapNoLack() {//i.e. the required qualifier is preset so no need to inherit
        Feature cdsFeature = featureFactory.createFeature("CDS");
        /**
         * as long as one is present (locus_tag, gene or gene_synonym) - not interested in inheriting
         */
        cdsFeature.addQualifier(qualifierFactory.createQualifier("locus_tag"));
        Order<Location> order = new Order<Location>();
        order.addLocation(locationFactory.createLocalRange(5l,10l));
        cdsFeature.setLocations(order);

        Feature geneFeature = featureFactory.createFeature("gene");
        geneFeature.addQualifier(qualifierFactory.createQualifier("locus_tag"));
        Order<Location> order2 = new Order<Location>();
        order2.addLocation(locationFactory.createLocalRange(4l,11l));
        geneFeature.setLocations(order2);

        entry.addFeature(cdsFeature);
        entry.addFeature(geneFeature);
        ValidationResult validationResult = check.check(entry);
        assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
    }

	@Test
	public void testCheck_GeneOverlapNothingInGene() {//i.e. gene has nothing to pass in
        Feature cdsFeature = featureFactory.createFeature("CDS");
        Order<Location> order = new Order<Location>();
        order.addLocation(locationFactory.createLocalRange(5l,10l));
        cdsFeature.setLocations(order);

        Feature geneFeature = featureFactory.createFeature("gene");
        Order<Location> order2 = new Order<Location>();
        order2.addLocation(locationFactory.createLocalRange(4l,11l));
        geneFeature.setLocations(order2);

        entry.addFeature(cdsFeature);
        entry.addFeature(geneFeature);
        ValidationResult validationResult = check.check(entry);
        assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
    }

	@Test
	public void testCheck_GeneOverlapInGene() {
        Feature cdsFeature = featureFactory.createFeature("CDS");
        Order<Location> order = new Order<Location>();
        order.addLocation(locationFactory.createLocalRange(5l,10l));
        cdsFeature.setLocations(order);

        Feature geneFeature = featureFactory.createFeature("gene");
        geneFeature.addQualifier(qualifierFactory.createQualifier("locus_tag"));
        geneFeature.addQualifier(qualifierFactory.createQualifier("gene_synonym"));
        Order<Location> order2 = new Order<Location>();
        order2.addLocation(locationFactory.createLocalRange(4l,11l));
        geneFeature.setLocations(order2);

        entry.addFeature(cdsFeature);
        entry.addFeature(geneFeature);
        ValidationResult validationResult = check.check(entry);
        assertEquals(1, validationResult.count("CDS_RNA_LocusFix", Severity.FIX));
        Collection<Feature> cdsFeatures = SequenceEntryUtils.getFeatures("CDS", entry);
        Feature fixedCDS = cdsFeatures.iterator().next();
        assertTrue(SequenceEntryUtils.isQualifierAvailable("locus_tag", fixedCDS));
        assertTrue(SequenceEntryUtils.isQualifierAvailable("gene_synonym", fixedCDS));
    }

    @Test
    public void testCheck_GeneOverlapCircularBoundary() {

        Sequence sequence = new SequenceFactory().createSequenceByte("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes());
        sequence.setTopology(Sequence.Topology.CIRCULAR);
        long sequenceLength = sequence.getLength();
        entry.setSequence(sequence);

        Feature cdsFeature = featureFactory.createFeature("CDS");
        Order<Location> order = new Order<Location>();
        order.addLocation(locationFactory.createLocalRange(5l,sequenceLength));
        order.addLocation(locationFactory.createLocalRange(1l,3l));
        cdsFeature.setLocations(order);

        Feature geneFeature = featureFactory.createFeature("gene");
        geneFeature.addQualifier(qualifierFactory.createQualifier("locus_tag"));
        geneFeature.addQualifier(qualifierFactory.createQualifier("gene_synonym"));
        Order<Location> order2 = new Order<Location>();
        order2.addLocation(locationFactory.createLocalRange(4l,11l));
        geneFeature.setLocations(order2);

        entry.addFeature(cdsFeature);
        entry.addFeature(geneFeature);
        ValidationResult validationResult = check.check(entry);
        assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
    }

    @Test
    public void testCheck_GeneOverlapDifferentStrands() {

        Sequence sequence = new SequenceFactory().createSequenceByte("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes());
        sequence.setTopology(Sequence.Topology.CIRCULAR);
        long sequenceLength = sequence.getLength();
        entry.setSequence(sequence);

        Feature cdsFeature = featureFactory.createFeature("CDS");
        Order<Location> order = new Order<Location>();
        order.addLocation(locationFactory.createLocalRange(5l,sequenceLength));
        order.addLocation(locationFactory.createLocalRange(1l,3l));
        order.setComplement(false);
        cdsFeature.setLocations(order);

        Feature geneFeature = featureFactory.createFeature("gene");
        geneFeature.addQualifier(qualifierFactory.createQualifier("locus_tag"));
        geneFeature.addQualifier(qualifierFactory.createQualifier("gene_synonym"));
        Order<Location> order2 = new Order<Location>();
        order2.addLocation(locationFactory.createLocalRange(4l,11l));
        order2.setComplement(true);
        geneFeature.setLocations(order2);

        entry.addFeature(cdsFeature);
        entry.addFeature(geneFeature);
        ValidationResult validationResult = check.check(entry);
        assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
    }
}
