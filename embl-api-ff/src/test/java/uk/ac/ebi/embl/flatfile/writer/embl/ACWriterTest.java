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
package uk.ac.ebi.embl.flatfile.writer.embl;

import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.flatfile.writer.embl.ACWriter;

import java.io.IOException;
import java.io.StringWriter;

public class ACWriterTest extends EmblWriterTest {

	public void testWrite_PrimaryAccessionOnly() throws IOException {
		StringWriter writer = new StringWriter();
		entry.setPrimaryAccession("DP000153");
		assertTrue(new ACWriter(entry, wrapType).write(writer));
		assertEquals("AC   DP000153;\n", writer.toString());
	}

	public void testWrite_SecondaryAccessionOnly() throws IOException {
		StringWriter writer = new StringWriter();
		entry.setPrimaryAccession(null);
		entry.addSecondaryAccession(new Text("A00001"));
		assertTrue(new ACWriter(entry, wrapType).write(writer));
		assertEquals("AC   ; A00001;\n", writer.toString());
	}
		
	public void testWrite_NoAccession() throws IOException {
		StringWriter writer = new StringWriter();
		entry.setPrimaryAccession(null);
		assertTrue(new ACWriter(entry, wrapType).write(writer));
		assertEquals("AC   ;\n", writer.toString());
	}

	public void testWrite_PrimaryAndSecondaryAccession() throws IOException {
		StringWriter writer = new StringWriter();
		entry.setPrimaryAccession("DP000153");
		entry.addSecondaryAccession(new Text("A00001"));
		entry.addSecondaryAccession(new Text("A00002"));
		entry.addSecondaryAccession(new Text("A00003"));
		entry.addSecondaryAccession(new Text("A00004"));
		entry.addSecondaryAccession(new Text("A00005"));
		entry.addSecondaryAccession(new Text("A00006"));
		entry.addSecondaryAccession(new Text("A00007"));
		entry.addSecondaryAccession(new Text("A00008"));
		entry.addSecondaryAccession(new Text("A00009"));
		entry.addSecondaryAccession(new Text("A00010"));
		entry.addSecondaryAccession(new Text("A00011"));
		entry.addSecondaryAccession(new Text("A00012"));
		entry.addSecondaryAccession(new Text("A00013"));
		entry.addSecondaryAccession(new Text("A00014"));
		entry.addSecondaryAccession(new Text("A00015"));
		entry.addSecondaryAccession(new Text("A00016"));
		entry.addSecondaryAccession(new Text("A00017"));
		assertTrue(new ACWriter(entry, wrapType).write(writer));
		assertEquals(
				"AC   DP000153; A00001; A00002; A00003; A00004; A00005; A00006; A00007; A00008;\n" +
				"AC   A00009; A00010; A00011; A00012; A00013; A00014; A00015; A00016; A00017;\n", 
				writer.toString());
	}
}
