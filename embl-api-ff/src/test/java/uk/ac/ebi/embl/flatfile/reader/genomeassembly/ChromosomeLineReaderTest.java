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
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.ChromosomeDataRow;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.Field;
import uk.ac.ebi.embl.api.validation.ValidationResult;


public class ChromosomeLineReaderTest extends GenomeAssemblyReaderTest
{
	 @SuppressWarnings("unchecked")
	public void testReadValidLine() throws IOException {
		    initLineReader("chI       I    chromosome",GenomeAssemblyRecord.CHROMOSOME_FILE_TYPE);
		    assertEquals((((ArrayList<ChromosomeDataRow>)gaRecord.getFields()).size()),1);
		    assertEquals((((ArrayList<ChromosomeDataRow>)gaRecord.getFields()).get(0).get_object_name()),"chI");
		    assertEquals((((ArrayList<ChromosomeDataRow>)gaRecord.getFields()).get(0).get_chromosome_name()),"I");
		    assertEquals((((ArrayList<ChromosomeDataRow>)gaRecord.getFields()).get(0).get_type()),"chromosome");

			
	    }
	 @SuppressWarnings("unchecked")
	public void testReadFail() throws IOException {
		 initLineReader("chI       Ichromosome",GenomeAssemblyRecord.CHROMOSOME_FILE_TYPE);
		    assertEquals((((ArrayList<ChromosomeDataRow>)gaRecord.getFields()).size()),1);
		    assertEquals((((ArrayList<ChromosomeDataRow>)gaRecord.getFields()).get(0).get_object_name()),null);
		    assertEquals((((ArrayList<ChromosomeDataRow>)gaRecord.getFields()).get(0).get_chromosome_name()),null);
		    assertEquals((((ArrayList<ChromosomeDataRow>)gaRecord.getFields()).get(0).get_type()),null);
	    }
}
