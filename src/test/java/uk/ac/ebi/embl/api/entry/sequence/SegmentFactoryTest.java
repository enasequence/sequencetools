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
package uk.ac.ebi.embl.api.entry.sequence;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.LocalBase;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.RemoteBase;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;

public class SegmentFactoryTest implements Sequences {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testCreateLocalBaseSegment() {
		SequenceFactory sequenceFactory = new SequenceFactory();
		Sequence sequence = sequenceFactory.createSequenceByte(SEQ1.getBytes());
		LocationFactory locationFactory = new LocationFactory();
		LocalBase localBase = locationFactory.createLocalBase(4L);
		localBase.setComplement(true);
		SegmentFactory segmentFactory = new SegmentFactory();
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
		SegmentFactory segmentFactory = new SegmentFactory();
		Segment segment = segmentFactory.createSegment(sequence, localRange);
		assertEquals("attt", new String(segment.getSequenceByte()));
	}

	@Test
	public void testCreateCompoundLocation1() throws SQLException, IOException {
		SequenceFactory sequenceFactory = new SequenceFactory();
		Sequence sequence = sequenceFactory.createSequenceByte(SEQ1.getBytes());
		LocationFactory locationFactory = new LocationFactory();
		LocalRange localRange1 = locationFactory.createLocalRange(1L, 4L);
		LocalRange localRange2 = locationFactory.createLocalRange(1L, 4L);
		localRange2.setComplement(true);
		Join<Location> join = new Join<Location>();
		join.addLocation(localRange1);
		join.addLocation(localRange2);
		SegmentFactory segmentFactory = new SegmentFactory();
		Segment segment = segmentFactory.createSegment(sequence, join);
		assertEquals("aaatattt", new String(segment.getSequenceByte()));
	}


	@Test
	public void testCreateCompoundLocation2() throws SQLException, IOException {
		SequenceFactory sequenceFactory = new SequenceFactory();
		Sequence sequence = sequenceFactory.createSequenceByte(SEQ2.getBytes());
		LocationFactory locationFactory = new LocationFactory();
		LocalRange localRange1 = locationFactory.createLocalRange(4L, 731L);
		LocalBase localBase1 = locationFactory.createLocalBase(960L);
		Join<Location> join = new Join<Location>();
		join.addLocation(localRange1);
		join.addLocation(localBase1);
		SegmentFactory segmentFactory = new SegmentFactory();
		Segment segment = segmentFactory.createSegment(sequence, join);
		assertEquals(SEQ3, new String(segment.getSequenceByte()));
	}

	@Test
	public void testCreateCompoundLocation3() throws SQLException, IOException {
		SequenceFactory sequenceFactory = new SequenceFactory();
		Sequence sequence = sequenceFactory.createSequenceByte(SEQ4.getBytes());
		LocationFactory locationFactory = new LocationFactory();
		LocalRange localRange1 = locationFactory.createLocalRange(1L, 1254L);
		LocalRange localRange2 = locationFactory.createLocalRange(1637L, 1696L);
		Join<Location> join = new Join<Location>();
		join.addLocation(localRange1);
		join.addLocation(localRange2);
		SegmentFactory segmentFactory = new SegmentFactory();
		Segment segment = segmentFactory.createSegment(sequence, join);
		assertEquals(SEQ5, new String(segment.getSequenceByte()));
	}

	@Test
	public void testCreateCompoundLocation4() throws SQLException, IOException {
		SequenceFactory sequenceFactory = new SequenceFactory();
		Sequence sequence = sequenceFactory.createSequenceByte(SEQ6.getBytes());
		LocationFactory locationFactory = new LocationFactory();
		LocalRange localRange1 = locationFactory.createLocalRange(1L, 43L);
		LocalRange localRange2 = locationFactory.createLocalRange(443L, 735L);
		Join<Location> join = new Join<Location>();
		join.addLocation(localRange1);
		join.addLocation(localRange2);
		SegmentFactory segmentFactory = new SegmentFactory();
		Segment segment = segmentFactory.createSegment(sequence, join);
		assertEquals(SEQ7, new String(segment.getSequenceByte()));
	}

	@Test
	public void testCreateCompoundLocation5() throws SQLException, IOException {
		SequenceFactory sequenceFactory = new SequenceFactory();
		Sequence sequence = sequenceFactory.createSequenceByte(SEQ8.getBytes());
		LocationFactory locationFactory = new LocationFactory();
		LocalRange localRange1 = locationFactory.createLocalRange(1381L, 3849L);
		localRange1.setComplement(true);
		Join<Location> join = new Join<Location>();
		join.addLocation(localRange1);
		SegmentFactory segmentFactory = new SegmentFactory();
		Segment segment = segmentFactory.createSegment(sequence, join);
		assertEquals(SEQ9, new String(segment.getSequenceByte()));
	}

