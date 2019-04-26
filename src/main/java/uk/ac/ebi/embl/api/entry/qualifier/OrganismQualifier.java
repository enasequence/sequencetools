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
package uk.ac.ebi.embl.api.entry.qualifier;

import java.io.Serializable;

import uk.ac.ebi.embl.api.taxonomy.HasTaxon;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;
import uk.ac.ebi.ena.taxonomy.taxon.TaxonFactory;


public class OrganismQualifier extends Qualifier implements HasTaxon, Serializable {
	
	private static final long serialVersionUID = -2651509305542549976L;

	private Taxon taxon  = (new TaxonFactory()).createTaxon();
	
	protected OrganismQualifier(String value) {
		super(ORGANISM_QUALIFIER_NAME, null);
		setValue(value);
	}

	public String getValue() {
		return taxon.getScientificName();
	}

	public void setValue(String value) {
		taxon.setScientificName(value);
	}
	
	public Taxon getTaxon() {
		return taxon;
	}

	public void setTaxon(Taxon taxon) {
		this.taxon = taxon;
	}
}
