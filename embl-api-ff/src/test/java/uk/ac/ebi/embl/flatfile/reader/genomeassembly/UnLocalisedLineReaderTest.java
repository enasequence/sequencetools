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

import java.io.IOException;
import java.util.ArrayList;

import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.UnlocalisedDataRow;

public class UnLocalisedLineReaderTest extends GenomeAssemblyReaderTest
{
	@SuppressWarnings("unchecked")
	public void testReadValidLine() throws IOException
	{
		initLineReader("cb25.NA_084	III", GenomeAssemblyRecord.UNLOCALISED_FILE_TYPE);
		assertEquals((((ArrayList<UnlocalisedDataRow>) gaRecord.getFields()).size()), 1);
		assertEquals((((ArrayList<UnlocalisedDataRow>) gaRecord.getFields()).get(0).get_object_name()), "cb25.NA_084");
		assertEquals((((ArrayList<UnlocalisedDataRow>) gaRecord.getFields()).get(0).get_chromosome_name()), "III");

	}

	@SuppressWarnings("unchecked")
	public void testReadFail() throws IOException
	{
		initLineReader("cb25.NA_084III", GenomeAssemblyRecord.UNLOCALISED_FILE_TYPE);
		assertEquals((((ArrayList<UnlocalisedDataRow>) gaRecord.getFields()).size()), 0);
	}
}
