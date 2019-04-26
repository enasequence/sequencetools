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
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;

public class LocusWriterTest extends GenbankWriterTest {

	public void testWrite_All() throws IOException {
		SequenceFactory sequenceFactory = new SequenceFactory();
		entry.setSequence(sequenceFactory.createSequenceofLength(335,'a'));
        entry.setPrimaryAccession("A00001");
        entry.getSequence().setTopology(Topology.LINEAR);
        entry.getSequence().setMoleculeType("genomic DNA");
       // entry.getSequence().setLength(335);
        entry.setDataClass("EST");
        entry.setLastUpdated(FlatFileUtils.getDay("05-SEP-2006"));
        StringWriter writer = new StringWriter();
        assertTrue(new LocusWriter(entry).write(writer));
        //System.out.print(writer.toString());
        assertEquals(
        		"LOCUS       A00001                   335 bp    DNA     linear   EST 05-SEP-2006\n", 
        		writer.toString());
    }

	public void testWrite_XXX() throws IOException {
        entry.setPrimaryAccession(null);
        entry.setVersion(null);
        entry.setDataClass(null);
        entry.setDivision(null);
        StringWriter writer = new StringWriter();
        assertTrue(new LocusWriter(entry).write(writer));
        //System.out.print(writer.toString());
        assertEquals(
        		"LOCUS       XXX                          bp    XXX     XXX      XXX XXX\n", 
        		writer.toString());
    }
}
