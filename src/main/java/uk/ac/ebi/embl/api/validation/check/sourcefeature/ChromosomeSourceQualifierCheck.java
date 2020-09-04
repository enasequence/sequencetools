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
package uk.ac.ebi.embl.api.validation.check.sourcefeature;

import java.util.ArrayList;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;

@GroupIncludeScope(group = { ValidationScope.Group.ASSEMBLY })
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_MASTER, ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class ChromosomeSourceQualifierCheck extends FeatureValidationCheck {

	private final static String CHROMOSOME_SOURCE_QUALIFIER_ERROR = "ChromosomeSourceQualiferCheck_1";	
	private final static String CHROMOSOME_SOURCE_QUALIFIER_MISSING_ERROR = "ChromosomeSourceQualiferCheck_2";	

    private Entry entry;
    
    public void setEntry(Entry entry) {
        this.entry = entry;
    }
	
	@Override
	public ValidationResult check(Feature feature) throws ValidationEngineException 
	{
		result = new ValidationResult();
		
		if(feature==null)
			return result;
		
		 if (!(feature instanceof SourceFeature)) {
	            return result;
	        }
		 
		if (!getEmblEntryValidationPlanProperty().validationScope.get().equals(ValidationScope.ASSEMBLY_CHROMOSOME))
		{
			return result;
		}
		
		
		SourceFeature source = (SourceFeature) feature;
		
		/*if(getEmblEntryValidationPlanProperty().analysis_id.get()!=null)
		{
			try
			{
			  ArrayList<Qualifier> chromosomeQualifiers =getEntryDAOUtils().getChromosomeQualifiers(getEmblEntryValidationPlanProperty().analysis_id.get(), entry.getSubmitterAccession(), source);
			  if(chromosomeQualifiers==null||chromosomeQualifiers.isEmpty())
				  reportError(entry.getOrigin(),CHROMOSOME_SOURCE_QUALIFIER_MISSING_ERROR, entry.getSubmitterAccession());				  			
			
			}catch(Exception e)
			{
				throw new ValidationEngineException(e);
			}
		}*/
		int cnt = 0;
			
		if(source.getSingleQualifier(Qualifier.ORGANELLE_QUALIFIER_NAME)!=null)
		   cnt++;
			
		if (source.getSingleQualifier(Qualifier.CHROMOSOME_QUALIFIER_NAME) != null) 
			cnt++;
		
		if(source.getSingleQualifier(Qualifier.PLASMID_QUALIFIER_NAME) != null)
			cnt++;
		
		if (source.getSingleQualifier(Qualifier.SEGMENT_QUALIFIER_NAME) != null)					
			cnt++;
		
        if(SequenceEntryUtils.isQualifierWithValueAvailable(Qualifier.NOTE_QUALIFIER_NAME,"monopartite", source))
        	cnt++;
		
		if (cnt != 1)
		{
			reportMessage(Severity.ERROR, source.getOrigin(), CHROMOSOME_SOURCE_QUALIFIER_ERROR);
		}			

		return result;

	}
}
