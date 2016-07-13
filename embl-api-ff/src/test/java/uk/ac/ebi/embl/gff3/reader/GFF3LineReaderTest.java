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
package uk.ac.ebi.embl.gff3.reader;

import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: lbower
 * Date: 21-Sep-2010
 * Time: 13:25:47
 * To change this template use File | Settings | File Templates.
 */
public class GFF3LineReaderTest extends GFF3ReaderTest {

    public void testRead() throws IOException {
        initLineReader("SJC_S000020\texonhunter\tdavid\t476254\t477153\t123.34\t+\t0\tID=Sjp_0006480;Name=Sjp_0006480;Alias=Sjc_0006480");
        ValidationResult validationResult = new GFF3LineReader(lineReader).read(entry);
        assertTrue(validationResult.isValid());
        assertTrue(entry.getRecords().size() == 1);
        assertTrue(entry.getRecords().get(0).getSequenceID().equals("SJC_S000020"));
        assertTrue(entry.getRecords().get(0).getSource().equals("exonhunter"));
        assertTrue(entry.getRecords().get(0).getType().equals("david"));
        assertTrue(entry.getRecords().get(0).getStart() == 476254);
        assertTrue(entry.getRecords().get(0).getEnd() == 477153);
        assertTrue(entry.getRecords().get(0).getPhase() == 0);
        assertTrue(entry.getRecords().get(0).getScore() == 123.34);
        assertTrue(entry.getRecords().get(0).getStrand() == 1);
        assertTrue(entry.getRecords().get(0).getAttributes().size() == 3);
        assertTrue(entry.getRecords().get(0).getAttributes().get("ID").equals("Sjp_0006480"));
    }

    public void testWrongColumnNumber() throws IOException {
        initLineReader("SJC_S000020\texonhunter\tdavid\t476254\t123.34\t+\t0\tID=Sjp_0006480;Name=Sjp_0006480;Alias=Sjc_0006480");
        ValidationResult validationResult = new GFF3LineReader(lineReader).read(entry);
        assertFalse(validationResult.isValid());
        assertEquals(1, validationResult.count("GFF.1", Severity.ERROR));
    }

    public void testUnmatchedAttribute() throws IOException {
        initLineReader("SJC_S000020\texonhunter\tdavid\t476254\t477153\t123.34\t+\t0\tID=Sjp_0006480;Name;Alias=Sjc_0006480");
        ValidationResult validationResult = new GFF3LineReader(lineReader).read(entry);
        assertFalse(validationResult.isValid());
        assertEquals(1, validationResult.count("GFF.8", Severity.ERROR));
    }

    public void testUnmatchedAttribute2() throws IOException {
        initLineReader("SJC_S000020\texonhunter\tdavid\t476254\t477153\t123.34\t+\t0\tID=Sjp_0006480;Name=;Alias=Sjc_0006480");
        ValidationResult validationResult = new GFF3LineReader(lineReader).read(entry);
        assertFalse(validationResult.isValid());
        assertEquals(1, validationResult.count("GFF.8", Severity.ERROR));
    }

    public void testReadInvalidNumbers() throws IOException {
        initLineReader("SJC_S000020\texonhunter\tdavid\tnot a number\tnot a number\tnot a number\t+\tnot a number\tID=Sjp_0006480;Name=Sjp_0006480;Alias=Sjc_0006480");
        ValidationResult validationResult = new GFF3LineReader(lineReader).read(entry);
        assertTrue(!validationResult.isValid());
        assertEquals(1, validationResult.count("GFF.3", Severity.ERROR));
        assertEquals(1, validationResult.count("GFF.4", Severity.ERROR));
        assertEquals(1, validationResult.count("GFF.5", Severity.ERROR));
        assertEquals(1, validationResult.count("GFF.7", Severity.ERROR));
    }

    public void testReadInvalidStrand() throws IOException {
        initLineReader("SJC_S000020\texonhunter\tdavid\t476254\t477153\t123.34\tBAD_STRAND\t0\tID=Sjp_0006480;Name=Sjp_0006480;Alias=Sjc_0006480");
        ValidationResult validationResult = new GFF3LineReader(lineReader).read(entry);
        assertTrue(!validationResult.isValid());
        assertEquals(1, validationResult.count("GFF.6", Severity.ERROR));
    }

    public void testReadCDSNoPhase() throws IOException {
        initLineReader("SJC_S000020\texonhunter\tCDS\t476254\t477153\t123.34\t+\t.\tID=Sjp_0006480;Name=Sjp_0006480;Alias=Sjc_0006480");
        ValidationResult validationResult = new GFF3LineReader(lineReader).read(entry);
        assertTrue(!validationResult.isValid());
        assertEquals(1, validationResult.count("GFF.9", Severity.ERROR));
    }
}
