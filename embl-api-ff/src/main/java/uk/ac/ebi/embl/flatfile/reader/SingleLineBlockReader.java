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
package uk.ac.ebi.embl.flatfile.reader;

import java.io.IOException;

import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileOrigin;

/** Reader for single line flat file blocks.
 */
public abstract class SingleLineBlockReader extends BlockReader {
		
    public SingleLineBlockReader(LineReader lineReader) {
    	super(lineReader);
    }

    private int lineNumber;
    
    public FlatFileOrigin getOrigin() {
    	if(!EmblEntryReader.isOrigin)
    	 return null;
    	else
    	return new FlatFileOrigin(getLineReader().getFileId(), lineNumber);
    }

    protected void readLines() throws IOException {
    	lineNumber = lineReader.getCurrentLineNumber();
		String line = lineReader.getCurrentShrinkedLine();
		if (line != null && line.length() > 0) {
			// Remove double spaces.
			line = FlatFileUtils.shrink(line);
			read(line);
		}
    }
}
