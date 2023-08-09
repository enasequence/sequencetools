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
package uk.ac.ebi.embl.api.validation.check.sequence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.location.*;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

/**
 * Created by IntelliJ IDEA. User: lbower Date: 26-Jan-2009 Time: 10:50:52 To change this template
 * use File | Settings | File Templates.
 */
public class SequenceBasesCheckTest {

  private Sequence sequence;
  private SequenceBasesCheck check;

  @Before
  public void setUp() {
    SequenceFactory sequenceFactory = new SequenceFactory();
    sequence = sequenceFactory.createSequence();

    check = new SequenceBasesCheck();
  }

  @Test
  public void testCheck_NoSequence() {
    assertTrue(check.check(null).isValid());
  }

  @Test
  public void testCheck_NoBases() {
    assertTrue(check.check(sequence).isValid());
  }

  @Test
  public void testCheck_InvalidBase() {
    sequence.setTopology(Sequence.Topology.LINEAR);
    sequence.setSequence(ByteBuffer.wrap("aaxaa".getBytes()));
    // sequence.setLength(5);
    ValidationResult validationResult = check.check(sequence);
    assertEquals(1, validationResult.count("SequenceBasesCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_InvalidBase2() {
    sequence.setTopology(Sequence.Topology.LINEAR);
    sequence.setSequence(ByteBuffer.wrap("aaaaaN".getBytes()));
    // sequence.setLength(6);
    ValidationResult validationResult = check.check(sequence);
    assertEquals(1, validationResult.count("SequenceBasesCheck", Severity.ERROR));
  }

  @Test
  public void testCheck_TerminalN() {
    sequence.setTopology(Sequence.Topology.LINEAR);
    sequence.setSequence(ByteBuffer.wrap("naaaaa".getBytes()));
    // sequence.setLength(6);
    ValidationResult validationResult = check.check(sequence);
    assertEquals(1, validationResult.count("SequenceBasesCheck-2", Severity.ERROR));
  }

  @Test
  public void testCheck_TerminalN2() {
    sequence.setTopology(Sequence.Topology.LINEAR);
    sequence.setSequence(ByteBuffer.wrap("aaaaan".getBytes()));
    // sequence.setLength(6);
    ValidationResult validationResult = check.check(sequence);
    assertEquals(1, validationResult.count("SequenceBasesCheck-2", Severity.ERROR));
  }

  @Test
  public void testCheck_Fine() {
    sequence.setTopology(Sequence.Topology.LINEAR);
    sequence.setSequence(ByteBuffer.wrap("aaaaa".getBytes()));
    // sequence.setLength(5);
    ValidationResult validationResult = check.check(sequence);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void testCheck_Fine2() {
    sequence.setTopology(Sequence.Topology.CIRCULAR);
    sequence.setSequence(ByteBuffer.wrap("aaaaan".getBytes()));
    // sequence.setLength(6);
    ValidationResult validationResult = check.check(sequence);
    assertTrue(validationResult.isValid());
  }
}
