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

import java.io.IOException;
import java.io.StringWriter;

import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.flatfile.writer.embl.DRWriter;

public class DRWriterTest extends EmblWriterTest {

    public void testWrite_Xref() throws IOException {
        StringWriter writer = new StringWriter();
    	EntryFactory entryFactory = new EntryFactory();
    	entry.addXRef(entryFactory.createXRef("IMGT/LIGM", "X54835", "X54835"));
    	entry.addXRef(entryFactory.createXRef("IMGT/PIG", "X54835", "X54835"));
        assertTrue(new DRWriter(entry).write(writer));
        assertEquals(
        		"DR   IMGT/LIGM; X54835; X54835.\n" +
                "DR   IMGT/PIG; X54835; X54835.\n",
                writer.toString());
    }

    public void testWrite_XrefNoSecondary() throws IOException {
        StringWriter writer = new StringWriter();
    	EntryFactory entryFactory = new EntryFactory();
    	entry.addXRef(entryFactory.createXRef("IMGT/LIGM", "X54835", null));
    	entry.addXRef(entryFactory.createXRef("IMGT/PIG", "X54835", null));
        assertTrue(new DRWriter(entry).write(writer));
        assertEquals(
        		"DR   IMGT/LIGM; X54835.\n" +
                "DR   IMGT/PIG; X54835.\n",
                writer.toString());
    }

    public void testWrite_NoXref() throws IOException {
        StringWriter writer = new StringWriter();
        assertFalse(new DRWriter(entry).write(writer));
        assertEquals(
        		"",
                writer.toString());
    }
}
