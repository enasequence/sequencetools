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
package uk.ac.ebi.embl.flatfile.reader.genbank;

import java.io.IOException;

import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.genbank.LocusReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileOrigin;

public class LocusReaderTest extends GenbankReaderTest {
	
	public void testRead_NoTopology() throws IOException {
		initLineReader(
				"LOCUS       SCU49845     5028 bp    DNA             PLN       21-JUN-1999"
		);
		ValidationResult result = (new LocusReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNull(entry.getPrimaryAccession());
		assertNull(entry.getSequence().getMoleculeType());
		assertEquals(Topology.LINEAR, entry.getSequence().getTopology());
		assertEquals("PLN", entry.getDivision());
		assertEquals(FlatFileUtils.getDay("21-JUN-1999"), entry.getLastUpdated());
	}

	public void testRead_CircularTopology() throws IOException {
		initLineReader(
				"LOCUS       SCU49845     5028 bp    RNA             circular PLN 21-JUN-1999"
		);
		ValidationResult result = (new LocusReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNull(entry.getPrimaryAccession());
		assertNull(entry.getSequence().getMoleculeType());
		assertEquals(Topology.CIRCULAR, entry.getSequence().getTopology());
		assertEquals("PLN", entry.getDivision());
		assertEquals(FlatFileUtils.getDay("21-JUN-1999"), entry.getLastUpdated());
	}
	
	public void testRead_LinearTopology() throws IOException {
		initLineReader(
				"LOCUS       SCU49845     5028 bp    DNA            linear PLN 21-JUN-1999"
		);
		ValidationResult result = (new LocusReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNull(entry.getPrimaryAccession());
		assertEquals(Topology.LINEAR, entry.getSequence().getTopology());
		assertEquals("PLN", entry.getDivision());
		assertEquals(FlatFileUtils.getDay("21-JUN-1999"), entry.getLastUpdated());
	}
	
	public void testRead_InvalidTopology() throws IOException {
		initLineReader(
				"LOCUS       SCU49845     5028 bp    DNA             spiky PLN 21-JUN-1999"
		);
		ValidationResult result = (new LocusReader(lineReader)).read(entry);
		assertEquals(1, result.count(Severity.ERROR));
		assertEquals(1, result.count("ID.1", Severity.ERROR));
	}
		
	public void testRead_NoMoleculeType() throws IOException {
		initLineReader(
				"LOCUS       SCU49845     5028 bp          PLN 21-JUN-1999"
		);
		ValidationResult result = (new LocusReader(lineReader)).read(entry);
		assertEquals(1, result.count(Severity.ERROR));
		assertEquals(1, result.count("FF.1", Severity.ERROR));
	}		
	
	public void testRead_NoDivision() throws IOException {
		initLineReader(
				"LOCUS       SCU49845     5028 bp    DNA         21-JUN-1999"
		);
		ValidationResult result = (new LocusReader(lineReader)).read(entry);
		assertEquals(1, result.count(Severity.ERROR));
		assertEquals(1, result.count("FF.1", Severity.ERROR));
	}
		
	public void testRead_NoSequenceLength() throws IOException {
		initLineReader(
			"LOCUS       SCU49845       DNA             PLN       21-JUN-1999"
		);
		ValidationResult result = (new LocusReader(lineReader)).read(entry);
		assertEquals(1, result.count(Severity.ERROR));
		assertEquals(1, result.count("FF.1", Severity.ERROR));
	}
		
	public void testRead_EmptyLine() throws IOException {
		initLineReader(
			"LOCUS"
		);
	}
		
	public void testRead_XXX() throws IOException {
		initLineReader(
				"LOCUS       XXX     5028 bp    DNA             PLN       21-JUN-1999"
		);
		ValidationResult result = (new LocusReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNull(entry.getPrimaryAccession());
		assertEquals(Topology.LINEAR, entry.getSequence().getTopology());
		assertEquals("PLN", entry.getDivision());
		assertEquals(FlatFileUtils.getDay("21-JUN-1999"), entry.getLastUpdated());
	}
	
	public void testRead_Origin() throws IOException {
		initLineReader(
				"LOCUS       SCU49845     5028 bp    DNA             PLN       21-JUN-1999"
		);
		ValidationResult result = (new LocusReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		FlatFileOrigin origin = (FlatFileOrigin)entry.getOrigin();
		assertEquals(1, origin.getFirstLineNumber());
		assertEquals(1, origin.getLastLineNumber());
	}
}
