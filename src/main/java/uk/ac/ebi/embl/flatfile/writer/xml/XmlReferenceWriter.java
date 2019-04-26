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
package uk.ac.ebi.embl.flatfile.writer.xml;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.reference.Article;
import uk.ac.ebi.embl.api.entry.reference.Book;
import uk.ac.ebi.embl.api.entry.reference.Patent;
import uk.ac.ebi.embl.api.entry.reference.Person;
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.Submission;
import uk.ac.ebi.embl.api.entry.reference.Thesis;
import uk.ac.ebi.embl.api.entry.reference.Unpublished;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.writer.FlatFileWriter;
import uk.ac.ebi.embl.flatfile.writer.embl.RPWriter;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/** Flat file writer for the reference lines.
 */
public class XmlReferenceWriter {

	private Entry entry;
    private Reference reference;
    
    private static final DateFormat YEAR_FORMAT = 
    		new SimpleDateFormat("yyyy");

	public XmlReferenceWriter(Entry entry, Reference reference) {
		this.entry = entry;
		this.reference = reference;
	}

    public boolean write(SimpleXmlWriter writer) throws IOException {
    	writer.beginElement("reference");
    	
    	Publication publication = reference.getPublication();

    	// Write type.
    	if (publication != null) {
			String type = "";
			if (publication instanceof Book) {
				type = "book";
			} 
			else if (publication instanceof Article) {
				type = "article";
			} 
			else if (publication instanceof Patent) {
				type = "patent";
			}
			else if (publication instanceof Submission) {
				type = "submission";
			} 
			else if (publication instanceof Thesis) {
				type = "thesis";
			} 
			else if (publication instanceof Unpublished) {
				type = "unpublished";
			}
			writer.writeAttribute("type", type);
    	}    	
    	
    	// Write reference number.
    	if (reference.getReferenceNumber() != null) {
    		writer.writeAttribute("number", reference.getReferenceNumber()); 
		}    	
    	
    	// Write location.
		writer.writeAttribute("location", RPWriter.renderLocation(reference));
    	
    	writer.openElement("reference");

    	// Write title.
    	if (publication != null) { 		
    		writer.writeSingleLineTextElement("title", publication.getTitle());
    	}
    	
    	// Write authors.
    	if (publication != null) {
    		for (Person author : publication.getAuthors()) {
    			String name = "";
            	if (!FlatFileWriter.isBlankString(author.getSurname())) {
            		name += author.getSurname().trim();
            	}
            	if (!FlatFileWriter.isBlankString(author.getFirstName())) {
            		name += " ";
            		name += author.getFirstName().trim();
            	}
            	writer.writeSingleLineTextElement("author", name);            	
    		}
    	}

    	// Write patent applicants.
    	if (publication != null &&
			publication instanceof Patent) {
    		Patent patent = (Patent)publication;
			if (patent.getApplicants() != null) {
				for (String applicant : patent.getApplicants()) {
					if (!FlatFileUtils.isBlankString(applicant)) {					
						writer.writeSingleLineTextElement("applicant", applicant);
					}
				}
			}
    	}    	
    	
    	// Write consortium.
    	if (publication != null) {
    		writer.writeSingleLineTextElement("consortium", publication.getConsortium());
    	}

    	// Write the submission date (used only for submission references).
    	if (publication != null) {
    		if (publication instanceof Submission) {
    			writer.writeSingleLineDateElement("submissionDate", ((Submission)publication).getDay());	
    		}
    	}
    	    	
    	// Write the journal name, year, volume, issue, first page, last page (used only for article references).
    	if (publication != null) {
    		if (publication instanceof Article) {
    			writer.writeSingleLineTextElement("journal", ((Article)publication).getJournal());
    			if (((Article)publication).getYear() != null)
    				writer.writeSingleLineTextElement("year", YEAR_FORMAT.format(((Article)publication).getYear()).toUpperCase());
    			writer.writeSingleLineTextElement("volume", ((Article)publication).getVolume());
    			writer.writeSingleLineTextElement("issue", ((Article)publication).getIssue());    			
    			writer.writeSingleLineTextElement("firstPage", ((Article)publication).getFirstPage());
    			writer.writeSingleLineTextElement("lastPage", ((Article)publication).getLastPage());    			
    		}
    	}
    	
    	// Write comment.
    	writer.writeSingleLineTextElement("comment", reference.getComment());

    	// Write reference location.
    	if (publication != null) {
    		(new XmlReferenceLocationWriter(
    				entry, publication)).write(writer);
    	}
    	
    	// Write cross-references.
    	if (publication != null) {
    		(new XmlXrefWriter(publication.getXRefs())).write(writer);    		
    	}    	
    	writer.closeElement("reference");
    	
    	return true;
     }
}
