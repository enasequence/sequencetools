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

import java.util.ArrayList;
import java.util.List;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;

@Description("/locus_tag  must exist for annotated contigs/scaffolds/chromosomes")
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_MASTER, ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
@GroupIncludeScope(group = { ValidationScope.Group.ASSEMBLY })
public class LocustagExistsCheck extends EntryValidationCheck {

    private final static String LOCUSTAG_MESSAGE_ID = "LocustagExistsCheck_1";
    
    public ValidationResult check(Entry entry) 
    {
		List<String> excludeFeatureCheckList=  new ArrayList<>();
    	excludeFeatureCheckList.add(Feature.REPEAT_REGION);
    	excludeFeatureCheckList.add(Feature.MISC_FEATURE_NAME);
    	
        result = new ValidationResult();
		
		if (entry == null)
		{
			return result;
		}
		
	   if(FileType.EMBL.equals(getEmblEntryValidationPlanProperty().fileType.get())&&(Entry.WGS_DATACLASS.equals(entry.getDataClass())||Entry.STD_DATACLASS.equals(entry.getDataClass())))
	   {
		   for(String featureName: excludeFeatureCheckList)
		   {
              if(SequenceEntryUtils.getFeatures(featureName, entry).size()!=0)
              {
            	  return result;
              }
		   }
		   
		   if(entry.getPrimarySourceFeature()!=null)
		   {
		   String scientificName=entry.getPrimarySourceFeature().getScientificName();
           TaxonHelper taxonHelper=getEmblEntryValidationPlanProperty().taxonHelper.get();
           if(taxonHelper.isChildOf(scientificName, "Viruses"))
        	   return result;
		   }
		if(SequenceEntryUtils.hasAnnotation(entry)&&!SequenceEntryUtils.isQualifierAvailable(Qualifier.LOCUS_TAG_QUALIFIER_NAME, entry))
		{
			reportMessage(Severity.ERROR, entry.getOrigin(),LOCUSTAG_MESSAGE_ID);
		}
	   }
		return result;
	}

}

