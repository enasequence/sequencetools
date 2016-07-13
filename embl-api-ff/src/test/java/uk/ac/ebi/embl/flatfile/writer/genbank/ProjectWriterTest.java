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
import java.util.Arrays;

public class ProjectWriterTest extends GenbankWriterTest {

    public void testWrite_OneProject() throws IOException {
    	entry.addProjectAccessions(Arrays.asList(new Text("17285")));
        StringWriter writer = new StringWriter();
        assertTrue(new ProjectWriter(entry, wrapType).write(writer));
        // System.out.print(writer.toString());
        assertEquals(
        		"PROJECT     GenomeProject:17285\n", 
        		writer.toString());
    }

    public void testWrite_TwoProjects() throws IOException {
    	entry.addProjectAccessions(Arrays.asList(new Text("17285"), new Text("123456")));
        StringWriter writer = new StringWriter();
        assertTrue(new ProjectWriter(entry, wrapType).write(writer));
        // System.out.print(writer.toString());
        assertEquals(
        		"PROJECT     GenomeProject:17285 GenomeProject:123456\n", 
        		writer.toString());
    }

    public void testWrite_ManyProjects() throws IOException {
    	entry.addProjectAccessions(Arrays.asList(
    			new Text("17285"), new Text("123456"), new Text("17285"), new Text("123456"),
    			new Text("17285"), new Text("123456"), new Text("17285"), new Text("123456"),
                new Text("17285"), new Text("123456")));

        StringWriter writer = new StringWriter();
        assertTrue(new ProjectWriter(entry, wrapType).write(writer));
        //System.out.print(writer.toString());
        assertEquals(
        		"PROJECT     GenomeProject:17285 GenomeProject:123456 GenomeProject:17285\n" +
        		"            GenomeProject:123456 GenomeProject:17285 GenomeProject:123456\n" +
        		"            GenomeProject:17285 GenomeProject:123456 GenomeProject:17285\n" +
        		"            GenomeProject:123456\n", 
        	writer.toString());
    }    
}
