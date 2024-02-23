/*
 * Copyright 2019-2024 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.validation.fixer.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;

public class ReferencePositionFixTest {

  private Entry entry;
  private ReferencePositionFix check;
  public EntryFactory entryFactory;
  public FeatureFactory featureFactory;
  public LocationFactory locationFactory;
  public QualifierFactory qualifierFactory;

  @Before
  public void setUp() {
    ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
    entryFactory = new EntryFactory();
    entry = entryFactory.createEntry();
    Sequence sequence =
        new SequenceFactory()
            .createSequenceByte("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes());
    entry.setSequence(sequence);
    check = new ReferencePositionFix();
  }

  public void testCheck_Empty() {
    entry.setSequence(null);
    ValidationResult result = check.check(entry);
    assertTrue(result.getMessages(Severity.FIX).isEmpty());
  }

  @Test
  public void testCheck_NoEntry() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoReference() {
    ValidationResult validationResult = check.check(entry);
    assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
  }

  @Test
  public void testCheck_RPgreaterThanSequence() {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Reference reference = referenceFactory.createReference();
    LocalRange location =
        (new LocationFactory()).createLocalRange(Long.valueOf(1), Long.valueOf(300));
    reference.getLocations().addLocation(location);
    entry.addReference(reference);
    ValidationResult validationResult = check.check(entry);
    assertEquals(1, validationResult.getMessages(Severity.FIX).size());
  }

  @Test
  public void testCheck_RPlessThanOne() {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Reference reference = referenceFactory.createReference();
    LocalRange location =
        (new LocationFactory()).createLocalRange(Long.valueOf(0), Long.valueOf(10));
    reference.getLocations().addLocation(location);
    entry.addReference(reference);
    ValidationResult validationResult = check.check(entry);
    assertEquals(1, validationResult.getMessages(Severity.FIX).size());
  }

  @Test
  public void testCheck_RPoutOfRange() {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Reference reference = referenceFactory.createReference();
    LocalRange location =
        (new LocationFactory()).createLocalRange(Long.valueOf(0), Long.valueOf(400));
    reference.getLocations().addLocation(location);
    entry.addReference(reference);
    ValidationResult validationResult = check.check(entry);
    assertEquals(1, validationResult.getMessages(Severity.FIX).size());
  }

  @Test
  public void testCheck_RPinRange() {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Reference reference = referenceFactory.createReference();
    LocalRange location =
        (new LocationFactory()).createLocalRange(Long.valueOf(1), Long.valueOf(10));
    reference.getLocations().addLocation(location);
    entry.addReference(reference);
    ValidationResult validationResult = check.check(entry);
    assertEquals(0, validationResult.getMessages(Severity.FIX).size());
  }
}
