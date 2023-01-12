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

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/** Flat file writer for the CC lines.
 */
public class CCWriter extends FlatFileWriter {

	public CCWriter(Entry entry) {
		super(entry);
	}

	public boolean write(Writer writer) throws IOException {
		if (entry.getComment() == null ||
			isBlankString(entry.getComment().getText())) {
			return false;
		}
		setForceLineBreak(true);
		setCustomMaximumLineLength(200);
		if(wrapType==WrapType.EMBL_WRAP)
		{
        setWrapChar(WrapChar.WRAP_CHAR_SPACE);
		StringBuilder block= new StringBuilder();
		block.append(entry.getComment().getText());
    	writeBlock(writer, EmblPadding.CC_PADDING, block.toString());
		}
		else
		{
		List<String> comments = Arrays.asList(entry.getComment().getText().split("\n"));

		for (String line : comments) {
			writeBlock(writer, EmblPadding.CC_PADDING, line);
		}
		}
		return true;
	}
}
