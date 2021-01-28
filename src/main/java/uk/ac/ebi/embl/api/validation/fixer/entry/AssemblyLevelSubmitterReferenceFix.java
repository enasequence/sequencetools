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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

@Description("Submitter Reference has been added to assembly entries")
@GroupIncludeScope(group = { ValidationScope.Group.ASSEMBLY })
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_MASTER, ValidationScope.NCBI_MASTER})
public class AssemblyLevelSubmitterReferenceFix extends EntryValidationCheck
{
	private final String SUBMITTER_REFERENCEFIX_ID = "AssemblyLevelSubmitterReferenceFix_1";


	public ValidationResult check(Entry entry) throws ValidationEngineException
	{
		result = new ValidationResult();

		if (entry == null)
		{
			return result;
		}

		if(Entry.WGS_DATACLASS.equals(entry.getDataClass())||getEraproDAOUtils()==null)
		{
			return result;
		}
		try
		{
		String analysisId=getEmblEntryValidationPlanProperty().analysis_id.get();
		
		Reference reference=getEraproDAOUtils().getSubmitterReference(analysisId);
		if(reference!=null)
		{
			Order<LocalRange> rp = new Order<>();
			rp.addLocation((new LocationFactory()).createLocalRange(1l,entry.getSequence().getLength()));
			if(entry.getSequence()!=null)
				reference.setLocations(rp);
			entry.addReference(reference);
		}
		reportMessage(Severity.FIX, entry.getOrigin(), SUBMITTER_REFERENCEFIX_ID);
		}
		catch(SQLException | UnsupportedEncodingException e)
		{
			throw new ValidationEngineException(e);
		}
		return result;
	}

}
