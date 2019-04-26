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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.*;
import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.ContigSequenceInfo;
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.helper.ByteBufferUtils;

public class SequenceTest {

	byte[] bytes;
	SequenceFactory sequenceFactory;
	
	@Before
	public void setUp() throws Exception {
		
		sequenceFactory=new SequenceFactory();
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testSequence() {
		Sequence seq = sequenceFactory.createSequenceByte("aaaa".getBytes());
		seq.setMoleculeType("DNA");
		seq.setTopology(Topology.LINEAR);
		assertEquals("aaaa", new String(seq.getSequenceByte()));
		assertEquals(4, seq.getLength());
		assertEquals("DNA", seq.getMoleculeType());
		assertEquals(Topology.LINEAR, seq.getTopology());
		assertNull(seq.getAccession());
		assertNull(seq.getVersion());		
	}
	
	@Test
	public void testSequenceByteBuffer() {
		String sequence="aaaa";
		Sequence seq=sequenceFactory.createSequenceByte(sequence.getBytes());
		seq.setMoleculeType("DNA");
		seq.setTopology(Topology.LINEAR);
		assertEquals("aaaa", new String(seq.getSequenceByte()));
		assertEquals(sequence.compareTo(new String(seq.getSequenceByte())),0);
		assertEquals(4, seq.getLength());
		assertEquals("DNA", seq.getMoleculeType());
		assertEquals(Topology.LINEAR, seq.getTopology());
		assertNull(seq.getAccession());
		assertNull(seq.getVersion());		
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testGetSequence() {
		Sequence seq = sequenceFactory.createSequenceByte("aaaa".getBytes());
		seq.setMoleculeType("DNA");
		seq.setTopology(Topology.LINEAR);
		assertEquals("aaaa", new String(seq.getSequenceByte(1L, 4L)));
	}
	
	/*@Test
	public void testGetSequenceBytes() {
		Sequence seq = new Sequence(bytes, 4);
		seq.setMoleculeType("DNA");
		seq.setTopology(Topology.LINEAR);
		assertEquals("aaaa", seq.getSequence(1L, 4L));
	}*/
		
	@SuppressWarnings("deprecation")
	@Test
	public void testSequenceAccession() {
		Sequence seq = sequenceFactory.createSequenceByte("aaaa".getBytes());
		seq.setMoleculeType("DNA");
		seq.setTopology(Topology.LINEAR);	
		assertNull(seq.getAccession());
		assertNull(seq.getVersion());		
		
		seq.setAccession("x");
		assertEquals("x", seq.getAccession());
		seq.setVersion(1);
		assertEquals(new Integer(1), seq.getVersion());
	}
	
	/*@Test
	public void testSequenceAccessionBytes() {
		Sequence seq = new Sequence(bytes, 4);
		seq.setMoleculeType("DNA");
		seq.setTopology(Topology.LINEAR);	
		assertNull(seq.getAccession());
		assertNull(seq.getVersion());		
		
		seq.setAccession("x");
		assertEquals("x", seq.getAccession());
		seq.setVersion(1);
		assertEquals(new Integer(1), seq.getVersion());
	}*/

	@Test
	public void testHashCode() {
		new Sequence().hashCode();	
		sequenceFactory.createSequenceByte("aaaa".getBytes()).hashCode();
		}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testEquals() {
		assertTrue(new Sequence().equals(
				new Sequence()));
		
		Sequence seq1 = sequenceFactory.createSequenceByte("aaaa".getBytes());
		seq1.setMoleculeType("DNA");
		seq1.setTopology(Topology.LINEAR);		
		assertTrue(seq1.equals(seq1));

		Sequence seq2 = sequenceFactory.createSequenceByte("aaaa".getBytes());
		seq2.setMoleculeType("DNA");
		seq2.setTopology(Topology.LINEAR);
	
		assertTrue(seq1.equals(seq2));
		assertTrue(seq2.equals(seq1));		

		seq2.setSequence(ByteBuffer.wrap("aaa".getBytes()));
		assertFalse(seq1.equals(seq2));
		seq2.setSequence(ByteBuffer.wrap("aaaa".getBytes()));
		
		/*seq2.setLength(3);
		assertFalse(seq1.equals(seq2));
		seq2.setLength(4);*/
		
		seq2.setMoleculeType("RNA");
		assertFalse(seq1.equals(seq2));
		seq2.setMoleculeType("DNA");
		
		seq2.setTopology(Topology.CIRCULAR);
		assertFalse(seq1.equals(seq2));
		seq2.setTopology(Topology.LINEAR);
		
		seq2.setVersion(1);
		assertFalse(seq1.equals(seq2));
		seq1.setVersion(1);
		
		seq2.setAccession("x");
		assertFalse(seq1.equals(seq2));
		seq1.setAccession("x");
		
		assertTrue(seq1.equals(seq2));
	}
	
/*	@Test
	public void testEqualsBytes() {
		assertTrue(new Sequence().equals(
				new Sequence()));
		
		Sequence seq1 = sequenceFactory.createSequence(bytes, 4);
		seq1.setMoleculeType("DNA");
		seq1.setTopology(Topology.LINEAR);		
		assertTrue(seq1.equals(seq1));

		Sequence seq2 = sequenceFactory.createSequence(bytes, 4);
		seq2.setMoleculeType("DNA");
		seq2.setTopology(Topology.LINEAR);
	
		assertTrue(seq1.equals(seq2));
		assertTrue(seq2.equals(seq1));		

		byte[] bytes1 = new byte[] { 'a','a','a' };
		seq2.setSequence(bytes1);
		assertFalse(seq1.equals(seq2));
		seq2.setSequence(bytes);
		
		seq2.setLength(3);
		assertFalse(seq1.equals(seq2));
		seq2.setLength(4);
		
		seq2.setMoleculeType("RNA");
		assertFalse(seq1.equals(seq2));
		seq2.setMoleculeType("DNA");
		
		seq2.setTopology(Topology.CIRCULAR);
		assertFalse(seq1.equals(seq2));
		seq2.setTopology(Topology.LINEAR);
		
		seq2.setVersion(1);
		assertFalse(seq1.equals(seq2));
		seq1.setVersion(1);
		
		seq2.setAccession("x");
		assertFalse(seq1.equals(seq2));
		seq1.setAccession("x");
		
		assertTrue(seq1.equals(seq2));
	}*/

	@Test
	public void testEquals_WrongObject() {
		assertFalse(sequenceFactory.createSequenceByte("aaaa".getBytes()).equals(
				new String()));
		/*assertFalse(new Sequence(bytes, 4).equals(
				new String()));*/
	}
	
	@Test
	public void testToString() {
		new Sequence().toString();	
		sequenceFactory.createSequenceByte("aaaa".getBytes()).toString();
	//	new Sequence(bytes, 4).toString();
	}
	
	@Test
	public void testAGPRows()
	{
		AgpRow validComponentrow1=new AgpRow();
		AgpRow validComponentrow2=new AgpRow();
		AgpRow validGaprow1=new AgpRow();
		validComponentrow1.setObject("IWGSC_CSS_6DL_scaff_3330716");
		validComponentrow1.setObject_beg(1l);
		validComponentrow1.setObject_end(330l);
		validComponentrow1.setPart_number(1);
		validComponentrow1.setComponent_type_id("W");
		validComponentrow1.setComponent_beg(1l);
		validComponentrow1.setComponent_end(330l);
		validComponentrow1.setComponent_id("IWGSC_CSS_6DL_contig_209591");
		validComponentrow1.setOrientation("+");
		validGaprow1.setObject("IWGSC_CSS_6DL_scaff_3330716");
		validGaprow1.setObject_beg(331);
		validGaprow1.setObject_end(354l);
		validGaprow1.setPart_number(2);
		validGaprow1.setComponent_type_id("N");
		validGaprow1.setGap_length(24l);
		validGaprow1.setGap_type("scaffold");
		validComponentrow2.setObject("IWGSC_CSS_6DL_scaff_3330716");
		validComponentrow2.setObject_beg(1l);
		validComponentrow2.setObject_end(330l);
		validComponentrow2.setPart_number(1);
		validComponentrow2.setComponent_type_id("W");
		validComponentrow2.setComponent_beg(1l);
		validComponentrow2.setComponent_end(330l);
		validComponentrow2.setComponent_id("IWGSC_CSS_6DL_contig_209591");
		validComponentrow2.setOrientation("+");
	    Sequence sequence = new SequenceFactory().createSequence();
	    sequence.addAgpRow(validGaprow1);
	    sequence.addAgpRow(validComponentrow2);
	    sequence.addAgpRow(validComponentrow1);
	    assertEquals(684,sequence.getLength());
	
	}

}
