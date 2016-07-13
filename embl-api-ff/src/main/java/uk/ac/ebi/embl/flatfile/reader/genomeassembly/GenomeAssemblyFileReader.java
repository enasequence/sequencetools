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

import java.io.BufferedReader;
import java.io.IOException;

import uk.ac.ebi.embl.api.genomeassembly.AssemblyRecord;
import uk.ac.ebi.embl.api.genomeassembly.ChromosomeRecord;
import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord;
import uk.ac.ebi.embl.api.genomeassembly.PlacedRecord;
import uk.ac.ebi.embl.api.genomeassembly.UnlocalisedRecord;
import uk.ac.ebi.embl.api.genomeassembly.UnplacedRecord;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.FlatFileReader;
import uk.ac.ebi.embl.flatfile.reader.LineReaderCache;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;
import uk.ac.ebi.embl.flatfile.reader.genomeassembly.LineReader;

public class 
GenomeAssemblyFileReader implements FlatFileReader<GenomeAssemblyRecord>
{
	protected LineReader lineReader;
    protected GenomeAssemblyAbstractLineReader gaLineReader;
    protected ValidationResult validationResult;
    private GenomeAssemblyRecord genomeRecord;
    private boolean isEntry;
      public GenomeAssemblyFileReader(BufferedReader bufferedReader,String fileid) {
        ValidationMessageManager.addBundle(FlatFileValidations.GENOMEASSEMBLY_FLAT_FILE_BUNDLE);
        this.lineReader = new LineReader(bufferedReader,fileid);
		if (fileid.contains(GenomeAssemblyRecord.ASSEMBLY_FILE_TYPE))
		{
			genomeRecord = new AssemblyRecord();
			this.gaLineReader = new AssemblyLineReader(lineReader);

		} else if (fileid.contains(GenomeAssemblyRecord.CHROMOSOME_FILE_TYPE))
		{
			genomeRecord = new ChromosomeRecord();
			this.gaLineReader = new ChromosomeLineReader(lineReader);

		} else if (fileid.contains(GenomeAssemblyRecord.PLACED_FILE_TYPE))
		{
			genomeRecord = new PlacedRecord();
			this.gaLineReader = new PlacedLineReader(lineReader);

		} else if (fileid.contains(GenomeAssemblyRecord.UNPLACED_FILE_TYPE))
		{
			genomeRecord = new UnplacedRecord();
			this.gaLineReader = new UnplacedLineReader(lineReader);

		} else if (fileid.contains(GenomeAssemblyRecord.UNLOCALISED_FILE_TYPE))
		{
			genomeRecord = new UnlocalisedRecord();
			this.gaLineReader = new UnLocalisedLineReader(lineReader);

		} else
		{
			error("GA.1", fileid);
		}

	}

    public final ValidationResult read() throws IOException {
        ValidationMessageManager.addBundle(FlatFileValidations.GENOMEASSEMBLY_FLAT_FILE_BUNDLE);
        validationResult = new ValidationResult();
        readLines();
        return validationResult;
    }

    public GenomeAssemblyRecord getEntry() {
        return genomeRecord;
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
 
                append(gaLineReader.read(genomeRecord));
                lineReader.readLine();
            }
        }
		
     
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
    public ValidationResult
    skip() throws IOException
    {
        return read();
    }
}
