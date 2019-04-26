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

import org.junit.Ignore;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

@Ignore
public class XmlContigWriterTest extends XmlWriterTest {

	public void testWrite() throws IOException {
		LocationFactory locationFactory = new LocationFactory();
		entry.getSequence().addContig(
				locationFactory.createRemoteRange("A00001", 1, 1l, 210l));
		entry.getSequence().addContig(locationFactory.createUnknownGap(100));
		entry.getSequence().addContig(locationFactory.createGap(1003l));
		entry.getSequence().addContig(
				locationFactory.createRemoteRange("A00001", 1, 1l, 210l));			
		entry.getSequence().addContig(
				locationFactory.createRemoteRange("A00001", 1, 1l, 210l));			
		entry.getSequence().addContig(
				locationFactory.createRemoteRange("A00001", 1, 1l, 210l));
		Location location = locationFactory.createRemoteRange("A00001", 1, 1l, 210l);
		location.setComplement(true);
		entry.getSequence().addContig(location);			
		entry.getSequence().addContig(
				locationFactory.createRemoteRange("A00001", 1, 1l, 210l));			
	    StringWriter writer = new StringWriter();
	    assertTrue(new XmlContigWriter(entry).write(new SimpleXmlWriter(writer)));
	    //System.out.println(writer.toString());
	    assertEquals(
	    		"<contig>\n" +
	    		"	<range primaryBegin=\"1\" primaryEnd=\"210\" begin=\"1\" end=\"210\" accession=\"A00001\" version=\"1\"/>\n" +
	    		"	<gap begin=\"211\" end=\"310\" length=\"100\" unknownLength=\"true\"/>\n" +
	    		"	<gap begin=\"311\" end=\"1313\" length=\"1003\"/>\n" +
	    		"	<range primaryBegin=\"1\" primaryEnd=\"210\" begin=\"1314\" end=\"1523\" accession=\"A00001\" version=\"1\"/>\n" +
	    		"	<range primaryBegin=\"1\" primaryEnd=\"210\" begin=\"1524\" end=\"1733\" accession=\"A00001\" version=\"1\"/>\n" +
	    		"	<range primaryBegin=\"1\" primaryEnd=\"210\" begin=\"1734\" end=\"1943\" accession=\"A00001\" version=\"1\"/>\n" +
	    		"	<range primaryBegin=\"1\" primaryEnd=\"210\" begin=\"1944\" end=\"2153\" accession=\"A00001\" version=\"1\" complement=\"true\"/>\n" +
	    		"	<range primaryBegin=\"1\" primaryEnd=\"210\" begin=\"2154\" end=\"2363\" accession=\"A00001\" version=\"1\"/>\n" +
	    		"</contig>\n",
	    		writer.toString());
		}
}
