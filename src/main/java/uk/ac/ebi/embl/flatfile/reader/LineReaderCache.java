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

import java.util.HashMap;

import uk.ac.ebi.embl.api.entry.reference.Publication;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;

public class LineReaderCache {

    public LineReaderCache() {
    	resetReferenceCache();
    	resetOrganismCache();	
    }

    private Reference reference;
    private String scientificName;
    private String molType;
    private HashMap<String, String> commonName;
    private HashMap<String, String> lineage;
    private HashMap<String, Long> taxId;
    private HashMap<String, Integer> tagCounts = new HashMap<String, Integer>();

    public void resetReferenceCache() {
    	ReferenceFactory referenceFactory = new ReferenceFactory();
		reference = referenceFactory.createReference();
		reference.setPublication(referenceFactory.createPublication());
    }

    public void resetOrganismCache() {
    	commonName = new HashMap<String, String>();    	
    	lineage = new HashMap<String, String>();
    	taxId = new HashMap<String, Long>();
    }
         
    public Reference getReference() {
    	return reference;
    }
      
    public Publication getPublication() {
    	return reference.getPublication();
    }

    public void setPublication(Publication publication) {
    	reference.setPublication(publication);
    }    
    
    public void setScientificName(String scientificName) {
    	this.scientificName = scientificName;
    }

    public void setCommonName(String commonName) {
    	this.commonName.put(this.scientificName, commonName);
    }

    public void setTaxId(Long taxId) {
    	this.taxId.put(this.scientificName, taxId);
    }

    public void setLineage(String lineage) {
    	this.lineage.put(scientificName, lineage);
    	// The scientific name may be followed with a common name
    	// or a strain name. If it is a strain name then the 
    	// strain will be repeated in the /organism qualifier.
    	String commonName = getCommonName(scientificName);
    	if (commonName != null) {
    		this.lineage.put(scientificName + " (" + commonName + ")", lineage);	
    	}
    }
    
    public String getCommonName(String scientificName) {
    	return commonName.get(scientificName);
    }    

    public Long getTaxId(String scientificName) {
    	return taxId.get(scientificName);
    }

    public String getLineage(String scientificName) {
      	return lineage.get(scientificName);
    }

    public void countTag(String tag) {
        if (tagCounts.containsKey(tag)) {
            Integer count = tagCounts.get(tag);
            tagCounts.put(tag, ++count);
        } else {
            tagCounts.put(tag, 1);
        }
    }

    public HashMap<String, Integer> getTagCounts() {
        return tagCounts;
    }

    public void setMolType(String molType) {
        this.molType = molType;
    }

    public String getMolType() {
        return molType;
    }
}
