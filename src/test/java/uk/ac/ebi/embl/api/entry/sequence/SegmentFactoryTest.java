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
package uk.ac.ebi.embl.api.entry.sequence;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.location.*;
import uk.ac.ebi.embl.api.service.SequenceRetrievalService;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;

public class SegmentFactoryTest implements Sequences {

  private final SegmentFactory segmentFactory = new SegmentFactory();

  @Before
  public void setUp() throws Exception {}

  @Test
  public void testCreateLocalBaseSegment() {
    SequenceFactory sequenceFactory = new SequenceFactory();
    Sequence sequence = sequenceFactory.createSequenceByte(SEQ1.getBytes());
    LocationFactory locationFactory = new LocationFactory();
    LocalBase localBase = locationFactory.createLocalBase(4L);
    localBase.setComplement(true);
    Segment segment = segmentFactory.createSegment(sequence, localBase);
    assertEquals("a", new String(segment.getSequenceByte()));
    assertEquals("a", new String(segment.getSequenceByte()));
  }

  @Test
  public void testCreateLocalRangeSegment() {
    SequenceFactory sequenceFactory = new SequenceFactory();
    Sequence sequence = sequenceFactory.createSequenceByte(SEQ1.getBytes());
    LocationFactory locationFactory = new LocationFactory();
    LocalRange localRange = locationFactory.createLocalRange(1L, 4L);
    localRange.setComplement(true);
    Segment segment = segmentFactory.createSegment(sequence, localRange);
    assertEquals("attt", new String(segment.getSequenceByte()));
  }

  @Test
  public void testCreateCompoundLocation1() throws IOException {
    SequenceFactory sequenceFactory = new SequenceFactory();
    Sequence sequence = sequenceFactory.createSequenceByte(SEQ1.getBytes());
    LocationFactory locationFactory = new LocationFactory();
    LocalRange localRange1 = locationFactory.createLocalRange(1L, 4L);
    LocalRange localRange2 = locationFactory.createLocalRange(1L, 4L);
    localRange2.setComplement(true);
    Join<Location> join = new Join<Location>();
    join.addLocation(localRange1);
    join.addLocation(localRange2);
    Segment segment = segmentFactory.createSegment(sequence, join);
    assertEquals("aaatattt", new String(segment.getSequenceByte()));
  }

  @Test
  public void testCreateCompoundLocation2() throws IOException {
    SequenceFactory sequenceFactory = new SequenceFactory();
    Sequence sequence = sequenceFactory.createSequenceByte(SEQ2.getBytes());
    LocationFactory locationFactory = new LocationFactory();
    LocalRange localRange1 = locationFactory.createLocalRange(4L, 731L);
    LocalBase localBase1 = locationFactory.createLocalBase(960L);
    Join<Location> join = new Join<Location>();
    join.addLocation(localRange1);
    join.addLocation(localBase1);
    Segment segment = segmentFactory.createSegment(sequence, join);
    assertEquals(SEQ3, new String(segment.getSequenceByte()));
  }

  @Test
  public void testCreateCompoundLocation3() throws IOException {
    SequenceFactory sequenceFactory = new SequenceFactory();
    Sequence sequence = sequenceFactory.createSequenceByte(SEQ4.getBytes());
    LocationFactory locationFactory = new LocationFactory();
    LocalRange localRange1 = locationFactory.createLocalRange(1L, 1254L);
    LocalRange localRange2 = locationFactory.createLocalRange(1637L, 1696L);
    Join<Location> join = new Join<Location>();
    join.addLocation(localRange1);
    join.addLocation(localRange2);
    Segment segment = segmentFactory.createSegment(sequence, join);
    assertEquals(SEQ5, new String(segment.getSequenceByte()));
  }

  @Test
  public void testCreateCompoundLocation4() throws IOException {
    SequenceFactory sequenceFactory = new SequenceFactory();
    Sequence sequence = sequenceFactory.createSequenceByte(SEQ6.getBytes());
    LocationFactory locationFactory = new LocationFactory();
    LocalRange localRange1 = locationFactory.createLocalRange(1L, 43L);
    LocalRange localRange2 = locationFactory.createLocalRange(443L, 735L);
    Join<Location> join = new Join<Location>();
    join.addLocation(localRange1);
    join.addLocation(localRange2);
    Segment segment = segmentFactory.createSegment(sequence, join);
    assertEquals(SEQ7, new String(segment.getSequenceByte()));
  }

  @Test
  public void testCreateCompoundLocation5() throws IOException {
    SequenceFactory sequenceFactory = new SequenceFactory();
    Sequence sequence = sequenceFactory.createSequenceByte(SEQ8.getBytes());
    LocationFactory locationFactory = new LocationFactory();
    LocalRange localRange1 = locationFactory.createLocalRange(1381L, 3849L);
    localRange1.setComplement(true);
    Join<Location> join = new Join<Location>();
    join.addLocation(localRange1);
    Segment segment = segmentFactory.createSegment(sequence, join);
    assertEquals(SEQ9, new String(segment.getSequenceByte()));
  }

