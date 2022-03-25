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

import java.util.*;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClientImpl;
import uk.ac.ebi.ena.taxonomy.taxon.SubmittableTaxon;
import uk.ac.ebi.ena.taxonomy.taxon.SubmittableTaxon.SubmittableTaxonStatus;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;
import uk.ac.ebi.ena.taxonomy.util.TaxonUtils;

public class TaxonHelperImpl implements TaxonHelper {
	
	private static TaxonomyClient taxonomyClient= new TaxonomyClientImpl();;
	private static Map<String, List <Taxon>> scientificNameTaxonCache = new HashMap<>();
	private static Map<String, List <Taxon>> commonNameTaxonCache = new HashMap<>();
	private static Map<Long, Taxon> taxIdTaxonCache = new HashMap<>();
	private static Map<String, SubmittableTaxon> submittableTaxonCache = new HashMap<>();
	private static Map<String, List <Taxon>> taxonAnyNameCache = new HashMap<>();
	
    public TaxonHelperImpl() 
    {
    	taxonomyClient= new TaxonomyClientImpl();
    }
    @Override
    public boolean isChildOfAny(String scientificName, String...parentScientificNames)  
    {

		for(String parentName : parentScientificNames)
		{
			if(isChildOf(scientificName, parentName))
				return true;
		}
		return false;
	}
    
    @Override
	public boolean isNotChildOfAny(String scientificName, String...parentScientificNames)  
    {
		return !isChildOfAny(scientificName,scientificName);
	}
	

    @Override
    public boolean isChildOf(String scientificName, String familyScientificName) 
    {
       	List <Taxon> taxons=getTaxonsByScientificName(scientificName);
		if(taxons == null || taxons.isEmpty())
			return false;
	
		for(Taxon taxon:taxons) {
			if(taxon.isChildOf(familyScientificName))
				return true;
		}
	    return false;
    }

    @Override
    public boolean isOrganismValid(String scientificName) {
    	List<Taxon> taxons = getByScientificName(scientificName);
        return taxons != null && !taxons.isEmpty();
    }

    @Override
    public Taxon getTaxonById(Long taxId) {

		Taxon taxon = taxIdTaxonCache.get(taxId);
		if (taxon == null) {
			try {
				taxon = taxonomyClient.getTaxonByTaxid(taxId);
				if(taxon != null ){
					taxIdTaxonCache.put(taxId, taxon);
				}
			} catch (Exception e) {
				return null;
			}
		}
       return taxon;
      } 
    
    @Override
    public List<Taxon> getTaxonsByScientificName(String scientificName) {
		return getByScientificName(scientificName);
    }

   @Override
    public Taxon getTaxonByScientificName(String scientificName) {
    	List<Taxon> taxons = getByScientificName(scientificName);
    	if(taxons == null || taxons.isEmpty())
    		return null;
    	return taxons.get(0);
    }
    
    @Override
    public List<Taxon> getTaxonsByCommonName(String commonName) {
    	return getByCommonName(commonName);
    }
   
    @Override
    public boolean isOrganismFormal(String scientificName) {
		List<Taxon> taxons = getByScientificName(scientificName);
		if(taxons != null && !taxons.isEmpty() ) {
			for (Taxon taxon : taxons) {
				if (taxon.isFormal())
					return true;
			}
		}
		return false;
	}
    
    
	
    @Override
	public boolean isOrganismMetagenome(String scientificName) 
	{
		List<Taxon> taxons = getByScientificName(scientificName);
    	if(taxons == null || taxons.isEmpty())
    		return false;
		for(Taxon taxon:taxons)
		{
			if(taxon.isMetagenome())
			return true;
		}
		return false;
	}

    @Override
	public boolean isProkaryotic(String scientificName) {
		if (scientificName != null
				&& isOrganismValid(scientificName)
				&& (isChildOf(scientificName, "Bacteria") 
				    || isChildOf(scientificName, "Archaea"))) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isOrganismSubmittable(String scientificName) {
		return TaxonUtils.getSubmittableTaxonStatus( getByScientificName(scientificName) ) == SubmittableTaxonStatus.SUBMITTABLE_TAXON;
	}
	
	@Override
	public boolean isTaxidSubmittable(Long taxId) {
    	Taxon taxon = getTaxonById(taxId);
		return taxon !=null && TaxonUtils.getSubmittableTaxonStatus(Collections.singletonList(taxon)) == SubmittableTaxonStatus.SUBMITTABLE_TAXON;
	}

	@Override
	public boolean isAnyNameSubmittable(String anyName) {
    	String key = normalizeString(anyName);
		SubmittableTaxon taxon = submittableTaxonCache.get(key);
    	if( taxon == null) {
			taxon = taxonomyClient.getSubmittableTaxonByAnyName(anyName);
			if(taxon == null) {
				return false;
			} else {
				submittableTaxonCache.put(key, taxon);
			}
		}
		return taxon.getSubmittableTaxonStatus() == SubmittableTaxonStatus.SUBMITTABLE_TAXON;
	}

	@Override
	public List<Taxon> getTaxonsByAnyName(String anyName) {
		String key = normalizeString(anyName);
		List<Taxon> taxonList = taxonAnyNameCache.get(key);
		if( taxonList == null || taxonList.isEmpty()) {
			taxonList = taxonomyClient.getTaxonByAnyName(anyName);
			if(taxonList == null) {
				return null;
			} else {
				taxonAnyNameCache.put(key, taxonList);
			}
		}
		return taxonList;
	}

	private List<Taxon> getByScientificName(String scientificName) {
    	String key = normalizeString(scientificName);
		List<Taxon> taxons= scientificNameTaxonCache.get(key);
		if(taxons == null) {
			try {
				taxons = taxonomyClient.getTaxonByScientificName(scientificName);
				cacheTaxons(taxons, key, true);
			} catch (Exception e) {
				return null;
			}
		}
		return taxons;
	}

	private List<Taxon> getByCommonName(String commonName) {
    	String key = normalizeString(commonName);
		List<Taxon> taxons= commonNameTaxonCache.get(key);
		if(taxons == null) {
			try {
				taxons = taxonomyClient.getTaxonByCommonName(commonName);
				cacheTaxons(taxons, key, false);
			} catch (Exception e) {
				return null;
			}
		}
		return taxons;
	}

	private void cacheTaxons(List<Taxon> taxons, String taxonName, boolean isScientific) {
    	if( taxons !=null && !taxons.isEmpty()) {
    		if(isScientific) {
				scientificNameTaxonCache.put(taxonName, taxons);
			} else {
    			commonNameTaxonCache.put(taxonName, taxons);
			}
			taxons.forEach(taxon -> taxIdTaxonCache.put(taxon.getTaxId(), taxon) ) ;
		}
	}

	private String normalizeString(String string){
    	if (string == null) {
    		return  string;
		}
		return StringUtils.normalizeSpace(string.toUpperCase());
	}

}

