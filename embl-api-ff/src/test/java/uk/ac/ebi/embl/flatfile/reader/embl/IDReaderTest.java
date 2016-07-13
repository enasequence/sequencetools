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
package uk.ac.ebi.embl.flatfile.reader.embl;

import java.io.IOException;

import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.embl.IDReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileOrigin;

public class IDReaderTest extends EmblReaderTest {
	
	public void testRead_LinearTopology() throws IOException {
		initLineReader(
			"iD   A00001; SV 1; linear; unassigned DNA; PaT; vrl; 335 BP."
		);
		ValidationResult result = (new IDReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals("A00001", entry.getPrimaryAccession());
		assertEquals(new Integer(1), entry.getSequence().getVersion());
		assertEquals(Topology.LINEAR, entry.getSequence().getTopology());
		assertEquals("unassigned DNA", entry.getSequence().getMoleculeType());
		assertEquals("PAT", entry.getDataClass());
		assertEquals("VRL", entry.getDivision());
		assertEquals(335, entry.getIdLineSequenceLength());
	}
	
	public void testRead_CircularTopology() throws IOException {
		initLineReader(
			"iD   A00001; SV 1; circular; unassigned DNA; PaT; vrl; 335 BP."
		);
		ValidationResult result = (new IDReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertEquals(Topology.CIRCULAR, entry.getSequence().getTopology());
	}	

	public void testRead_InvalidTopology() throws IOException {
		initLineReader(
			"iD   A00001; SV 1; spiky; unassigned DNA; PaT; vrl; 335 BP."
		);
		ValidationResult result = (new IDReader(lineReader)).read(entry);
		assertEquals(1, result.count(Severity.ERROR));
		assertEquals(1, result.count("ID.1", Severity.ERROR));
	}

	public void testRead_NoPrimaryAccession() throws IOException {
		initLineReader(
			"iD   ; SV 1; circular; unassigned DNA; PaT; vrl; 335 BP."
		);
		ValidationResult result = (new IDReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNull(entry.getPrimaryAccession());
	}	

	public void testRead_NoSequenceVersion() throws IOException {
		initLineReader(
			"iD   ; SV; circular; unassigned DNA; PaT; vrl; 335 BP."
		);
		ValidationResult result = (new IDReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNull(entry.getSequence().getVersion());
	}

	public void testRead_NoSequenceVersion2() throws IOException {
		initLineReader(
			"ID   FM178291; ; linear; genomic DNA; XXX; XXX; 637 BP."
		);
		ValidationResult result = (new IDReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNull(entry.getSequence().getVersion());
	}
		
	public void testRead_NoTopology() throws IOException {
		initLineReader(
			"iD   ; SV;    ; unassigned DNA; PaT; vrl; 335 BP."
		);
		ValidationResult result = (new IDReader(lineReader)).read(entry);
		assertEquals(1, result.count("ID.2", Severity.ERROR));
		assertNull(entry.getSequence().getTopology());
	}	
	
	public void testRead_NoMoleculeType() throws IOException {
		initLineReader(
			"iD   ; SV; circular;     ; PaT; vrl; 335 BP."
		);
		ValidationResult result = (new IDReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNull(entry.getSequence().getMoleculeType());
	}		
	
	public void testRead_NoDataClass() throws IOException {
		initLineReader(
			"iD   ; SV; circular; unassigned DNA; ; vrl; 335 BP."
		);
		ValidationResult result = (new IDReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNull(entry.getDataClass());
	}

	public void testRead_NoDivision() throws IOException {
		initLineReader(
			"iD   ; SV; circular; unassigned DNA; ; ; 335 BP."
		);
		ValidationResult result = (new IDReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNull(entry.getDivision());
	}

	public void testRead_EmptyLine() throws IOException {
		initLineReader(
			"iD"
		);
		ValidationResult result = (new IDReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		initLineReader(
			"iD   "
		);
		result = (new IDReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		initLineReader(
				"iD         "
		);
		result = (new IDReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
	}
	
	public void testRead_InvalidFormat() throws IOException {
		initLineReader(
			"iD   A00001; SV 1;"
		);
		ValidationResult result = (new IDReader(lineReader)).read(entry);
		assertEquals(1, result.count(Severity.ERROR));
	}		
	
	public void testRead_XXX() throws IOException {
		initLineReader(
				"iD   XXX; SV XXX; linear; XXX; XXX; XXX; 335 BP."
		);
		ValidationResult result = (new IDReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNull(entry.getPrimaryAccession());
		assertNull(entry.getSequence().getVersion());
		assertEquals(Topology.LINEAR, entry.getSequence().getTopology());
		assertNull( entry.getSequence().getMoleculeType());
		assertNull(entry.getDataClass());
		assertNull(entry.getDivision());
		assertEquals(335, entry.getIdLineSequenceLength());
	}	
	
	public void testRead_Origin() throws IOException {
		initLineReader(
				"iD   XXX; SV XXX; linear; XXX; XXX; XXX; 335 BP."
		);
		ValidationResult result = (new IDReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		FlatFileOrigin origin = (FlatFileOrigin)entry.getOrigin();
		assertEquals(1, origin.getFirstLineNumber());
		assertEquals(1, origin.getLastLineNumber());
	}
}
