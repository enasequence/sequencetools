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
package uk.ac.ebi.embl.flatfile.writer.genbank;


import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.flatfile.GenbankPadding;
import uk.ac.ebi.embl.flatfile.writer.WrapChar;
import uk.ac.ebi.embl.flatfile.writer.WrapType;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;

import java.io.IOException;
import java.io.Writer;

/** Flat file writer for the ORGANISM lines.
 */
public class OrganismWriter extends FlatFileWriter {

	public OrganismWriter(Entry entry, SourceFeature sourceFeature, WrapType wrapType) {
		super(entry, wrapType);
		this.sourceFeature = sourceFeature;
		setWrapChar(WrapChar.WRAP_CHAR_SPACE);
	}

	private SourceFeature sourceFeature;
	
	public boolean write(Writer writer) throws IOException {
		String header = GenbankPadding.ORGANISM_PADDING;
		Taxon taxon = sourceFeature.getTaxon();
		if (taxon != null) {
	    	writeBlock(writer, GenbankPadding.ORGANISM_PADDING,
	    			GenbankPadding.BLANK_PADDING,
	    			taxon.getScientificName());
	    	header = GenbankPadding.BLANK_PADDING;
		}
		if(taxon != null && taxon.getLineage() != null && taxon.getFamilyNames().size() > 0){
			StringBuilder block = new StringBuilder();
			boolean isFirstTaxon = true;
			for(String familyName : taxon.getFamilyNames()) {
				if (!isFirstTaxon) {
					block.append("; ");
				}
				else {
					isFirstTaxon = false;
				}
				block.append(familyName);
			}					
			block.append(".");
			writeBlock(writer, header, block.toString());
		}
		else {
			writeBlock(writer, header, "unclassified sequences.");                    
		}
		return true;
	}		
}
