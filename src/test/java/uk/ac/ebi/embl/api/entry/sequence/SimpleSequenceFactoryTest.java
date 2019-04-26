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

import static org.junit.Assert.*;
import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;

public class SimpleSequenceFactoryTest {

	private SequenceFactory factory;
	
	@Before
	public void setUp() throws Exception {
		factory = new SequenceFactory();
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testCreateSequence() {
		Sequence sequence = factory.createSequenceByte("aatt".getBytes());
		sequence.setMoleculeType("DNA");
		sequence.setTopology(Topology.CIRCULAR);
		assertEquals("aatt", new String(sequence.getSequenceByte()));
		assertEquals(4, sequence.getLength());
		assertEquals("DNA", sequence.getMoleculeType());
		assertEquals(Topology.CIRCULAR, sequence.getTopology());
		
		assertNull(sequence.getAccession());
		assertNull(sequence.getVersion());
	}
	
	/*@Test
	public void testCreateSequenceBytes() {
		
		byte[] bytes = new byte[] { 'a','a','t','t' };
		Sequence sequence = factory.createSequence(bytes);
		sequence.setMoleculeType("DNA");
		sequence.setTopology(Topology.CIRCULAR);
		assertEquals(ByteBuffer.wrap(bytes), sequence.getSequenceBuffer());
		assertEquals(4, sequence.getLength());
		assertEquals("DNA", sequence.getMoleculeType());
		assertEquals(Topology.CIRCULAR, sequence.getTopology());
		
		assertNull(sequence.getAccession());
		assertNull(sequence.getVersion());
	}*/
	}
