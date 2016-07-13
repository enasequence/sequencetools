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

import java.io.IOException;
import java.io.Writer;

import uk.ac.ebi.embl.flatfile.writer.EntryWriter;
import uk.ac.ebi.embl.flatfile.writer.WrapType;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.taxonomy.Taxon;
import uk.ac.ebi.embl.flatfile.GenbankTag;

/** Flat file writer for the sequence entry.
 */
public class GenbankEntryWriter extends EntryWriter {

	public GenbankEntryWriter(Entry entry) {
		super(entry);
		wrapType = WrapType.GENBANK_WRAP;
	}
	
	public final static String TERMINATOR_LINE = GenbankTag.TERMINATOR_TAG + "\n";
	
	public boolean write(Writer writer) throws IOException {
		if (entry == null) {
			return false;
		}
		new LocusWriter(entry).write(writer);
		new DefinitionWriter(entry, wrapType).write(writer);
		new AccessionWriter(entry, wrapType).write(writer);
		new VersionWriter(entry).write(writer);
		new ProjectWriter(entry, wrapType).write(writer);
		new DblinkWriter(entry).write(writer);
		new KeywordsWriter(entry, wrapType).write(writer);
		
		SourceFeature sourceFeature = entry.getPrimarySourceFeature();
		if (sourceFeature != null) {
			Taxon taxon = (sourceFeature).getTaxon();
			if (taxon != null && 
				taxon.getScientificName() != null) {
				new GenbankOrganismWriter(entry,
						sourceFeature, wrapType).write(writer);
			}
		}
	
        writeReferences(writer);

		new CommentWriter(entry).write(writer);
		new PrimaryWriter(entry).write(writer);

        writeFeatures(writer);

		new ContigWriter(entry, wrapType).write(writer);

        new GenbankSequenceWriter(entry, entry.getSequence()).write(writer);
		writer.write(TERMINATOR_LINE);

        writer.flush();
        return true;
	}
		
	@Override
    protected void writeFeatures(Writer writer) throws IOException {
        new FeaturesWriter(entry, isSortFeatures(), isSortQualifiers(), wrapType).write(writer);
    }

	@Override
	protected void writeReferences(Writer writer) throws IOException {
        for (Reference reference : entry.getReferences()) {
            new GenbankReferenceWriter(entry, reference, wrapType).write(writer);
        }
    }
}
