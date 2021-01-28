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
import uk.ac.ebi.embl.api.validation.FileType;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.AGPValidationCheck;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.EntryUtils;

import java.sql.SQLException;
import java.util.HashMap;


@Description("")
public class AgptoConFix extends EntryValidationCheck
{
	static final HashMap<String, String> gapType= new HashMap<>();
	static final HashMap<String, String> linkageEvidence= new HashMap<>();

	static
	{
		gapType.put("unknown","unknown");
		gapType.put("repeatnoLinkage","repeat between scaffolds");
		gapType.put("scaffold","within scaffold");
		gapType.put("contig","between scaffolds");
		gapType.put("centromere","centromere");
		gapType.put("short_arm","short arm");
		gapType.put("heterochromatin","heterochromatin");
		gapType.put("telomere","telomere");
		gapType.put("repeatwithLinkage","repeat within scaffold");
		gapType.put("contamination","contamination");
		linkageEvidence.put("pcr","pcr");
		linkageEvidence.put("na","unspecified");
		linkageEvidence.put("paired-ends","paired-ends");
		linkageEvidence.put("align_genus","align genus");
		linkageEvidence.put("align_xgenus","align xgenus");
		linkageEvidence.put("align_trnscpt","align trnscpt");
		linkageEvidence.put("within_clone","within clone");
		linkageEvidence.put("clone_contig","clone contig");
		linkageEvidence.put("map","map");
		linkageEvidence.put("strobe","strobe");
		linkageEvidence.put("unspecified","unspecified");
    }

	public ValidationResult check(Entry entry) throws ValidationEngineException
	{
		result = new ValidationResult();

		if (entry == null||(entry.getSequence()!=null&&entry.getSequence().getAgpRows().size()==0)||!FileType.AGP.equals(getEmblEntryValidationPlanProperty().fileType.get()))
		{
			return result;
		}
		
        AGPValidationCheck check=new AGPValidationCheck();

        check.setEmblEntryValidationPlanProperty(getEmblEntryValidationPlanProperty());
        check.setEntryDAOUtils(getEntryDAOUtils());
        if(!check.check(entry).isValid())
        	return result;
        EntryUtils.convertAGPtofeatureNContigs(entry);
        entry.setDataClass(Entry.CON_DATACLASS);

		return result;
	}

}
