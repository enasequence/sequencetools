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
package uk.ac.ebi.embl.api.translation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CodonTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testSetCodon() {
		Codon codon = new Codon();
		assertNull(codon.getCodon());
		codon.setCodon("aaa");
		assertEquals("aaa", codon.getCodon());
	}

	@Test
	public void testSetPos() {
		Codon codon = new Codon();
		assertNull(codon.getPos());
		codon.setPos(1);
		assertEquals(new Integer(1), codon.getPos());
	}

	@Test
	public void testSetAminoAcid() {
		Codon codon = new Codon();
		assertNull(codon.getAminoAcid());
		codon.setAminoAcid('A');
		assertEquals(new Character('A'), codon.getAminoAcid());
	}
}
