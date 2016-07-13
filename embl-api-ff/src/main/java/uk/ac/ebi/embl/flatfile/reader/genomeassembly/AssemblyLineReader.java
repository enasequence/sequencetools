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
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.Field;
import uk.ac.ebi.embl.flatfile.reader.genomeassembly.LineReader;

public class AssemblyLineReader extends GenomeAssemblyAbstractLineReader
{
	//used by unit tests
	protected AssemblyLineReader(GenomeAssemblyRecord gaRecord,LineReader lineReader)
	{
		super(gaRecord,lineReader);
		
	}

	public AssemblyLineReader(LineReader lineReader)
	{
		super(lineReader);
		
	}

	@Override
	protected void readLine(String line)
	{
		Field assemblyField;
		
		String[] tokens = line.split("\t");
		if (tokens.length != 2)
		{
			tokens=line.split("\\s+");
		}
		
		//System.out.println("tokens = " + tokens.length);
		if (tokens.length >= 2)
		{
			assemblyField = new Field(tokens[0],line.substring(tokens[0].length()).trim());
		}
		else
		{
			assemblyField = new Field();
			assemblyField.setValid(false);
		}
        assert assemblyField!=null;
		assemblyField.setOrigin(getOrigin());
		gaRecord.addField(assemblyField);
		gaRecord.setOrigin(getOrigin());
			
	}
}

