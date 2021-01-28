package uk.ac.ebi.embl.api.validation.check.entries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.annotation.GroupIncludeScope;

@Description("Entry Set has Duplicated entry_names \"{0}\"")
@ExcludeScope(validationScope={ValidationScope.ASSEMBLY_MASTER, ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
@GroupIncludeScope(group = { ValidationScope.Group.ASSEMBLY })
public class Entry_NameCheck extends EntriesValidationCheck
{
	private final static String ENTRY_NAME_ID = "Entry_NameCheck1";

	@Override
	public ValidationResult check(ArrayList<Entry> entryList)
	{
		result = new ValidationResult();
		if(entryList==null)
		{
			return result;
		}
		Set<String> entry_nameSet = new HashSet<>();
		for (Entry entry : entryList)
		{
			String entry_name = entry.getSubmitterAccession();
			if(entry_name==null)
			  continue;
			if (!entry_nameSet.add(entry_name))
			{
				reportError(entry.getOrigin(), ENTRY_NAME_ID, entry_name);
			}
		}

		return result;
	}

}