  @Test
  public void testCreateCompoundLocation6() throws IOException {
    SequenceFactory sequenceFactory = new SequenceFactory();
    Sequence sequence = sequenceFactory.createSequenceByte(SEQ10.getBytes());
    LocationFactory locationFactory = new LocationFactory();
    LocalRange localRange1 = locationFactory.createLocalRange(10L, 1281L);
    localRange1.setComplement(true);
    Join<Location> join = new Join<Location>();
    join.addLocation(localRange1);
    Segment segment = segmentFactory.createSegment(sequence, join);
    assertEquals(SEQ11, new String(segment.getSequenceByte()));
  }

  @Test
  public void testCreateCompoundLocation7() throws IOException {
    SequenceFactory sequenceFactory = new SequenceFactory();
    Sequence sequence = sequenceFactory.createSequenceByte(SEQ12.getBytes());
    LocationFactory locationFactory = new LocationFactory();
    LocalRange localRange1 = locationFactory.createLocalRange(2592L, 4415L);
    LocalRange localRange2 = locationFactory.createLocalRange(4760L, 5002L);
    Join<Location> join = new Join<Location>();
    join.setComplement(true);
    join.addLocation(localRange1);
    join.addLocation(localRange2);
    Segment segment = segmentFactory.createSegment(sequence, join);
    assertEquals(SEQ13, new String(segment.getSequenceByte()));
  }

  @Test
  public void testCreateCompoundLocation8() throws IOException {
    SequenceFactory sequenceFactory = new SequenceFactory();
    Sequence sequence = sequenceFactory.createSequenceByte(SEQ14.getBytes());
    LocationFactory locationFactory = new LocationFactory();
    LocalRange localRange1 = locationFactory.createLocalRange(1L, 7L);
    LocalRange localRange2 = locationFactory.createLocalRange(8L, 15L);
    localRange1.setComplement(true);
    localRange2.setComplement(true);
    Join<Location> join = new Join<Location>();
    join.setComplement(true);
    join.addLocation(localRange1);
    join.addLocation(localRange2);
    Segment segment = segmentFactory.createSegment(sequence, join);
    assertEquals("gaccttggacgtttg", new String(segment.getSequenceByte()));
  }

  @Test
  public void testCreateRemoteRange() throws IOException, ValidationEngineException {
    SequenceRetrievalService sequenceRetrievalService = createMock(SequenceRetrievalService.class);
    expect(sequenceRetrievalService.getSequence(EasyMock.isA(RemoteRange.class)))
        .andReturn(ByteBuffer.wrap("gaccttggacg".getBytes()));
    replay(sequenceRetrievalService);
    LocationFactory locationFactory = new LocationFactory();
    RemoteRange remoteRange = locationFactory.createRemoteRange("A00001", 1, 10L, 20L);
    SegmentFactory segmentFactory = new SegmentFactory(sequenceRetrievalService);
    Segment segment = segmentFactory.createSegment(remoteRange);
    assertEquals("gaccttggacg", new String(segment.getSequenceByte()));
  }

  @Test
  public void testCreateRemoteBase() throws IOException, ValidationEngineException {
    SequenceRetrievalService sequenceRetrievalService = createMock(SequenceRetrievalService.class);
    expect(sequenceRetrievalService.getSequence(EasyMock.isA(RemoteBase.class)))
        .andReturn(ByteBuffer.wrap("g".getBytes()));
    replay(sequenceRetrievalService);
    LocationFactory locationFactory = new LocationFactory();
    RemoteBase remoteBase = locationFactory.createRemoteBase("A00001", 1, 1L);
    SegmentFactory segmentFactory = new SegmentFactory(sequenceRetrievalService);
    Segment segment = segmentFactory.createSegment(remoteBase);
    assertEquals("g", new String(segment.getSequenceByte()));
  }

  @Test
  public void testCreateRemoteCompoundLocation() throws ValidationEngineException, IOException {
    SequenceFactory sequenceFactory = new SequenceFactory();
    Sequence sequence = sequenceFactory.createSequenceByte(SEQ10.getBytes());
    LocationFactory locationFactory = new LocationFactory();
    LocalRange localRange1 = locationFactory.createLocalRange(10L, 1281L);
    localRange1.setComplement(true);
    RemoteRange remoteRange = locationFactory.createRemoteRange("A00001", 1, 10L, 20L);
    remoteRange.setComplement(true);
    Join<Location> join = new Join<Location>();
    join.addLocation(localRange1);
    join.addLocation(remoteRange);

    SequenceRetrievalService sequenceRetrievalService = createMock(SequenceRetrievalService.class);
    expect(sequenceRetrievalService.getSequence(EasyMock.isA(RemoteRange.class)))
        .andReturn(ByteBuffer.wrap("cgtccaaggtc".getBytes()));
    replay(sequenceRetrievalService);
    SegmentFactory segmentFactory = new SegmentFactory(sequenceRetrievalService);
    Segment segment = segmentFactory.createSegment(sequence, join);

    assertEquals(SEQ11 + "cgtccaaggtc", new String(segment.getSequenceByte()));
  }
}
