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
package uk.ac.ebi.embl.api.validation.helper.taxon;

import java.util.List;

import uk.ac.ebi.ena.taxonomy.taxon.Taxon;


public interface TaxonHelper {

	/**
	 * Checks whether taxonomy with provided scientific name belongs to 
	 * any of the taxonomy with provided parent scientific name. It also 
	 * returns true when both scientific names are the same.  
	 * 
	 * @param scientificName scientific name of the taxonomy to be checked
	 * @param parentScientificNames scientific names of parent taxonomy
	 * @return true if taxonomy is a child of any parent taxonomy (or both are 
	 * the same taxonomy) 
	 */
	boolean isChildOfAny(String scientificName, 
			String... parentScientificNames);

	/**
	 * Checks whether taxonomy with provided scientific name does not belong to 
	 * any of the taxonomy with provided parent scientific name. 
	 * 
	 * @param scientificName scientific name of the taxonomy to be checked
	 * @param parentScientificNames scientific name of parent taxonomy
	 * @return true if taxonomy is not a child of each of parent taxonomy 
	 */
	boolean isNotChildOfAny(String scientificName, 
			String... parentScientificNames);

	/**
	 * Checks whether taxonomy with provided scientific name belongs to 
	 * the taxonomy with provided parent scientific name. It also 
	 * returns true when both scientific names are the same.  
	 * 
	 * @param scientificName scientific name of the taxonomy to be checked
	 * @param familyScientificName scientific name of family taxonomy
	 * @return true if taxonomy is a child of the parent taxonomy (or both are 
	 * the same taxonomy)
	 */
	boolean isChildOf(String scientificName, String familyScientificName);

    boolean isOrganismValid(String scientificName);
    
    uk.ac.ebi.ena.taxonomy.taxon.Taxon getTaxonById(Long taxId);
    
    boolean isOrganismMetagenome(String scientificName);
    boolean isOrganismFormal(String scientificName);
    boolean isProkaryotic(String scientificName);

    Taxon getTaxonByScientificName(String scientificName);
    
    List<Taxon> getTaxonsByScientificName(String scientificName);

    List<Taxon> getTaxonsByCommonName(String commonName);
  
	boolean isOrganismSubmittable(String scientificName);

    boolean isTaxidSubmittable(Long taxId);
    
    boolean isAnyNameSubmittable(String anyName);
}
