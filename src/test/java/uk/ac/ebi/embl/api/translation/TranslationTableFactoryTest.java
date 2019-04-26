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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;


public class TranslationTableFactoryTest {

	@Before
	public void setUp() throws Exception {
	}

	private TranslationTableFactory factory = new TranslationTableFactory(); 
	
	private void testTranslationTable(Integer number) {
		 assertNotNull(factory.createTranslationTable(number));
		 assertEquals(number, factory.createTranslationTable(number).getNumber());		 		
	}
	
	@Test
	public void test() {
		testTranslationTable(1);
        testTranslationTable(2); 
        testTranslationTable(3); 
        testTranslationTable(4); 
        testTranslationTable(5); 
        testTranslationTable(6); 
        testTranslationTable(9); 
        testTranslationTable(10); 
        testTranslationTable(11); 
        testTranslationTable(12); 
        testTranslationTable(13); 
        testTranslationTable(14); 
        testTranslationTable(16);
        testTranslationTable(21);
        testTranslationTable(22);
        testTranslationTable(23);
	}	
}
