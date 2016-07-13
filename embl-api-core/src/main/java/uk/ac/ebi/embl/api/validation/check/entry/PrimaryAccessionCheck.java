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
package uk.ac.ebi.embl.api.validation.check.entry;

import java.util.regex.Matcher;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.helper.DataclassProvider;
import uk.ac.ebi.embl.api.validation.helper.EntryUtils;
import uk.ac.ebi.embl.api.validation.helper.Utils;

@Description("invalid accession \"{0}\" for dataclass \"{1}\""
		+ "invalid accession prefix : \"{0}\". Accession prefix is not registered in cv/prefix tables")
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_MASTER})
public class PrimaryAccessionCheck extends EntryValidationCheck {

	private final static String INVALID_ACCESSION_FORMAT_ID = "PrimaryAccessionCheck1";
	private final static String INVALID_ACCESSION_PREFIX_ID = "PrimaryAccessionCheck2";

	public PrimaryAccessionCheck() {

	}
	
		public ValidationResult check(Entry entry) throws ValidationEngineException {
	
		
		if(entry==null)
			return result;	
		if (null == entry.getPrimaryAccession()|| entry.getPrimaryAccession().isEmpty())
		{
			return result;
		}
		
		boolean invalidDataclass= false;

		String accessionPrefix = null;
	if ( Entry.WGS_DATACLASS.equals(entry.getDataClass()) )
		{
			Matcher m = DataclassProvider.WGS_PRIMARY_ACCESSION_PATTERN.matcher( entry.getPrimaryAccession() );			
			if (!m.matches() )	
				invalidDataclass=true;
		}
		else
		if ( Entry.SET_DATACLASS.equals( entry.getDataClass() ) )			
		{
			Matcher m1 = DataclassProvider.WGSMASTER_PRIMARY_ACCESSION_PATTERN.matcher( entry.getPrimaryAccession() );
			Matcher m2 = DataclassProvider.ASSEMBLYMASTER_PRIMARY_ACCESSION_PATTERN.matcher( entry.getPrimaryAccession() );
			if (!m1.matches() && !m2.matches() )
				invalidDataclass=true;
		}
		else
		if ( Entry.TPX_DATACLASS.equals( entry.getDataClass() ) )			
		{
			Matcher m = DataclassProvider.TPX_PRIMARY_ACCESSION_PATTERN.matcher( entry.getPrimaryAccession() );
			if (!m.matches() )
				invalidDataclass =true;
		}
		else
		{
			Matcher m = DataclassProvider.SEQUENCE_PRIMARY_ACCESSION_PATTERN.matcher( entry.getPrimaryAccession() );
			if (!m.matches())
               invalidDataclass= true;
		}
			
		if(invalidDataclass)
		{
			reportError(entry.getOrigin(),INVALID_ACCESSION_FORMAT_ID,entry.getPrimaryAccession(),entry.getDataClass());
		}
		//check accession prefix is registered in cv_database_prefix table
	   else if(getEntryDAOUtils()!=null)
	   {
		   accessionPrefix=EntryUtils.getAccessionPrefix(entry.getPrimaryAccession());
		   try
		   {
		   String dbcode=getEntryDAOUtils().getDbcode(accessionPrefix);
		   if(dbcode==null)
		   {
				reportError(entry.getOrigin(),INVALID_ACCESSION_PREFIX_ID,accessionPrefix);
		   }
		   
		   }catch(Exception e)
		   {
			   throw new ValidationEngineException(e.getMessage());
		   }
	   }
		return result;
	}

}
