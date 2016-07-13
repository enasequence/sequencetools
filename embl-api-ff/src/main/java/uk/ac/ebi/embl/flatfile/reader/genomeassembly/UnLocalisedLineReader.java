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
package uk.ac.ebi.embl.flatfile.reader.genomeassembly;

import java.util.ArrayList;
import java.util.regex.Pattern;

import uk.ac.ebi.embl.api.genomeassembly.ChromosomeRecord;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.UnlocalisedDataRow;
import uk.ac.ebi.embl.flatfile.reader.genomeassembly.LineReader;

public class UnLocalisedLineReader extends GenomeAssemblyAbstractLineReader
{
	// String[]

	protected UnLocalisedLineReader(GenomeAssemblyRecord gaRecord,LineReader lineReader)
	{
       super(gaRecord,lineReader);
	}

	public UnLocalisedLineReader(LineReader lineReader)
	{
		super(lineReader);

	}

	Pattern pattern = Pattern.compile("\\s+");
	
	@Override
	protected void readLine(String line)
	{
		UnlocalisedDataRow unlocalisedDataRow;
		String[] tokens = pattern.split(line);

		if (tokens.length != 2)
		{
			error("GA.2", GenomeAssemblyRecord.UNLOCALISED_FILE_TYPE, tokens.length, 2);
			return;
		}

		unlocalisedDataRow = new UnlocalisedDataRow(tokens[0], tokens[1]);
		unlocalisedDataRow.setOrigin(getOrigin());
		gaRecord.addField(unlocalisedDataRow);
		gaRecord.setOrigin(getOrigin());

	}

}
