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
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.ChromosomeDataRow;

public class ChromosomeFileWriter extends GenomeAssemblyFileWriter
{
	GenomeAssemblyRecord gaRecord;

	protected ChromosomeFileWriter(GenomeAssemblyRecord gaRecord)
	{
		this.gaRecord = gaRecord;
	}

	@Override
	public boolean write(Writer writer)
	{
		if (gaRecord == null)
			return false;
		@SuppressWarnings("unchecked")
		ArrayList<ChromosomeDataRow> rows = (ArrayList<ChromosomeDataRow>) gaRecord.getFields();
		for (ChromosomeDataRow row : rows)
		{
			writeRow(row.get_object_name(), row.get_chromosome_name(), WordUtils.capitalize(row.get_type().toLowerCase()), (row.get_location()!=null)?WordUtils.capitalize(row.get_location().toLowerCase()):null, writer);
		}
		return true;
	}
}
