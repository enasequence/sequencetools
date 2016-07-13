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
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.ChromosomeDataRow;
import uk.ac.ebi.embl.flatfile.reader.genomeassembly.LineReader;

public class ChromosomeLineReader extends GenomeAssemblyAbstractLineReader
{
	protected ChromosomeLineReader(GenomeAssemblyRecord gaRecord,LineReader lineReader)
	{
       super(gaRecord,lineReader);
	}
	public ChromosomeLineReader(LineReader lineReader)
	{
		super(lineReader);

	}

	@Override
	protected void readLine(String line)
	{
		ChromosomeDataRow chromosomeDataRow;
		String[] tokens = line.split("\t");
		if (tokens.length != 3 && tokens.length != 4)
		{
			tokens=line.split("\\s+");
		}
		
		//System.out.println("tokens = " + tokens.length);
		if (tokens.length != 3 && tokens.length != 4)
		{
			chromosomeDataRow = new ChromosomeDataRow();
			chromosomeDataRow.setValid(false);
		}
		else
		{
		if (tokens.length == 3)
			chromosomeDataRow = new ChromosomeDataRow(tokens[0], tokens[1], tokens[2], null);
		else
			chromosomeDataRow = new ChromosomeDataRow(tokens[0], tokens[1], tokens[2], tokens[3]);
		}
		assert chromosomeDataRow!=null;
		gaRecord.addField(chromosomeDataRow);
		chromosomeDataRow.setOrigin(getOrigin());
		gaRecord.setOrigin(getOrigin());

	}

}
