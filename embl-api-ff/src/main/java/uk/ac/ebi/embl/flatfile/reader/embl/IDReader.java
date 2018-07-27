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

import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.reader.FlatFileMatcher;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.SingleLineBlockReader;

/** Reader for the flat file ID lines.
 */
public class IDReader extends SingleLineBlockReader {
	boolean preserveCase = false;
	
	public IDReader(LineReader lineReader) {
		super(lineReader);
	}
	
	public IDReader(LineReader lineReader, boolean preserveCase) {
		super(lineReader);
		this.preserveCase = preserveCase;
	}

	private static final Pattern PATTERN = Pattern.compile(
				"^\\s*" +
				"([^\\s;]+)?" + // primary accession
				"\\s*" +
				";" +
				"\\s*" +
				"(SV\\s*)?" +
				"([^\\s;]+)?" + // sequence version
				"\\s*" +
				";" +  	
		        "\\s*" +
		        "([^\\s;]+)?" + // topology
		        "\\s*" +
		        ";" +
		        "\\s*" +
		        "([^;]+)?" + // molecule type
		        "\\s*" +
		        ";" +
		        "\\s*" +
		        "([^\\s;]+)?" + // data class
		        "\\s*" +
		        ";" +          
		        "\\s*" +
		        "([^\\s;]+)?" + // division
		        "\\s*" +
		        ";" +
		        "\\s*" +
		        "(\\d+)?" + // sequence length
		        ".*$");

	private static int GROUP_PRIMARY_ACCESSION = 1;
	private static int GROUP_SEQUENCE_VERSION = 3;
	private static int GROUP_TOPOLOGY = 4;
	private static int GROUP_MOLECULE_TYPE = 5;
	private static int GROUP_DATACLASS = 6;
	private static int GROUP_DIVISION = 7;
	private static int GROUP_SEQUENCE_LENGTH = 8;

	@Override
	public String getTag() {
		return EmblTag.ID_TAG;
	}
	
	@Override
	protected void read(String block) {
		entry.setOrigin(getOrigin());
		FlatFileMatcher matcher = new FlatFileMatcher(this, PATTERN);
		if(!matcher.match(block)) {
			error("FF.1", getTag());
			return;
		}
		if (matcher.isValueXXX(GROUP_PRIMARY_ACCESSION)) {
			if (preserveCase) {
				entry.setPrimaryAccession(matcher.getString(GROUP_PRIMARY_ACCESSION));
				entry.getSequence().setAccession(matcher.getString(GROUP_PRIMARY_ACCESSION));
			}
			else {
				entry.setPrimaryAccession(matcher.getUpperString(GROUP_PRIMARY_ACCESSION));
				entry.getSequence().setAccession(matcher.getUpperString(GROUP_PRIMARY_ACCESSION));
			}
					
		}
		if (matcher.isValueXXX(GROUP_SEQUENCE_VERSION) && matcher.getInteger(GROUP_SEQUENCE_VERSION) >= 1) {
			entry.getSequence().setVersion(matcher.getInteger(GROUP_SEQUENCE_VERSION));
		}

		Topology topology = getTopology(matcher.getString(GROUP_TOPOLOGY));
		entry.getSequence().setTopology(topology);
		if (matcher.isValueXXX(GROUP_MOLECULE_TYPE)) {
			entry.getSequence().setMoleculeType(matcher.getString(GROUP_MOLECULE_TYPE));
			/*if (entry.getSequence().getMoleculeType() == null) {
				error("ID.3");
			}*/
		}
		Long sequenceLength = matcher.getLong(GROUP_SEQUENCE_LENGTH);
		if (sequenceLength != null) {
			entry.setIdLineSequenceLength(matcher.getLong(GROUP_SEQUENCE_LENGTH));			
		}
		if (matcher.isValueXXX(GROUP_DATACLASS)) {
			entry.setDataClass(matcher.getUpperString(GROUP_DATACLASS));
			}
		if (matcher.isValueXXX(GROUP_DIVISION)) {
			entry.setDivision(matcher.getUpperString(GROUP_DIVISION));
		}
	}
	
	private Topology getTopology(String topology) {
		if (topology == null) {
			error("ID.2");
			return null;
		}
		topology = topology.toLowerCase();
        if ( topology.equals("linear")) {
        	return Topology.LINEAR;
        }
        else if ( topology.equals( "circular" )) {
        	return Topology.CIRCULAR;
        }
        error("ID.1", topology);
        return null;
	}
}
