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
package uk.ac.ebi.embl.flatfile.writer;

import uk.ac.ebi.embl.api.entry.Assembly;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class AssemblyWriter extends FlatFileWriter {

	public static int LOCAL_SPAN_COLUMN_WIDTH = 20;
	public static int PRIMARY_IDENTIFIER_COLUMN_WIDTH = 19;
	public static int PRIMARY_SPAN_COLUMN_WIDTH = 20;
	
	private String header;

	public AssemblyWriter(Entry entry, String header) {
		super(entry);
		this.header = header;
	}

	public boolean write(Writer writer) throws IOException {
		List<Assembly> assemblies = entry.getAssemblies();
		if (assemblies == null ||
			assemblies.size() == 0) {
			return false;
		}

		for (Assembly assembly : assemblies) {
			writer.write(header);

			LocalRange secondarySpan = assembly.getSecondarySpan();
			String locanSpanString = secondarySpan.getBeginPosition() + "-" +
				secondarySpan.getEndPosition();
			
			RemoteRange primarySpan = assembly.getPrimarySpan();
			String primaryIdentifierString = primarySpan.getVersion()==null?
					                         primarySpan.getAccession() : 
					                         primarySpan.getAccession()+ "." + primarySpan.getVersion();
			
			String primarySpanString = primarySpan.getBeginPosition() + "-" + 
				primarySpan.getEndPosition();
			
			String complementString = "";
			if (primarySpan.isComplement()) {
				complementString = "c";
			}

			java.util.Formatter formatter = new java.util.Formatter();
			String line = formatter.format(
					"%-" + LOCAL_SPAN_COLUMN_WIDTH + "s%-" +
					PRIMARY_IDENTIFIER_COLUMN_WIDTH + "s%-" +
					PRIMARY_SPAN_COLUMN_WIDTH + "s%s",
					new Object[] { 
							locanSpanString,
							primaryIdentifierString, 
							primarySpanString,
							complementString }).toString();
			writer.write(FlatFileUtils.trimRight(line));
			writer.write("\n");
		}
		return true;		
	}
}
