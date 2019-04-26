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
package uk.ac.ebi.embl.api.entry.qualifier;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.qualifier.CodonStartQualifier;
import uk.ac.ebi.embl.api.validation.ValidationException;

public class CodonStartQualifierTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testValidQualifier() throws ValidationException {
		CodonStartQualifier qual = new CodonStartQualifier("1");
		assertEquals(qual.getStartCodon(), new Integer(1));
	}

	@Test(expected=ValidationException.class)
	public void testInvalidQualifier1() throws ValidationException {
		CodonStartQualifier qual = new CodonStartQualifier("X");
		qual.getStartCodon();
	}
}
