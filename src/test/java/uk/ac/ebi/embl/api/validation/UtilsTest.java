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
package uk.ac.ebi.embl.api.validation;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.qualifier.AnticodonQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.Rpt_Unit_RangeQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.Tag_PeptideQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.TranslExceptQualifier;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.helper.Utils;

public class UtilsTest {

  private EntryFactory entryFactory;
  private SequenceFactory sequenceFactory;
  private FeatureFactory featureFactory;
  private LocationFactory locationFactory;
  private ReferenceFactory referenceFactory;

  @Before
  public void setUp() throws Exception {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
    entryFactory = new EntryFactory();
    sequenceFactory = new SequenceFactory();
    featureFactory = new FeatureFactory();
    locationFactory = new LocationFactory();
    referenceFactory = new ReferenceFactory();
  }

  // test for shiftLocation Method of Utils

  @Test
  public void testCheck_featureLocationBeginandEndwithNs() {
    int beginN = 10;
    Entry entry = entryFactory.createEntry();
    Sequence newsequence =
        sequenceFactory.createSequenceByte("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
    entry.setSequence(newsequence);
    Feature feature1 = featureFactory.createFeature("feature1");
    Order<Location> order1 = new Order<Location>();
    order1.addLocation(locationFactory.createLocalRange((long) 1, (long) 8));
    feature1.setLocations(order1);
    Feature feature2 = featureFactory.createFeature("feature2");
    Order<Location> order2 = new Order<Location>();
    order2.addLocation(locationFactory.createLocalRange((long) 40, (long) 46));
    feature2.setLocations(order2);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    assertEquals(2, Utils.shiftLocation(entry, beginN, false).size());
  }

  @Test
  public void testCheck_featureLocationBeginandEndwithinnewSequence() {
    int beginN = 10;
    Entry entry = entryFactory.createEntry();
    Sequence newsequence =
        sequenceFactory.createSequenceByte("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
    entry.setSequence(newsequence);
    Feature feature1 = featureFactory.createFeature("feature1");
    Order<Location> order1 = new Order<Location>();
    order1.addLocation(locationFactory.createLocalRange((long) 12, (long) 20));
    feature1.setLocations(order1);
    entry.addFeature(feature1);
    assertEquals(0, Utils.shiftLocation(entry, beginN, false).size());
  }

  @Test
  public void testCheck_featureLocationBeginOREndoutofnewSequence() {
    int beginN = 10;
    Entry entry = entryFactory.createEntry();
    Sequence newsequence =
        sequenceFactory.createSequenceByte("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
    entry.setSequence(newsequence);
    Feature feature1 = featureFactory.createFeature("feature1");
    Order<Location> order1 = new Order<Location>();
    order1.addLocation(locationFactory.createLocalRange((long) 1, (long) 20));
    Feature feature2 = featureFactory.createFeature("feature2");
    Order<Location> order2 = new Order<Location>();
    order2.addLocation(locationFactory.createLocalRange((long) 20, (long) 36));
    feature2.setLocations(order2);
    feature1.setLocations(order1);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    assertEquals(0, Utils.shiftLocation(entry, beginN, false).size());
  }

  @Test
  public void testCheck_featureLocationBeginandEndequals() {
    int beginN = 10;
    Entry entry = entryFactory.createEntry();
    Sequence newsequence =
        sequenceFactory.createSequenceByte("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
    entry.setSequence(newsequence);
    Feature feature1 = featureFactory.createFeature("feature1");
    Order<Location> order1 = new Order<Location>();
    order1.addLocation(locationFactory.createLocalRange((long) 1, (long) 11));
    Feature feature2 = featureFactory.createFeature("feature2");
    Order<Location> order2 = new Order<Location>();
    order2.addLocation(locationFactory.createLocalRange((long) 36, (long) 46));
    feature2.setLocations(order2);
    feature1.setLocations(order1);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    assertEquals(2, Utils.shiftLocation(entry, beginN, false).size());
    // Collection<ValidationMessage> validationMessages=Utils.shiftLocation(entry, beginN);

  }

  @Test
  public void testCheck_GapLocation() {
    int beginN = 10;
    Entry entry = entryFactory.createEntry();
    Sequence newsequence =
        sequenceFactory.createSequenceByte("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
    entry.setSequence(newsequence);
    Feature feature1 = featureFactory.createFeature("gap");
    Order<Location> order1 = new Order<Location>();
    order1.addLocation(locationFactory.createLocalRange((long) 40, (long) 46));
    Feature feature2 = featureFactory.createFeature("gap");
    Order<Location> order2 = new Order<Location>();
    order2.addLocation(locationFactory.createLocalRange((long) 1, (long) 8));
    feature2.setLocations(order2);
    feature1.setLocations(order1);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    assertEquals(2, Utils.shiftLocation(entry, beginN, false).size());
    Collection<ValidationMessage> validationMessages = Utils.shiftLocation(entry, beginN, false);
  }

  @Test
  public void testCheck_SequencewithallNs() {
    int beginN = 40;
    Entry entry = entryFactory.createEntry();
    Sequence newsequence = sequenceFactory.createSequenceByte("".getBytes());
    entry.setSequence(newsequence);
    Feature feature1 = featureFactory.createFeature("feature1");
    Order<Location> order1 = new Order<Location>();
    order1.addLocation(locationFactory.createLocalRange((long) 20, (long) 26));
    Feature feature2 = featureFactory.createFeature("gap");
    Order<Location> order2 = new Order<Location>();
    order2.addLocation(locationFactory.createLocalRange((long) 14, (long) 30));
    feature2.setLocations(order2);
    feature1.setLocations(order1);
    entry.addFeature(feature1);
    entry.addFeature(feature2);
    assertEquals(2, Utils.shiftLocation(entry, beginN, false).size());
  }

  @Test
  public void testCheck_QualifierWithInvalidLocation() {
    int beginN = 40;
    Feature feature = featureFactory.createFeature("feature1");
    AnticodonQualifier antiCodonqualifier = new AnticodonQualifier("(pos:39..300,aa:Phe)");
    TranslExceptQualifier translExceptqualifier = new TranslExceptQualifier("(pos:20..30,aa:Trp)");
    Rpt_Unit_RangeQualifier rptUnitRangequalifier = new Rpt_Unit_RangeQualifier("950..960");
    Tag_PeptideQualifier tagPeptidequalifier = new Tag_PeptideQualifier("30..40");
    Order<Location> order1 = new Order<Location>();
    order1.addLocation(locationFactory.createLocalRange((long) 1, (long) 900));
    feature.setLocations(order1);
    feature.addQualifier(translExceptqualifier);
    feature.addQualifier(antiCodonqualifier);
    feature.addQualifier(rptUnitRangequalifier);
    feature.addQualifier(tagPeptidequalifier);
    // assertEquals("Utility_shift_Location_3",Utils.shiftLocationQualifier(translExceptqualifier,
    // beginN, feature).getMessageKey());
    // assertEquals("Utility_shift_Location_3",Utils.shiftLocationQualifier(antiCodonqualifier,
    // beginN, feature).getMessageKey());
    assertEquals(
        "Utility_shift_Location_3",
        Utils.shiftLocationQualifier(rptUnitRangequalifier, beginN, feature).getMessageKey());
    assertEquals(
        "Utility_shift_Location_3",
        Utils.shiftLocationQualifier(tagPeptidequalifier, beginN, feature).getMessageKey());
  }

  @Test
  public void testCheck_QualifierWithValidLocation() {
    int beginN = 400;
    Feature feature = featureFactory.createFeature("feature1");
    AnticodonQualifier antiCodonqualifier = new AnticodonQualifier("(pos:50..300,aa:Phe)");
    TranslExceptQualifier translExceptqualifier = new TranslExceptQualifier("(pos:100..30,aa:Trp)");
    Rpt_Unit_RangeQualifier rptUnitRangequalifier = new Rpt_Unit_RangeQualifier("200..920");
    Tag_PeptideQualifier tagPeptidequalifier = new Tag_PeptideQualifier("60..90");
    Order<Location> order1 = new Order<Location>();
    order1.addLocation(locationFactory.createLocalRange((long) 1, (long) 900));
    feature.setLocations(order1);
    feature.addQualifier(translExceptqualifier);
    feature.addQualifier(antiCodonqualifier);
    feature.addQualifier(rptUnitRangequalifier);
    feature.addQualifier(tagPeptidequalifier);
    // assertEquals("Utility_shift_Location_3",Utils.shiftLocationQualifier(translExceptqualifier,
    // beginN, feature).getMessageKey());
    // assertEquals("Utility_shift_Location_3",Utils.shiftLocationQualifier(antiCodonqualifier,
    // beginN, feature).getMessageKey());
    assertEquals(
        "Utility_shift_Location_3",
        Utils.shiftLocationQualifier(rptUnitRangequalifier, beginN, feature).getMessageKey());
    assertEquals(
        "Utility_shift_Location_3",
        Utils.shiftLocationQualifier(tagPeptidequalifier, beginN, feature).getMessageKey());
  }

  @Test
  public void testCheck_referenceWithValidLocation() {
    int newSequenceLength = 40;
    Entry entry = entryFactory.createEntry();
    Reference reference = referenceFactory.createReference();
    Order<LocalRange> order1 = new Order<LocalRange>();
    order1.addLocation(locationFactory.createLocalRange((long) 1, (long) 900));
    reference.setLocations(order1);
    entry.addReference(reference);
    assertNull(Utils.shiftReferenceLocation(entry, newSequenceLength));
  }

  @Test
  public void testCheck_referenceWithInValidLocation() {
    int newSequenceLength = 1;
    Entry entry = entryFactory.createEntry();
    Reference reference = referenceFactory.createReference();
    Order<LocalRange> order1 = new Order<LocalRange>();
    order1.addLocation(locationFactory.createLocalRange((long) 1, (long) 900));
    reference.setLocations(order1);
    entry.addReference(reference);
    assertNotNull(Utils.shiftReferenceLocation(entry, newSequenceLength));
  }

  // remove features test
  @Test
  public void testCheck_removeInvalidFeature() {
    int beginN = 40;
    boolean removeall = true;
    Entry entry = entryFactory.createEntry();
    Sequence newsequence = sequenceFactory.createSequenceByte("".getBytes());
    entry.setSequence(newsequence);
    Feature feature1 = featureFactory.createFeature("feature1");
    Order<Location> order1 = new Order<Location>();
    order1.addLocation(locationFactory.createLocalRange((long) 20, (long) 26));
    feature1.setLocations(order1);
    entry.addFeature(feature1);
    assertEquals(1, Utils.shiftLocation(entry, beginN, removeall).size());
  }

  // feature begin and end position equal test removeall true
  @Test
  public void testCheck_featureBeginEndPositionEqual1() {
    int beginN = 40;
    boolean removeall = true;
    Entry entry = entryFactory.createEntry();
    Sequence newsequence = sequenceFactory.createSequenceByte("ADFSGDFHGHJK".getBytes());
    entry.setSequence(newsequence);
    Feature feature1 = featureFactory.createFeature("feature1");
    Order<Location> order1 = new Order<Location>();
    order1.addLocation(locationFactory.createLocalRange((long) 45, (long) 45));
    feature1.setLocations(order1);
    entry.addFeature(feature1);
    assertEquals(0, Utils.shiftLocation(entry, beginN, removeall).size());
  }

  // feature begin and end position equal test removeall false
  @Test
  public void testCheck_featureBeginEndPositionEqual2() {
    int beginN = 40;
    boolean removeall = false;
    Entry entry = entryFactory.createEntry();
    Sequence newsequence = sequenceFactory.createSequenceByte("ADFSGDFHGHJK".getBytes());
    entry.setSequence(newsequence);
    Feature feature1 = featureFactory.createFeature("feature1");
    Order<Location> order1 = new Order<Location>();
    order1.addLocation(locationFactory.createLocalRange((long) 45, (long) 45));
    feature1.setLocations(order1);
    entry.addFeature(feature1);
    assertEquals(1, Utils.shiftLocation(entry, beginN, removeall).size());
  }

  @Test
  public void escapeASCIIHtmlEntities() {
    // null
    assertNull(Utils.escapeASCIIHtmlEntities(null));
    // string with multiple spaces
    StringBuilder ip = new StringBuilder("   ");
    Assert.assertEquals("   ", Utils.escapeASCIIHtmlEntities(ip).toString());
    // string with single space
    ip = new StringBuilder(" ");
    Assert.assertEquals(" ", Utils.escapeASCIIHtmlEntities(ip).toString());
    // empty string
    ip = new StringBuilder();
    Assert.assertEquals("", Utils.escapeASCIIHtmlEntities(ip).toString());
    // Valid html entity name &amp;
    ip = new StringBuilder("gene &amp; protein");
    Assert.assertEquals("gene & protein", Utils.escapeASCIIHtmlEntities(ip).toString());
    // html entity name without ending ; &amp
    ip = new StringBuilder("gene &amp protein");
    Assert.assertEquals("gene &amp protein", Utils.escapeASCIIHtmlEntities(ip).toString());
    // valid html entity number &#45;
    ip = new StringBuilder("gene &#45; protein");
    Assert.assertEquals("gene - protein", Utils.escapeASCIIHtmlEntities(ip).toString());
    // html entity number without ending ; &#45
    ip = new StringBuilder("gene &#45 protein");
    Assert.assertEquals("gene &#45 protein", Utils.escapeASCIIHtmlEntities(ip).toString());
    // invalid html entity number followed by a valid
    ip = new StringBuilder("gene &#45&#45; protein");
    Assert.assertEquals("gene &#45- protein", Utils.escapeASCIIHtmlEntities(ip).toString());
    // invalid html entity name followed by a valid
    ip = new StringBuilder("gene &gt&gt; protein &#45; organ");
    Assert.assertEquals("gene &gt> protein - organ", Utils.escapeASCIIHtmlEntities(ip).toString());
    // multiple html entity number
    ip = new StringBuilder("gene &#45; protein &#45; organ");
    Assert.assertEquals("gene - protein - organ", Utils.escapeASCIIHtmlEntities(ip).toString());
    // multiple html entity number with html entity name
    ip = new StringBuilder("gene &gt; protein &amp; &#45; organ");
    Assert.assertEquals("gene > protein & - organ", Utils.escapeASCIIHtmlEntities(ip).toString());
    // without html entity
    ip = new StringBuilder("gene & protein - organ");
    Assert.assertEquals("gene & protein - organ", Utils.escapeASCIIHtmlEntities(ip).toString());
    // without html entity
    ip = new StringBuilder("gene protein organ");
    Assert.assertEquals("gene protein organ", Utils.escapeASCIIHtmlEntities(ip).toString());
    // non ascii html entity number
    ip = new StringBuilder("gene &#127; protein - organ");
    Assert.assertEquals(
        "gene &#127; protein - organ", Utils.escapeASCIIHtmlEntities(ip).toString());
    // non ascii html entity name and number
    ip = new StringBuilder("gene &#127; protein &Ccedil; organ");
    Assert.assertEquals(
        "gene &#127; protein &Ccedil; organ", Utils.escapeASCIIHtmlEntities(ip).toString());
  }

  @Test
  public void testCreateRange() {

    // only one
    List<Text> outpout = Utils.createRange(getSecondaryAccnList("ARZ001"), null);
    assertEquals(1, outpout.size());
    assertEquals("ARZ001", outpout.get(0).getText());

    // empty input
    outpout = Utils.createRange(new ArrayList<>(), null);
    assertEquals(0, outpout.size());

    outpout = Utils.createRange(null, null);
    assertEquals(0, outpout.size());

    // No ranges all single
    outpout =
        Utils.createRange(
            getSecondaryAccnList("ARZ001", "ARZ002", "ARZ003", "ARZ004", "ARZ005", "ARZ011"), null);
    assertEquals(2, outpout.size());
    assertEquals("ARZ001-ARZ005", outpout.get(0).getText());
    assertEquals("ARZ011", outpout.get(1).getText());

    // Singles and ranges combined
    outpout =
        Utils.createRange(
            getSecondaryAccnList("ARZ001-ARZ007", "ARZ011", "ARZ012", "ARZ013-ARZ017"), null);
    assertEquals(3, outpout.size());
    assertEquals("ARZ001-ARZ007", outpout.get(0).getText());
    assertEquals("ARZ011-ARZ012", outpout.get(1).getText());
    assertEquals("ARZ013-ARZ017", outpout.get(2).getText());

    // different sequence
    outpout =
        Utils.createRange(
            getSecondaryAccnList("ARZ000", "ARZ001-ARZ007", "ARZ011", "ARZ013", "ARZ019", "CRX1"),
            null);
    assertEquals(6, outpout.size());
    assertEquals("ARZ000", outpout.get(0).getText());
    assertEquals("ARZ001-ARZ007", outpout.get(1).getText());
    assertEquals("ARZ011", outpout.get(2).getText());
    assertEquals("ARZ013", outpout.get(3).getText());
    assertEquals("ARZ019", outpout.get(4).getText());
    assertEquals("CRX1", outpout.get(5).getText());

    // "ARZ001", "ARZ0002"(ARZ0002 with an extra 0, should not form a range)
    outpout =
        Utils.createRange(
            getSecondaryAccnList("ARZ001", "ARZ0002", "ARZ003", "ARZ004", "ARZ005", "ARZ011"),
            null);
    assertEquals(4, outpout.size());
    assertEquals("ARZ001", outpout.get(0).getText());
    assertEquals("ARZ0002", outpout.get(1).getText());
    assertEquals("ARZ003-ARZ005", outpout.get(2).getText());
    assertEquals("ARZ011", outpout.get(3).getText());

    // "ARZ001","ARA001" numbers are contiguous but different prefix should not form range
    outpout =
        Utils.createRange(
            getSecondaryAccnList(
                "ARZ001", "ARA002", "ARZ0002", "ARZ003", "ARZ004", "ARZ005", "ARZ011"),
            null);
    assertEquals(5, outpout.size());
    assertEquals("ARZ001", outpout.get(0).getText());
    assertEquals("ARA002", outpout.get(1).getText());
    assertEquals("ARZ0002", outpout.get(2).getText());
    assertEquals("ARZ003-ARZ005", outpout.get(3).getText());
    assertEquals("ARZ011", outpout.get(4).getText());
  }

  @Test
  public void expandRanges() {
    List<Text> output =
        Utils.expandRanges(
            getSecondaryAccnListAsArray("ARZB01000005-ARZB01000009", "BBZB01000009")); // old WGS
    for (int i = 0; i <= 4; i++) {
      assertEquals("ARZB0100000" + (5 + i), output.get(i).getText());
    }
    assertEquals("BBZB01000009", output.get(output.size() - 1).getText());

    output =
        Utils.expandRanges(
            getSecondaryAccnListAsArray(
                "ARZB01S000005-ARZB01000009",
                "BBZB01000009",
                "BBCCDD01S000005001-BBCCDD01S000005004")); // with S, new WGS format
    for (int i = 0; i <= 4; i++) {
      assertEquals("ARZB01S00000" + (5 + i), output.get(i).getText());
    }
    assertEquals("BBZB01000009", output.get(5).getText());
    for (int i = 6; i < output.size(); i++) {
      assertEquals("BBCCDD01S00000500" + (i - 5), output.get(i).getText());
    }

    output = Utils.expandRanges(getSecondaryAccnListAsArray("A12345-A12349")); // STD format 1 5
    for (int i = 0; i <= 4; i++) {
      assertEquals("A1234" + (5 + i), output.get(i).getText());
    }

    output =
        Utils.expandRanges(
            getSecondaryAccnListAsArray(
                "ARZB01000015-ARZB010000115")); // ok to reduce one leading 0 at end
    // "ARZB01000015-ARZB01000115"
    assertEquals(101, output.size());
    assertEquals(
        "ARZB01000115",
        output.get(output.size() - 1).getText()); // Note, one leading reduced 4 2 7 become 4 2 6
    assertEquals("ARZB01000114", output.get(output.size() - 2).getText());
  }

  private Text[] getSecondaryAccnListAsArray(String... accns) {
    List<Text> list = getSecondaryAccnList(accns);
    Text[] array = new Text[list.size()];
    return list.toArray(array);
  }

  private List<Text> getSecondaryAccnList(String... accns) {
    return Stream.of(accns).map(Text::new).collect(Collectors.toList());
  }
}
