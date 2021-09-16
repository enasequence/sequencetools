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
package uk.ac.ebi.embl.flatfile.reader.genbank;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.FlatFileOrigin;
import uk.ac.ebi.embl.flatfile.GenbankTag;
import uk.ac.ebi.embl.flatfile.reader.embl.*;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;
import uk.ac.ebi.embl.flatfile.reader.FeatureReader;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.SequenceReader;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.Severity;

public class GenbankEntryReader extends EntryReader {

    private static final List<String> AT_LEAST_ONCE_BLOCKS = Collections.singletonList(GenbankTag.REFERENCE_TAG);

    private static final List<String> EXACTLY_ONCE_BLOCKS = Arrays.asList(
            GenbankTag.LOCUS_TAG,
            GenbankTag.ACCESSION_TAG,
            GenbankTag.DEFINITION_TAG);

    private static final List<String> NONE_OR_ONCE_BLOCKS = Arrays.asList(
            GenbankTag.KEYWORDS_TAG,
            GenbankTag.PROJECT_TAG,
            GenbankTag.ORIGIN_TAG,
            GenbankTag.PRIMARY_TAG,
            GenbankTag.CONTIG_TAG);

    public GenbankEntryReader(BufferedReader reader) {
    	this(reader, null);
    }

    public GenbankEntryReader(BufferedReader reader, String fileId) {
    	super(new GenbankLineReader(reader, fileId));
    	addBlockReader(new LocusReader(lineReader));
    	addBlockReader(new AccessionReader(lineReader));
    	addBlockReader(new VersionReader(lineReader));
    	addBlockReader(new DefinitionReader(lineReader));
    	addBlockReader(new KeywordsReader(lineReader));
    	addBlockReader(new VersionReader(lineReader));
    	//Delete below line after discussing with the team
    	addBlockReader(new ProjectReader(lineReader));
    	addBlockReader(new DblinkReader(lineReader));
    	addBlockReader(new ContigReader(lineReader));
    	addBlockReader(new OriginReader(lineReader));
    	addBlockReader(new PrimaryReader(lineReader));
    	addBlockReader(new CommentReader(lineReader, FileType.GENBANK));
    	addBlockReader(new FeaturesReader(lineReader));
    	addBlockReader(new SourceReader(lineReader));
    	addBlockReader(new OrganismReader(lineReader));
    	addBlockReader(new ReferenceReader(lineReader));
    	addBlockReader(new AuthorsReader(lineReader));
    	addBlockReader(new ConsrtmReader(lineReader));
    	addBlockReader(new TitleReader(lineReader));
    	addBlockReader(new JournalReader(lineReader, FileType.GENBANK));
    	addBlockReader(new PubmedReader(lineReader));
    	addBlockReader(new RemarkReader(lineReader));
    	addBlockReader(new MedlineReader(lineReader));
    	addBlockReader(new BaseCountReader(lineReader));
    	addBlockReader(new StandardReader(lineReader));
    	addBlockReader(new SegmentReader(lineReader));
		addBlockReader(new MasterWGSReader(lineReader));
		addBlockReader(new MasterCONReader(lineReader));
		addBlockReader(new MasterTLSReader(lineReader));
		addBlockReader(new MasterTSAReader(lineReader));
		addBlockReader(new WGS_SCAFLDReader(lineReader));
		addBlockReader(new TLS_SCAFLDReader(lineReader));
		addBlockReader(new TSA_SCAFLDReader(lineReader));

        /**
         * Have to add the line types seperatly as are not registered with the main hash of readers.
         */
    	getBlockCounter().put(GenbankTag.FEATURES_TAG, 0);
    	getBlockCounter().put(GenbankTag.ORIGIN_TAG, 0);
    }
    
    @Override
    protected boolean readFeature(LineReader lineReader,
    		Entry entry) throws IOException {
    	if (lineReader.getActiveTag().equals(GenbankTag.FEATURES_TAG) && !lineReader.isCurrentTag()) {
    		append((new FeatureReader(lineReader)).read(entry));
            Integer count = getBlockCounter().get(GenbankTag.FEATURES_TAG);
            getBlockCounter().put(GenbankTag.FEATURES_TAG, ++count);
            return true;
    	}
    	return false;
    }    
    
    @Override
    protected boolean readSequence(LineReader lineReader, 
    		Entry entry) throws IOException {
    	if (lineReader.getActiveTag().equals(GenbankTag.ORIGIN_TAG) &&
    		!lineReader.isCurrentTag()) {
    		append((new SequenceReader(lineReader)).read(entry));
    		return true;
    	}
    	return false;
    }

    protected void checkBlockCounts(Entry entry) {
        for(String tag : getBlockCounter().keySet()){
            Integer count = getBlockCounter().get(tag);

            FlatFileOrigin origin = new FlatFileOrigin(currentEntryLine);

            if(AT_LEAST_ONCE_BLOCKS.contains(tag) && count < 1){
                validationResult.append(FlatFileValidations.message(lineReader, Severity.ERROR, "FF.7", origin, tag));
            }

            if(EXACTLY_ONCE_BLOCKS.contains(tag) && count != 1){
                validationResult.append(FlatFileValidations.message(lineReader, Severity.ERROR, "FF.5", origin, tag));
            }

            if(NONE_OR_ONCE_BLOCKS.contains(tag) && count > 1){
                validationResult.append(FlatFileValidations.message(lineReader, Severity.ERROR, "FF.9", origin, tag));
            }

        }
    }
}
