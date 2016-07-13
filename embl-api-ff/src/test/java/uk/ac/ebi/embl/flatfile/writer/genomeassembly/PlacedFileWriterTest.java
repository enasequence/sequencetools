/*
 * # Copyright 2012-2013 EMBL-EBI, Hinxton outstation
*
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
*
# http://www.apache.org/licenses/LICENSE-2.0
*
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
 */
package uk.ac.ebi.embl.flatfile.writer.genomeassembly;

import java.io.IOException;
import java.io.StringWriter;

import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord.Field;
import uk.ac.ebi.embl.flatfile.writer.genomeassembly.GenomeAssemblyWriterTest;
import uk.ac.ebi.embl.flatfile.writer.genomeassembly.PlacedFileWriter;

public class PlacedFileWriterTest extends GenomeAssemblyWriterTest
{
	public void testWrite_All() throws IOException {
		gaRecord.addField(new Field("OBJECT_NAME","cb25.NA_011"));
		gaRecord.addField(new Field("OBJECT_NAME","cb25.NA_012"));
		StringWriter writer = new StringWriter();
        assertTrue(new PlacedFileWriter(gaRecord).write(writer));
        assertEquals(
        		"cb25.NA_011\n" +
        		"cb25.NA_012\n", 
        		writer.toString());
    }
	

}
