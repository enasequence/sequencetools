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
import java.io.Writer;
import java.util.HashSet;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.writer.EntryWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapType;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;

/** Flat file writer for the sequence entry.
 */
public class EmblEntryWriter extends EntryWriter {

	private boolean isConvff = false;
	public EmblEntryWriter(Entry entry) {
		super(entry);
		wrapType = WrapType.EMBL_WRAP;
	}


	public EmblEntryWriter(Entry entry, boolean isConvff) {
		super(entry);
		wrapType = WrapType.EMBL_WRAP;
		this.isConvff = isConvff;
	}


	public final static String SEPARATOR_LINE = EmblTag.XX_TAG  + "\n";
	public final static String TERMINATOR_LINE = EmblTag.TERMINATOR_TAG  + "\n";
	
	public boolean write(Writer writer) throws IOException {
		if (entry == null) {
			return false;
		}
		if(new IDWriter(entry).write(writer)) {
			writer.write(SEPARATOR_LINE);
		}
		if(new STStarWriter(entry).write(writer)) {
			writer.write(SEPARATOR_LINE);
		}
		if (new ACWriter(entry, wrapType).write(writer)) {
			writer.write(SEPARATOR_LINE);
		}
		if(new ACStarWriter(entry).write(writer)) {
			writer.write(SEPARATOR_LINE);
		}
		if(new PRWriter(entry, wrapType).write(writer)) {
			writer.write(SEPARATOR_LINE);	
		}
		if(new DTWriter(entry, isConvff).write(writer)) {
			writer.write(SEPARATOR_LINE);
		}
		if(new DEWriter(entry, wrapType).write(writer)) {
			writer.write(SEPARATOR_LINE);
		}
		if (new KWWriter(entry, wrapType).write(writer)) {
			writer.write(SEPARATOR_LINE);
		}

		// Show each source only once.
		HashSet<String> scientificName = new HashSet<String>();		
		for (Feature feature : entry.getFeatures()) {
			if (feature instanceof SourceFeature) {	
				Taxon taxon = ((SourceFeature)feature).getTaxon();
				if (taxon != null && 
					taxon.getScientificName() != null) {
					if (!scientificName.contains(taxon.getScientificName())) {								
						if(new EmblOrganismWriter(entry,
								(SourceFeature)feature, wrapType).write(writer)) {
							writer.write(SEPARATOR_LINE);
						}
					}
					scientificName.add(taxon.getScientificName());
				}
			}
		}

        writeReferences(writer);

        if (new DRWriter(entry).write(writer)) {
        	writer.write(SEPARATOR_LINE);
		}
		if(new CCWriter(entry).write(writer)) {
        	writer.write(SEPARATOR_LINE);
		}
		if(new ASWriter(entry).write(writer)) {
			writer.write(SEPARATOR_LINE);
		}
		//Not expanded entry 
		if( null == entry.getSequence() 
		    || null == entry.getSequence().getSequenceByte()||entry.isNonExpandedCON() )
			writeFeatures( writer );
		
		if(new COWriter(entry, wrapType).write(writer)) {
			if (entry.getSequence() != null &&
				entry.getSequence().getSequenceByte() != null&&!entry.isNonExpandedCON()) {
				writer.write(SEPARATOR_LINE);
			}
		}
		
		//Expanded entry
        if( null != entry.getSequence() 
            && null != entry.getSequence().getSequenceByte()&&!entry.isNonExpandedCON() )
            writeFeatures( writer );
        
        if(new MasterWGSWriter(entry, wrapType).write(writer)) {
			writer.write(SEPARATOR_LINE);
		}
		if(new MasterCONWriter(entry, wrapType).write(writer)) {
			writer.write(SEPARATOR_LINE);
		}

		if((new MasterTPAWriter(entry, wrapType)).write(writer)) {
			writer.write(SEPARATOR_LINE);
		}
		if((new MasterTLSWriter(entry, wrapType)).write(writer)) {
			writer.write(SEPARATOR_LINE);
		}

		(new MasterTSAWriter(entry, wrapType)).write(writer);

		if(entry.getMasterScaffoldAccessions() != null && !entry.getMasterScaffoldAccessions().isEmpty()) {
			new MasterScaffoldWriter(entry, wrapType).write(writer);
		}

		if(!entry.isNonExpandedCON())
        new EmblSequenceWriter(entry, entry.getSequence()).write(writer);
		writer.write(TERMINATOR_LINE);

        writer.flush();
        return true;
	}
	
    @Override
    public void writeFeatures(Writer writer) throws IOException {
        if (new FTWriter(entry, isSortFeatures(), isSortQualifiers(), wrapType).write(writer)) {
            writer.write(SEPARATOR_LINE);
        }
    }

    @Override
    public void writeReferences(Writer writer) throws IOException {
        for (Reference reference : entry.getReferences()) {
            if(new EmblReferenceWriter(entry, reference, wrapType).write(writer)) {
                writer.write(SEPARATOR_LINE);
            }
        }
    }
}
