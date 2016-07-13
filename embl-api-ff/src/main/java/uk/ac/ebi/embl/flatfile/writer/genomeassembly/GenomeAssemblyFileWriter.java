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
package uk.ac.ebi.embl.flatfile.writer.genomeassembly;

import java.io.Writer;

import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord;

public abstract class GenomeAssemblyFileWriter
{
	public abstract boolean write(Writer writer);

	public static GenomeAssemblyFileWriter getWriter(GenomeAssemblyRecord gaRecord)
	{
		if (gaRecord.isAssembly())
		{
			return new AssemblyFileWriter(gaRecord);
		}
		if (gaRecord.isChromosome())
		{
			return new ChromosomeFileWriter(gaRecord);
		}
		if (gaRecord.isUnLocalised())
		{
			return new UnlocalisedFileWriter(gaRecord);
		}
		if (gaRecord.isPlaced())
		{
			return new PlacedFileWriter(gaRecord);
		}
		if (gaRecord.isUnplaced())
		{
			return new UnplacedFileWriter(gaRecord);
		}

		return null;
	}

	public boolean writeRow(Object column1, Object column2, Object column3, Object column4, Writer writer)
	{
		StringBuffer rowString = new StringBuffer();

		if (column1 != null)
			rowString.append(column1);
		if (column2 != null)
			rowString.append("\t" + column2);
		if (column3 != null)
			rowString.append("\t" + column3);
		if (column4 != null)
			rowString.append("\t" + column4);
		try
		{
			writer.write(rowString.toString() + "\n");
		} catch (Exception e)
		{
			return false;
		}
		return true;
	}
}