	@Test
	public void testCreateCompoundLocation6() throws SQLException, IOException {
		SequenceFactory sequenceFactory = new SequenceFactory();
		Sequence sequence = sequenceFactory.createSequenceByte(SEQ10.getBytes());
		LocationFactory locationFactory = new LocationFactory();
		LocalRange localRange1 = locationFactory.createLocalRange(10L, 1281L);
		localRange1.setComplement(true);
		Join<Location> join = new Join<Location>();
		join.addLocation(localRange1);
		SegmentFactory segmentFactory = new SegmentFactory();
		Segment segment = segmentFactory.createSegment(sequence, join);
		assertEquals(SEQ11, new String(segment.getSequenceByte()));
	}

	@Test
	public void testCreateCompoundLocation7() throws SQLException, IOException {
		SequenceFactory sequenceFactory = new SequenceFactory();
		Sequence sequence = sequenceFactory.createSequenceByte(SEQ12.getBytes());
		LocationFactory locationFactory = new LocationFactory();
		LocalRange localRange1 = locationFactory.createLocalRange(2592L, 4415L);
		LocalRange localRange2 = locationFactory.createLocalRange(4760L, 5002L);
		Join<Location> join = new Join<Location>();
		join.setComplement(true);
		join.addLocation(localRange1);
		join.addLocation(localRange2);
		SegmentFactory segmentFactory = new SegmentFactory();
		Segment segment = segmentFactory.createSegment(sequence, join);
		assertEquals(SEQ13, new String(segment.getSequenceByte()));
	}

	@Test
	public void testCreateCompoundLocation8() throws SQLException, IOException {
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
		SegmentFactory segmentFactory = new SegmentFactory();
		Segment segment = segmentFactory.createSegment(sequence, join);
		assertEquals("gaccttggacgtttg", new String(segment.getSequenceByte()));
	}

	@Test
	public void testCreateRemoteRange() throws SQLException, IOException {
		SequenceFactory sequenceFactory = new SequenceFactory();
		LocationFactory locationFactory = new LocationFactory();
		Sequence sequence = sequenceFactory.createSequenceByte(null);
		SegmentFactory segmentFactory = new SegmentFactory(createMock(Connection.class));
		EntryDAOUtils entryDAOUtils=createMock(EntryDAOUtils.class);
		expect(entryDAOUtils.getSubSequence("A00001.1",10L,11L)).andReturn("gaccttggacg".getBytes());
		replay(entryDAOUtils);
		segmentFactory.setEntryDAOUtils(entryDAOUtils);
		RemoteRange remoteRange = locationFactory.createRemoteRange("A00001", 1,10L,20L);
		Segment segment = segmentFactory.createSegment(remoteRange);
		assertEquals("gaccttggacg", new String(segment.getSequenceByte()));
		
	}
	
	@Test(expected=SQLException.class)
	public void testCreateInvalidRemoteRange() throws SQLException, IOException {
		SequenceFactory sequenceFactory = new SequenceFactory();
		LocationFactory locationFactory = new LocationFactory();
		Sequence sequence = sequenceFactory.createSequenceByte(null);
		SegmentFactory segmentFactory = new SegmentFactory(createMock(Connection.class));
		EntryDAOUtils entryDAOUtils=createMock(EntryDAOUtils.class);
		expect(entryDAOUtils.getSubSequence("A00001.1",10L,12L)).andReturn("gaccttggacg".getBytes());
		replay(entryDAOUtils);
		segmentFactory.setEntryDAOUtils(entryDAOUtils);
		RemoteRange remoteRange = locationFactory.createRemoteRange("A00001", 1,10L,21L);
		segmentFactory.createSegment(remoteRange);
	}
	
	@Test
	public void testCreateRemoteRangeComplement() throws SQLException, IOException {
		SequenceFactory sequenceFactory = new SequenceFactory();
		LocationFactory locationFactory = new LocationFactory();
		Sequence sequence = sequenceFactory.createSequenceByte(null);
		SegmentFactory segmentFactory = new SegmentFactory(createMock(Connection.class));
		EntryDAOUtils entryDAOUtils=createMock(EntryDAOUtils.class);
		expect(entryDAOUtils.getSubSequence("A00001.1",10L,11L)).andReturn("gaccttggacg".getBytes());
		replay(entryDAOUtils);
		segmentFactory.setEntryDAOUtils(entryDAOUtils);
		RemoteRange remoteRange = locationFactory.createRemoteRange("A00001", 1,10L,20L);
		remoteRange.setComplement(true);
		Segment segment = segmentFactory.createSegment(remoteRange);
		assertEquals("cgtccaaggtc", new String(segment.getSequenceByte()));
		
	}

