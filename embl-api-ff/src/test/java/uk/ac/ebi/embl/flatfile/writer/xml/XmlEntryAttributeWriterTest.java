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

import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

public class XmlEntryAttributeWriterTest extends XmlWriterTest {

	public void testWrite() throws IOException {	
		SequenceFactory sequenceFactory = new SequenceFactory();
		entry.setSequence(sequenceFactory.createSequenceByte("aa".getBytes()));
		entry.setIdLineSequenceLength(2);
	    entry.setPrimaryAccession("DP000153");
	    entry.getSequence().setVersion(2);
	    entry.setVersion(3);
	    entry.setDataClass("CON");
	    entry.setDivision("MAM");
	    entry.getSequence().setMoleculeType("genomic RNA");	    
	    entry.getSequence().setTopology(Topology.LINEAR);
		entry.setFirstPublic(FlatFileUtils.getDay("06-SEP-2006"));
		entry.setLastUpdated(FlatFileUtils.getDay("05-SEP-2006"));            
		entry.setFirstPublicRelease(1);
		entry.setLastUpdatedRelease(2);
	    StringWriter writer = new StringWriter();
	    assertTrue(new XmlEntryAttributeWriter(entry).write(new SimpleXmlWriter(writer)));
	    //System.out.print(writer.toString());
	    assertEquals(
	    		" accession=\"DP000153\" version=\"2\" entryVersion=\"3\" dataClass=\"CON\" taxonomicDivision=\"MAM\" moleculeType=\"genomic RNA\" sequenceLength=\"2\" topology=\"linear\" firstPublic=\"2006-09-06\" firstPublicRelease=\"1\" lastUpdated=\"2006-09-05\" lastUpdatedRelease=\"2\"",	    		 
	    		writer.toString());
	}	
}
