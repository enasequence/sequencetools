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

import uk.ac.ebi.embl.api.entry.Text;

import java.io.IOException;
import java.io.StringWriter;

public class DefinitionWriterTest extends GenbankWriterTest {

    public void testWrite_Description() throws IOException {
		entry.setDescription(new Text(
			"Felis \tcatus target 36 genomic scaffold " +
			"Felis \ncatus target 36 genomic scaffold " +
			"Felis \rcatus target 36 genomic scaffold " +
			"Felis catus target 36 genomic scaffold " +
			"Felis catus target 36 genomic scaffold " +
			"Felis catus target 36 genomic scaffold " +
			"Felis catus target 36 genomic scaffold " +
			"Felis catus target 36 genomic scaffold " +
			"Felis catus target 36 genomic scaffold"));
	    StringWriter writer = new StringWriter();
	    assertTrue(new DefinitionWriter(entry, wrapType).write(writer));
	    //System.out.print(writer.toString());
	    assertEquals(
	    		"DEFINITION  Felis  catus target 36 genomic scaffold Felis  catus target 36\n" +
	            "            genomic scaffold Felis  catus target 36 genomic scaffold Felis\n" +
	            "            catus target 36 genomic scaffold Felis catus target 36 genomic\n" +
	            "            scaffold Felis catus target 36 genomic scaffold Felis catus target\n" +
	            "            36 genomic scaffold Felis catus target 36 genomic scaffold Felis\n" +
	            "            catus target 36 genomic scaffold\n",
	    		writer.toString());
	}
	    
    public void testWrite_NoDescription() throws IOException {
    	entry.setDescription(null);
	    StringWriter writer = new StringWriter();
	    assertTrue(new DefinitionWriter(entry, wrapType).write(writer));
	    assertEquals("DEFINITION  .\n", writer.toString());
    }
}
