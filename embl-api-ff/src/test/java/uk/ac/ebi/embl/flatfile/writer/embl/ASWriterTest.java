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

public class ASWriterTest extends EmblWriterTest {

    public void testWrite_Assembly() throws IOException {
    	EntryFactory entryFactory = new EntryFactory();
    	entry.addAssembly(entryFactory.createAssembly("AC004528", 1, 18665l,
    		19090l, true, 1l, 426l));
    	entry.addAssembly(entryFactory.createAssembly("AC004529", 6, 45665l,
    		98790l, true, 6l, 546l));    	
	    StringWriter writer = new StringWriter();
	    assertTrue(new ASWriter(entry).write(writer));
	    //System.out.print(writer.toString());
	    assertEquals(
	    		"AH   LOCAL_SPAN          PRIMARY_IDENTIFIER PRIMARY_SPAN        COMP\n" +
	    		"AS   1-426               AC004528.1         18665-19090         c\n" +
	    		"AS   6-546               AC004529.6         45665-98790         c\n",
                writer.toString());
    }

    public void testWrite_NoAssembly() throws IOException {
	    entry.removeAssemblies();
	    StringWriter writer = new StringWriter();
	    assertFalse(new ASWriter(entry).write(writer));
	    // System.out.print(writer.toString());
	    assertEquals(
	    		"", 
                writer.toString());

    }
}
