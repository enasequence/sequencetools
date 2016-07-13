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

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.gff3.GFF3RecordSet;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.FlatFileReader;
import uk.ac.ebi.embl.flatfile.reader.LineReaderCache;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;
import uk.ac.ebi.embl.gff3.mapping.GFF3Mapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

/**
 * Reader for flat file gff3 entries.
 */
public class GFF3FlatFileEntryReader implements FlatFileReader<GFF3RecordSet> {

    protected LineReader lineReader;
    protected GFF3LineReader gffLineReader;
    protected ValidationResult validationResult;
    private GFF3RecordSet recordSet;
    private boolean isEntry;

    public GFF3FlatFileEntryReader(BufferedReader bufferedReader) {
        ValidationMessageManager.addBundle(FlatFileValidations.GFF3_FLAT_FILE_BUNDLE);
        this.lineReader = new LineReader(bufferedReader);
        this.gffLineReader = new GFF3LineReader(lineReader);
    }

    public final ValidationResult read() throws IOException {
        ValidationMessageManager.addBundle(FlatFileValidations.FLAT_FILE_BUNDLE);
        validationResult = new ValidationResult();
        readLines();
        return validationResult;
    }

    public GFF3RecordSet getEntry() {
        return recordSet;
    }

    public boolean isEntry() {
        return isEntry;
    }

    public LineReaderCache getCache() {
        return lineReader.getCache();
    }

    public void readLines() throws IOException {
        lineReader.readLine();
        isEntry = false;
        recordSet = new GFF3RecordSet();
        /**
         * only enter loop if the first line read is a line - as there are no terminator characters we have to decide
         * when the file is finished this way, rather than looking for a terminator tag.
         */
        if (lineReader.isCurrentLine()) {
            while (true) {
                if (!lineReader.isCurrentLine()) {
                    isEntry = true;//say its an entry when the file runs out - may add checks for format errors if needed
                    break;
                }

                append(gffLineReader.read(recordSet));
                lineReader.readLine();
            }
        }
		GFF3Mapper gm = new GFF3Mapper();
		List<Entry> emblEntryList = gm.mapGFF3ToEntry(recordSet);
     
     }

    protected void append(ValidationResult result) {
        this.validationResult.append(result);
    }

    protected void error(String messageKey, Object... params) {
        validationResult.append(FlatFileValidations.message(
                lineReader.getCurrentLineNumber(), Severity.ERROR, messageKey, params));
    }

    protected void warning(String messageKey, Object... params) {
        validationResult.append(FlatFileValidations.message(
                lineReader.getCurrentLineNumber(), Severity.WARNING, messageKey, params));
    }

    @Override
    public ValidationResult skip() throws IOException
    {
        return read();
    }
}
