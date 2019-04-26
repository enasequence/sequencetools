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
package uk.ac.ebi.embl.gff3.writer;

//http://www.sequenceontology.org/gff3.shtml

import java.io.IOException;
import java.io.Writer;

/** Writes features in GFF3 format for AnnotationSketch.
 * */
public class GFF3Writer {
		
	public static void writeVersionPragma(Writer writer) throws IOException {
		writer.write("##gff-version   3\n");
	}	

	public static void writeRegionPragma(Writer writer, String accession, 
			Long beginPos, Long endPos) throws IOException {
		writer.write("##sequence-region ");
		writer.write(accession);
		writer.write(" ");
		writer.write(beginPos.toString());
		writer.write(" ");
		writer.write(endPos.toString());
		writer.write("\n");
	}	
			
	public static void writeAttribute(Writer writer, String tag, 
			String value) throws IOException {
		writer.write(tag);
		writer.write("=");
		writer.write(value);
	}

	private static void writeStrandColumn(Writer writer, Boolean isComplement) throws IOException {
		/*
		 * The strand of the feature. + for positive strand (relative to the landmark), 
		 * - for minus strand, and . for features that are not stranded. In addition, 
		 * ? can be used for features whose strandedness is relevant, but unknown. 
		 */
		if (isComplement == null) {
			// writeColumn(writer, "."); // AnnotationSketch generates both left and right arrows
			writeColumn(writer, "?"); // AnnotationSketch generates no arrows
		}
		else if (isComplement)
			writeColumn(writer, "+");
		else
			writeColumn(writer, "-");
	}	

	public static void writeColumns(Writer writer,
			String accession, String type, 
			Long beginPosition,	Long endPosition, 
			Boolean isComplement) throws IOException {
		writeColumns(writer, accession, type, 
				beginPosition, endPosition, isComplement, null);
	}

	public static void writeColumns(Writer writer,
			String accession, String type, 
			Long beginPosition,	Long endPosition) throws IOException {
		writeColumns(writer, accession, type, 
				beginPosition, endPosition, null, null);
	}
	
	public static void writeColumns(Writer writer,
			String accession, String type, 
			Long beginPosition,	Long endPosition, 
			Boolean isComplement, String phase) throws IOException {
		writeColumn(writer, accession);
		writeColumn(writer, "ENA");
		// Types in AnnotationSketch must not contain single quotes.
		// Replace single quotes with an underscore.
		type = type.replace('\'', '_');
		// Types in AnnotationSketch must not contain dash.
		// Replace dash with an underscore.
		type = type.replace('-', '_');
		// Types in AnnotationSketch must not start with a number.
		// Prefix all feature names with ENA_.
		type = "ENA_" + type;
		writeColumn(writer, type);
		writeColumn(writer, String.valueOf(beginPosition));
		writeColumn(writer, String.valueOf(endPosition));
		writeScoreColumn(writer);
		writeStrandColumn(writer, isComplement);
		if (phase == null) {
			phase = ".";
		}
		writeColumn(writer, phase);
	}
	
	private static void writeColumn(Writer writer, String value) throws IOException {
		writer.write(value);
		writer.write("\t");
	}	
	
	private static void writeScoreColumn(Writer writer) throws IOException {
		writeColumn(writer, ".");
	}		
}
