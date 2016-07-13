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
package uk.ac.ebi.embl.flatfile.writer.genbank;

import java.io.IOException;
import java.io.StringWriter;

import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;

public class VersionWriterTest extends GenbankWriterTest {

	public void testWriteWithGIAccession() throws IOException {
	    StringWriter writer = new StringWriter();
		SequenceFactory sequenceFactory = new SequenceFactory();
		entry.setSequence(sequenceFactory.createSequenceByte("aa".getBytes()));	    
	    entry.getSequence().setAccession("A00001");
	    entry.getSequence().setVersion(1);
	    entry.getSequence().setGIAccession("GI:58418");
	    assertTrue(new VersionWriter(entry).write(writer));
	    assertEquals(
	    		"VERSION     A00001.1  GI:58418\n",
		    writer.toString());
	}

	public void tesWriteWithoutGIAccession() throws IOException {
	    StringWriter writer = new StringWriter();
		SequenceFactory sequenceFactory = new SequenceFactory();
		entry.setSequence(sequenceFactory.createSequenceByte("aa".getBytes()));	    
	    entry.getSequence().setAccession("A00001");
	    entry.getSequence().setVersion(1);
	    assertTrue(new VersionWriter(entry).write(writer));
	    assertEquals(
	    		"VERSION     A00001.1\n",
		    writer.toString());
	}
}
