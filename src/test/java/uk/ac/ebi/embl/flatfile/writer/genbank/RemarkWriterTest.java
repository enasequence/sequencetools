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

import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;

public class RemarkWriterTest extends GenbankWriterTest {

    public void testWrite_Comment() throws IOException {
	    ReferenceFactory referenceFactory = new ReferenceFactory();
		Reference reference = referenceFactory.createReference();
		reference.setComment("blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah");
	    StringWriter writer = new StringWriter();
	    assertTrue(new RemarkWriter(entry, reference, wrapType).write(writer));
	    //System.out.print(writer.toString());
	    assertEquals(
	    		"  REMARK    blah blah blah blah blah blah blah blah blah blah blah blah blah\n" +
	    		"            blah blah blah blah blah blah blah\n",
	    		writer.toString());
    }

    public void testWrite_NoComment() throws IOException {
	    ReferenceFactory referenceFactory = new ReferenceFactory();
	    Reference reference = referenceFactory.createReference();
		reference.setComment(null);
	    StringWriter writer = new StringWriter();
	    assertFalse(new RemarkWriter(entry, reference, wrapType).write(writer));
	    //System.out.print(writer.toString());
	    assertEquals(
	    		"",
	    		writer.toString());    
    }
}
