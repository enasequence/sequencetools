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
import java.util.List;

import uk.ac.ebi.embl.api.entry.location.Gap;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;
import uk.ac.ebi.embl.api.validation.FlatFileOrigin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.genbank.ContigReader;

public class ContigReaderTest extends GenbankReaderTest {

	public void testRead() throws IOException {
		initLineReader(
			"CONTIG      join(AL358912.1:1..39187,\n" +
			"CONTIG      gap(unk100),gap(43),complement(AAOX01000077.1:1..2879))\n"
		);
		ValidationResult result = (new ContigReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		List<Location> contigs = entry.getSequence().getContigs();
		assertEquals(4, contigs.size());
		assertTrue(contigs.get(0) instanceof RemoteRange);
		assertTrue(contigs.get(1) instanceof Gap);
		assertTrue(contigs.get(2) instanceof Gap);
		assertTrue(contigs.get(3) instanceof RemoteRange);
		RemoteRange remoteRange = (RemoteRange)contigs.get(0);
		assertEquals(
				"AL358912",
				remoteRange.getAccession());
		assertEquals(
				new Integer(1),
				remoteRange.getVersion());
		assertEquals(
				new Long(1),
				remoteRange.getBeginPosition());
		assertEquals(
				new Long(39187),
				remoteRange.getEndPosition());
		Gap gap = (Gap)contigs.get(1);
		assertTrue(gap.isUnknownLength());
		gap = (Gap)contigs.get(2);
		assertFalse(gap.isUnknownLength());
		assertEquals(43L, gap.getLength());
		remoteRange = (RemoteRange)contigs.get(3);
		assertTrue(remoteRange.isComplement());
		assertEquals(
				"AAOX01000077",
				remoteRange.getAccession());
		assertEquals(
				new Integer(1),
				remoteRange.getVersion());
		assertEquals(
				new Long(1),
				remoteRange.getBeginPosition());
		assertEquals(
				new Long(2879),
				remoteRange.getEndPosition());		
	}
	
	public void testRead_Origin() throws IOException {
		initLineReader(
			"CONTIG      join(AL358912.1:1..39187,\n" +
			"CONTIG      gap(unk100),gap(43),complement(AAOX01000077.1:1..2879))\n"
		);
		ValidationResult result = (new ContigReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		List<Location> contigs = entry.getSequence().getContigs();
		assertEquals(4, contigs.size());
		FlatFileOrigin origin = (FlatFileOrigin)contigs.get(0).getOrigin();
		assertEquals(1, origin.getFirstLineNumber());
		assertEquals(2, origin.getLastLineNumber());	
		origin = (FlatFileOrigin)contigs.get(1).getOrigin();
		assertEquals(1, origin.getFirstLineNumber());
		assertEquals(2, origin.getLastLineNumber());	
		origin = (FlatFileOrigin)contigs.get(2).getOrigin();
		assertEquals(1, origin.getFirstLineNumber());
		assertEquals(2, origin.getLastLineNumber());	
		origin = (FlatFileOrigin)contigs.get(3).getOrigin();
		assertEquals(1, origin.getFirstLineNumber());
		assertEquals(2, origin.getLastLineNumber());	
	}	
}
