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
package uk.ac.ebi.embl.flatfile.writer.embl;

import java.io.IOException;
import java.io.StringWriter;

import uk.ac.ebi.embl.flatfile.writer.embl.ACStarWriter;

public class ACStarWriterTest extends EmblWriterTest {

	public void testWrite_SubmitterAccession() throws IOException {
        entry.setSubmitterAccession("_AAAAA");
        StringWriter writer = new StringWriter();
        assertTrue(new ACStarWriter(entry).write(writer));
        //System.out.print(writer.toString());
        assertEquals(
        		"AC * _AAAAA\n", 
        		writer.toString());
    }

	public void testWrite_SubmitterWgsVersion() throws IOException {
        entry.setSubmitterAccession("_AAAAA");
        entry.setSubmitterWgsVersion(34);
        StringWriter writer = new StringWriter();
        assertTrue(new ACStarWriter(entry).write(writer));
        //System.out.print(writer.toString());
        assertEquals(
        		"AC * _AAAAA 34\n", 
        		writer.toString());
    }
}