	@Test
	public void testCreateRemoteBase() throws SQLException, IOException {
		SequenceFactory sequenceFactory = new SequenceFactory();
		LocationFactory locationFactory = new LocationFactory();
		Sequence sequence = sequenceFactory.createSequenceByte(null);
		SegmentFactory segmentFactory = new SegmentFactory(createMock(Connection.class));
		EntryDAOUtils entryDAOUtils=createMock(EntryDAOUtils.class);
		expect(entryDAOUtils.getSubSequence("A00001.1",10L,1L)).andReturn("g".getBytes());
		replay(entryDAOUtils);
		segmentFactory.setEntryDAOUtils(entryDAOUtils);
		RemoteBase remoteBase = locationFactory.createRemoteBase("A00001", 1,10L);
		Segment segment = segmentFactory.createSegment(remoteBase);
		assertEquals("g", new String(segment.getSequenceByte()));
		
	}
	@Test(expected = SQLException.class)
	public void testCreateInvalidRemoteBase() throws SQLException, IOException {
		SequenceFactory sequenceFactory = new SequenceFactory();
		LocationFactory locationFactory = new LocationFactory();
		Sequence sequence = sequenceFactory.createSequenceByte(null);
		SegmentFactory segmentFactory = new SegmentFactory(createMock(Connection.class));
		EntryDAOUtils entryDAOUtils=createMock(EntryDAOUtils.class);
		expect(entryDAOUtils.getSubSequence("A00001.1",10L,1L)).andReturn("".getBytes());
		replay(entryDAOUtils);
		segmentFactory.setEntryDAOUtils(entryDAOUtils);
		RemoteBase remoteBase = locationFactory.createRemoteBase("A00001", 1,10L);
		segmentFactory.createSegment(remoteBase);
		}
	
	
	@Test
	public void testCreateRemoteBaseComplement() throws SQLException, IOException {
		SequenceFactory sequenceFactory = new SequenceFactory();
		LocationFactory locationFactory = new LocationFactory();
		Sequence sequence = sequenceFactory.createSequenceByte(null);
		SegmentFactory segmentFactory = new SegmentFactory(createMock(Connection.class));
		EntryDAOUtils entryDAOUtils=createMock(EntryDAOUtils.class);
		expect(entryDAOUtils.getSubSequence("A00001.1",10L,1L)).andReturn("g".getBytes());
		replay(entryDAOUtils);
		segmentFactory.setEntryDAOUtils(entryDAOUtils);
		RemoteBase remoteBase = locationFactory.createRemoteBase("A00001", 1,10L);
		remoteBase.setComplement(true);
		Segment segment = segmentFactory.createSegment(remoteBase);
		assertEquals("c", new String(segment.getSequenceByte()));
		
	}
	
	@Test
	public void testCreateRemoteCompoundLocation6() throws SQLException, IOException {
		SequenceFactory sequenceFactory = new SequenceFactory();
		Sequence sequence = sequenceFactory.createSequenceByte(SEQ10.getBytes());
		LocationFactory locationFactory = new LocationFactory();
		LocalRange localRange1 = locationFactory.createLocalRange(10L, 1281L);
		localRange1.setComplement(true);
		RemoteRange remoteRange = locationFactory.createRemoteRange("A00001", 1,10L,20L);
		remoteRange.setComplement(true);
		Join<Location> join = new Join<Location>();
		join.addLocation(localRange1);
		join.addLocation(remoteRange);
		SegmentFactory segmentFactory = new SegmentFactory(createMock(Connection.class));
		EntryDAOUtils entryDAOUtils=createMock(EntryDAOUtils.class);
		expect(entryDAOUtils.getSubSequence("A00001.1",10L,11L)).andReturn("gaccttggacg".getBytes());
		replay(entryDAOUtils);
		segmentFactory.setEntryDAOUtils(entryDAOUtils);
		Segment segment = segmentFactory.createSegment(sequence, join);
		assertEquals(SEQ11+"cgtccaaggtc", new String(segment.getSequenceByte()));
	}

}
