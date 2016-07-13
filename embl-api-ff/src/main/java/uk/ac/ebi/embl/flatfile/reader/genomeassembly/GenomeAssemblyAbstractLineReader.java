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

import uk.ac.ebi.embl.api.genomeassembly.GenomeAssemblyRecord;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.genomeassembly.LineReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileOrigin;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;


public abstract class GenomeAssemblyAbstractLineReader
{
	LineReader lineReader;
    GenomeAssemblyRecord gaRecord;
    ValidationResult validationResult;
    int lineNumber;

    protected GenomeAssemblyAbstractLineReader(LineReader lineReader) {
        this.lineReader = lineReader;
    }

    //used by unit tests
    protected GenomeAssemblyAbstractLineReader(GenomeAssemblyRecord gaRecord,LineReader lineReader) {
    	this.gaRecord=gaRecord;
    	this.lineReader = lineReader;
    }

    public FlatFileOrigin getOrigin() {
        return new FlatFileOrigin(getLineReader().getFileId(), lineNumber);
    }

	public final ValidationResult read(GenomeAssemblyRecord gaRecord) throws IOException {
		this.gaRecord = gaRecord;
		validationResult = new ValidationResult();
		readLines();
		return validationResult;
	}

    protected void readLines(){
    	lineNumber = lineReader.getCurrentLineNumber();
		String line = lineReader.getCurrentLine();
		if (line != null && line.length() > 0) {
			readLine(line);
		}
    }

    protected abstract void readLine(String line);

    protected void error(String messageKey, Object... params) {
        validationResult
                .append(FlatFileValidations.message(lineReader.getCurrentLineNumber(), Severity.ERROR, messageKey,
                        params));
    }

    protected void warning(String messageKey, Object... params) {
        validationResult
                .append(FlatFileValidations.message(lineReader.getCurrentLineNumber(), Severity.ERROR, messageKey,
                        params));
    }

    protected LineReader getLineReader() {
        return lineReader;
    }
}
