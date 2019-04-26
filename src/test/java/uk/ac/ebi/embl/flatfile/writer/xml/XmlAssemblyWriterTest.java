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
package uk.ac.ebi.embl.flatfile.writer.xml;

import java.io.IOException;
import java.io.StringWriter;

import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

public class XmlAssemblyWriterTest extends XmlWriterTest {

	public void testWrite() throws IOException {
    	EntryFactory entryFactory = new EntryFactory();
    	entry.addAssembly(entryFactory.createAssembly("AC004528", 1, 18665l,
    		19090l, true, 1l, 426l));
    	entry.addAssembly(entryFactory.createAssembly("AC004529", 6, 45665l,
    		98790l, true, 6l, 546l));    	
	    StringWriter writer = new StringWriter();
	    assertTrue(new XmlAssemblyWriter(entry).write(new SimpleXmlWriter(writer)));
	    //System.out.println(writer.toString());
	    assertEquals(
	    		"<assembly>\n" +
	    		"	<range begin=\"1\" end=\"426\" primaryBegin=\"18665\" primaryEnd=\"19090\" accession=\"AC004528\" version=\"1\" complement=\"true\"/>\n" +
	    		"	<range begin=\"6\" end=\"546\" primaryBegin=\"45665\" primaryEnd=\"98790\" accession=\"AC004529\" version=\"6\" complement=\"true\"/>\n" +
	    		"</assembly>\n",
                writer.toString());
	    }
}
