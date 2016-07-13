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

import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord;
import uk.ac.ebi.embl.api.genomeassembly.PlacedRecord;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.Field;
import uk.ac.ebi.embl.flatfile.reader.genomeassembly.LineReader;

public class PlacedLineReader extends GenomeAssemblyAbstractLineReader
{
	// String[]

	protected PlacedLineReader(GenomeAssemblyRecord gaRecord,LineReader lineReader)
	{
       super(gaRecord,lineReader);
	}

	public PlacedLineReader(LineReader lineReader)
	{
		super(lineReader);

	}

	@Override
	protected void readLine(String line)
	{
			String[] tokens = line.split("\\s+");
		if (tokens.length > 1)
		{
			error("GA.3", GenomeAssemblyRecord.PLACED_FILE_TYPE);
			return;
		}
		Field placedField = new Field(PlacedRecord.OBJECT_NAME_KEYWORD,line);
		placedField.setOrigin(getOrigin());
		gaRecord.addField(placedField);
		gaRecord.setOrigin(getOrigin());

	}

}
