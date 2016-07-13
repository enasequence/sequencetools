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
import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.Submission;
import uk.ac.ebi.embl.api.entry.reference.Thesis;
import uk.ac.ebi.embl.api.entry.reference.Unpublished;
import uk.ac.ebi.ena.xml.SimpleXmlWriter;

import java.io.IOException;

/** Flat file writer for the reference lines.
 */
public class XmlReferenceLocationWriter {

	private Entry entry;
    private Publication publication;

	public XmlReferenceLocationWriter(Entry entry, Publication publication) {
		this.entry = entry;
		this.publication = publication;
	}

    public boolean write(SimpleXmlWriter writer) throws IOException {
    	if (publication != null) {
	    	writer.beginElement("referenceLocation");			
	    	writer.openElement("referenceLocation");    		    		
			if (publication instanceof Book) {
				(new XmlBookWriter(
					entry, (Book)publication)).write(writer);
			} 
			else if (publication instanceof Article) {
				(new XmlArticleWriter(
					entry, (Article)publication)).write(writer);
			} 
			else if (publication instanceof Patent) {
				(new XmlPatentWriter(
					(Patent)publication)).write(writer);
			}
			else if (publication instanceof Submission) {
				(new XmlSubmissionWriter(
					entry, (Submission)publication)).write(writer);
			} 
			else if (publication instanceof Thesis) {
				(new XmlThesisWriter(
					entry, (Thesis)publication)).write(writer);
			} 
			else if (publication instanceof Unpublished) {
				(new XmlUnpublishedWriter()).write(writer);
			}
			writer.closeElement("referenceLocation");
			return true;
    	}
    	return false;
     }
}
