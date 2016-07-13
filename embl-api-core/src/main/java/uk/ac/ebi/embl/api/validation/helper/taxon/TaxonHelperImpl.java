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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.ebi.embl.api.taxonomy.Taxon;
import uk.ac.ebi.embl.api.taxonomy.TaxonFactory;


public class TaxonHelperImpl implements TaxonHelper {
	
	private final String   scientificNameUri="http://www.ebi.ac.uk/ena/data/taxonomy/v1/taxon/scientific-name/";
	private final String   taxidUri="http://www.ebi.ac.uk/ena/data/taxonomy/v1/taxon/tax-id/";
	private final String   commonNameUri="http://www.ebi.ac.uk/ena/data/taxonomy/v1/taxon/common-name/";
    static Map<String, Taxon> taxonScientificNameCache = Collections.synchronizedMap(new HashMap<String, Taxon>());
    static Map<Long, Taxon> taxonIdCache = Collections.synchronizedMap(new HashMap<Long, Taxon>());
    static Map<String, Taxon> taxonCommonNameCache = Collections.synchronizedMap(new HashMap<String, Taxon>());

    public TaxonHelperImpl() {
	
    }
    @Override
    public boolean isChildOfAny(String scientificName, String...parentScientificNames)  {

		Taxon taxon=taxonScientificNameCache.get(scientificName);
		if(taxon==null)
		{
			taxon=getTaxonsByScientificName(scientificName);
			if(taxon==null)
				return false;
		}
		
		for(String parentName : parentScientificNames)
		{
			if(taxon.isChildOf(parentName))
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
    	
    	Taxon taxon=taxonScientificNameCache.get(scientificName);
		if(taxon==null)
		{
			taxon=getTaxonsByScientificName(scientificName);
			if(taxon==null)
				return false;
		}
		
          return taxon.isChildOf(familyScientificName);
    }

    @Override
    public boolean isOrganismValid(String scientificName) {
    	
		Taxon taxon=taxonScientificNameCache.get(scientificName);
        if(taxon==null)
        {
        	taxon=getTaxonsByScientificName(scientificName);
        }
        return taxon!=null;
    }

    @Override
    public Taxon getTaxonById(Long taxId) {
    	if(taxId==null)
    		return null;
    	TaxonFactory taxonFactory=new TaxonFactory();
    	Taxon taxon=null;
    try{
    	String uri=taxidUri+taxId;
    	if(taxonIdCache.get(taxId)==null)
    	{
    		 URL url = new URL(uri.replaceAll(" ", "%20"));
    		if(isURLNotValid(url))
    		{
    			 taxonIdCache.put(taxId, taxon);
    			 return taxon;
    		}
    		 BufferedReader in = null;
    			in = new BufferedReader(new InputStreamReader(url.openStream()));
    			String currLine = "";
    			StringBuilder taxonString = new StringBuilder();
    			while ((currLine = in.readLine()) != null) {
    				taxonString.append(currLine);
    			}
    			if(taxonString!=null&&isJSONObjectValid(taxonString.toString()))
    			{
    				JSONObject jsonTaxonObject=new JSONObject(taxonString.toString());
        			taxon=taxonFactory.createTaxon(jsonTaxonObject);
        		}
    			 taxonIdCache.put(taxId, taxon);
    	}
    	else
    	{
            taxon=taxonIdCache.get(taxId);
    	}
    }catch(Exception e)
     {
    	  e.printStackTrace();
     }
    	return taxon;

        } 

    @Override
    public Taxon getTaxonsByScientificName(String scientificName) {

    	if(scientificName==null)
    		return null;
		TaxonFactory taxonFactory=new TaxonFactory();
    	Taxon taxon=null;
    	
    	
    try{
    	String uri=scientificNameUri+scientificName;
    	if(taxonScientificNameCache.get(scientificName)==null)
    	{
    		    URL url = new URL(uri.replaceAll(" ", "%20"));
    		    if(isURLNotValid(url))
        		{
        			taxonScientificNameCache.put(scientificName, taxon);
        			 return taxon;
        		}
        		
    			BufferedReader in = null;
    			in = new BufferedReader(new InputStreamReader(url.openStream()));
    			String currLine = "";
    			StringBuilder taxonString = new StringBuilder();
    			while ((currLine = in.readLine()) != null) {
    				taxonString.append(currLine);
    			}
    			if(taxonString!=null&&isJSONArrayValid(taxonString.toString()))
    			{
        		JSONArray jsonTaxonObject=new JSONArray(taxonString.toString());
    			taxon=taxonFactory.createTaxon(jsonTaxonObject.getJSONObject(0));
    			}
                taxonScientificNameCache.put(scientificName, taxon);
    	}
    	else
    	{
            taxon=taxonScientificNameCache.get(scientificName);
    	}
    }catch(Exception e)
     {
	  e.printStackTrace();
     }
    	return taxon;
    }
    
    @Override
    public Taxon getTaxonsByCommonName(String commonName) {

    	if(commonName==null)
    		return null;
		TaxonFactory taxonFactory=new TaxonFactory();
    	Taxon taxon=null;
    	
    	
    try{
    	String uri=commonNameUri+commonName;
    	if(taxonCommonNameCache.get(commonName)==null)
    	{
    		    URL url = new URL(uri.replaceAll(" ", "%20"));
    		   	if(isURLNotValid(url))
        		{
        			taxonCommonNameCache.put(commonName, taxon);
        			 return taxon;
        		}
    			BufferedReader in = null;
    			in = new BufferedReader(new InputStreamReader(url.openStream()));
    			String currLine = "";
    			StringBuilder taxonString = new StringBuilder();
    			while ((currLine = in.readLine()) != null) {
    				taxonString.append(currLine);
    			}
    			if(taxonString!=null&&isJSONArrayValid(taxonString.toString()))
    			{
    			JSONArray jsonTaxonObject=new JSONArray(taxonString.toString());
    			taxon=taxonFactory.createTaxon(jsonTaxonObject.getJSONObject(0));
    			}
    			taxonCommonNameCache.put(commonName, taxon);
    	}
    	else
    	{
            taxon=taxonCommonNameCache.get(commonName);
    	}
    }catch(Exception e)
     {
	  e.printStackTrace();
     }
    	return taxon;
    }
   
    @Override
    public boolean isOrganismFormal(String scientificName) 
	{
		Taxon taxon=taxonScientificNameCache.get(scientificName);
		if(taxon==null)
		{
			taxon=getTaxonsByScientificName(scientificName);
			if(taxon==null)
			 return false;
		}
		
		return taxon.isFormal();
	}
	
    @Override
	public boolean isOrganismMetagenome(String scientificName) 
	{
		Taxon taxon=taxonScientificNameCache.get(scientificName);
		if(taxon==null)
		{
			taxon=getTaxonsByScientificName(scientificName);
			if(taxon==null)
			  return false;
		}
		
		return taxon.isMetagenome();
	}

 private boolean isJSONArrayValid(String taxonString) {
	    try {
	        new JSONArray(taxonString);
	    } catch (JSONException ex) {
	           return false;
	        }
	    return true;
	}
 
 private boolean isJSONObjectValid(String taxonString) {
	 try {
	        new JSONObject(taxonString);
	    } catch (JSONException ex) {
	           return false;
	        }
	    return true;
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
	
	boolean isURLNotValid(URL url) throws IOException
	{
		//String  proxy = "http://wwwcache.sanger.ac.uk";
	    //String port = "3128";
		    HttpURLConnection con=(HttpURLConnection) url.openConnection();
		    con.connect();
		    return con.getResponseCode()>=400;
		
	}
}

