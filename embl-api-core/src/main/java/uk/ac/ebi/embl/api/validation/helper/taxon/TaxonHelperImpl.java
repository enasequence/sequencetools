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

import uk.ac.ebi.ena.taxonomy.client.TaxonomyClient;
import uk.ac.ebi.ena.taxonomy.client.TaxonomyClientImpl;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;


public class TaxonHelperImpl implements TaxonHelper {
	
	TaxonomyClient taxonomyClient= null;
    public TaxonHelperImpl() {
    	taxonomyClient= new TaxonomyClientImpl();
    }
    @Override
    public boolean isChildOfAny(String scientificName, String...parentScientificNames)  {

			
		for(String parentName : parentScientificNames)
		{
			if(isChildOf(scientificName, parentName))
				return true;
		}
		return false;
	}
    
    @Override
	public boolean isNotChildOfAny(String scientificName, String...parentScientificNames)  {

		return !isChildOfAny(scientificName,scientificName);
	}
	

    @Override
    public boolean isChildOf(String scientificName, String familyScientificName) {
    	
    	List <Taxon> taxons=getTaxonsByScientificName(scientificName);
		if(taxons==null||taxons.size()==0)
			return false;
	
	for(Taxon taxon:taxons)
	{
		if(taxon.isChildOf(familyScientificName))
			return true;
	}
          return false;
    }

    @Override
    public boolean isOrganismValid(String scientificName) {
    	
		List<Taxon> taxons=getTaxonsByScientificName(scientificName);
        if(taxons==null||taxons.size()==0)
        {
        	return false;
        }
        return true;
    }

    @Override
    public Taxon getTaxonById(Long taxId) {
    	 
    	Taxon taxon=null;
       try
       {
    	   taxon= taxonomyClient.getTaxonByTaxid(taxId);
       }catch(Exception e)
       {
    	   return null;
       }
       return taxon;
      } 
    
    @Override
    public List<Taxon> getTaxonsByScientificName(String scientificName) {

    	List<Taxon> taxons= new ArrayList<Taxon>();
    	try{
    		taxons.addAll(taxonomyClient.getTaxonByScientificName(scientificName));
    	}catch(Exception e)
    	{
    		return null;
    	}
    	return taxons;
    	
    }

@Override
    public Taxon getTaxonByScientificName(String scientificName) {
               List<Taxon> taxons=getTaxonsByScientificName(scientificName);
    	if(taxons==null||taxons.size()==0)
    		return null;
    	return taxons.get(0);
    }
    
    @Override
    public List<Taxon> getTaxonsByCommonName(String commonName) {

    	List<Taxon> taxons= new ArrayList<Taxon>();
    	try{
    		taxons.addAll(taxonomyClient.getTaxonByCommonName(commonName));
    	}catch(Exception e)
    	{
    		return null;
    	}
    	return taxons;
    }
   
    @Override
    public boolean isOrganismFormal(String scientificName) 
	{
		List<Taxon> taxons=getTaxonsByScientificName(scientificName);
		if(taxons==null||taxons.size()==0)
    		return false;
		for(Taxon taxon:taxons)
		{
			if(taxon.isFormal())
			return true;
		}
		return false;
	}
    
    
	
    @Override
	public boolean isOrganismMetagenome(String scientificName) 
	{
    	List<Taxon> taxons=getTaxonsByScientificName(scientificName);
    	if(taxons==null||taxons.size()==0)
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
	public boolean isOrganismSubmittable(String scientificName) 
	{
		try{
		Taxon taxon=taxonomyClient.getSubmittableTaxonByScientificName(scientificName);
		if(taxon==null)
			return false;
		}catch(Exception e)
		{
			return false;
		}
		return true;
	}
	
	@Override
	public boolean isTaxidSubmittable(Long taxId) {
		try{
		Taxon taxon=taxonomyClient.getSubmittableTaxonByTaxId(taxId);
		if(taxon==null)
			return false;
		}catch(Exception e)
		{
			return false;
		}
		return true;
	}
	@Override
	public boolean isAnyNameSubmittable(String anyName)
	{
		try{
			Taxon taxon= taxonomyClient.getSubmittableTaxonByAnyName(anyName);
			if(taxon==null)
				return false;
		  }
		catch(Exception e)
		{
			return false;
		}
		return true;
	}
	
}

