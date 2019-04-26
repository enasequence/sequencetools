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

import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.flatfile.writer.embl.DRWriter;

public class DblinkWriterTest extends GenbankWriterTest {

	 public void testWrite_Xref() throws IOException {
        StringWriter writer = new StringWriter();
    	EntryFactory entryFactory = new EntryFactory();
    	entry.addXRef(entryFactory.createXRef("BioProject","PRJNA28847"));
    	entry.addXRef(entryFactory.createXRef("BioSample", "SAMN02436234"));
        assertTrue(new DblinkWriter(entry).write(writer));
        assertEquals(
        		"DBLINK      BioProject: PRJNA28847\n"
        	  + "            BioSample: SAMN02436234\n",
                writer.toString());
    }

   public void testWrite_NoXref() throws IOException {
        StringWriter writer = new StringWriter();
        assertFalse(new DblinkWriter(entry).write(writer));
        assertEquals(
        		"",
                writer.toString());
    }
	
	
}
