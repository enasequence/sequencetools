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

import junit.framework.TestCase;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public abstract class GenomeAssemblyReaderTest extends TestCase
{

	protected GenomeAssemblyRecord gaRecord;
	protected GenomeAssemblyAbstractLineReader genomeLineReader;

	protected void setUp() throws Exception
	{
		super.setUp();
		ValidationMessageManager.addBundle(FlatFileValidations.GENOMEASSEMBLY_FLAT_FILE_BUNDLE);
		gaRecord = new GenomeAssemblyRecord();
	}

	protected void initLineReader(String line,String fileType) throws IOException
	{  
		LineReader reader= new LineReader(new BufferedReader(new StringReader(line)));
	    reader.readLine();
		if (fileType.equals(GenomeAssemblyRecord.ASSEMBLY_FILE_TYPE))
		{
			genomeLineReader = new AssemblyLineReader(gaRecord,reader);
		} else if (fileType.equals(GenomeAssemblyRecord.CHROMOSOME_FILE_TYPE))
		{
			genomeLineReader = new ChromosomeLineReader(gaRecord,reader);
		} else if (fileType.equals(GenomeAssemblyRecord.PLACED_FILE_TYPE))
		{   
			genomeLineReader = new PlacedLineReader(gaRecord,reader);
		} else if (fileType.equals(GenomeAssemblyRecord.UNLOCALISED_FILE_TYPE))
		{
			genomeLineReader = new UnLocalisedLineReader(gaRecord,reader);
		} else if (fileType.equals(GenomeAssemblyRecord.UNPLACED_FILE_TYPE))
		{
			genomeLineReader = new UnplacedLineReader(gaRecord,reader);
		}
		genomeLineReader.read(gaRecord);
	}

}
